package endrov.recording.recmetBurst;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.FrameControl;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.ev.EV;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardware;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.imageset.SwapImages;
import endrov.recording.CameraImage;
import endrov.recording.EvAcquisition;
import endrov.recording.RecordingResource;
import endrov.recording.ResolutionManager;
import endrov.recording.device.HWCamera;
import endrov.recording.device.HWTrigger;
import endrov.recording.device.HWTrigger.TriggerListener;
import endrov.util.EvDecimal;

/**
 * Burst acquisition - Using camera in video mode for fast frame rate
 * 
 * @author Johan Henriksson
 *
 */
public class EvBurstAcquisition extends EvAcquisition
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="burstAcq";
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	public EvDecimal duration;
	public String durationUnit;
	public boolean maxDuration;
	
	public EvDecimal rate;
	public String rateUnit;
	
	public boolean earlySwap;
	
	public String channelName;
	public EvContainer container;
	
	public EvDevicePath deviceTriggerOn;
	public EvDevicePath deviceTriggerOff;

	
	public void setDurationSeconds(EvDecimal s)
		{
		this.duration=s;
		durationUnit="Seconds";
		}

	public void setDurationFrames(EvDecimal frames)
		{
		this.duration=new EvDecimal(1).divide(frames);
		durationUnit="Frames";
		}

	
	public void setRateSeconds(EvDecimal rate)
		{
		this.rate=rate.multiply(1000);
		rateUnit="ms";
		}

	public void setRateHz(EvDecimal rate)
		{
		this.rate=new EvDecimal(1).divide(rate);
		rateUnit="Hz";
		}
	

	
	
	/**
	 * Thread to perform acquisition
	 */
	public static class AcqThread extends Thread implements AcquisitionThread
		{
		private EvBurstAcquisition settings;
		private boolean toStop=true;
		int totalFrameCount=0;
		private EvDecimal totalSecCount=EvDecimal.ZERO;
		
		public boolean isRunning()
			{
			return !toStop || isAlive();
			}
		
		public void tryStop()
			{
			toStop=true;
			semTriggered.release();
			}
		
		private boolean shouldContinue()
			{
			if(toStop)
				return false;
			if(settings.maxDuration)
				{
				if(settings.durationUnit.equals("Seconds") && totalSecCount.greater(settings.duration))
					return false;
				if(settings.durationUnit.equals("Frames") && totalFrameCount>=settings.duration.intValue())
					return false;
				}
			return true;
			}
		
		private AcqThread(EvBurstAcquisition settings)
			{
			this.settings=settings;
			}

		
		private Semaphore semTriggered=new Semaphore(0);
		
		private TriggerListener listenerOn=new TriggerListener()
			{
			public void triggered()
				{
				System.out.println("trigger on");
				semTriggered.drainPermits();
				semTriggered.release();
				System.out.println("trigger on2");
				}
			};
		
		private TriggerListener listenerOff=new TriggerListener()
			{
			public void triggered()
				{
				System.out.println("trigger off");
				semTriggered.drainPermits();
				}
			};	
			
				
		@Override
		public void run()
			{
			EvDecimal firstFrame=null;
			
			if(settings.deviceTriggerOn!=null)
				((HWTrigger)settings.deviceTriggerOn.getDevice()).addTriggerListener(listenerOn);
			if(settings.deviceTriggerOff!=null)
				((HWTrigger)settings.deviceTriggerOff.getDevice()).addTriggerListener(listenerOff);

			//Get current camera
			HWCamera cam=EvHardware.getCoreDevice().getCurrentCamera();
			EvDevicePath campath=EvHardware.getCoreDevice().getCurrentDevicePathCamera();
			
			EvDecimal interval;
			double intervalMS;
			if(settings.rateUnit.equals("ms"))
				{
				interval=settings.rate.divide(new EvDecimal(1000));
				intervalMS=settings.rate.doubleValue();
				}
			else
				{
				interval=new EvDecimal(1).divide(settings.rate);
				intervalMS=1000.0/settings.rate.doubleValue();
				}
			EvDecimal actualInt=interval;
			
			
			/*
			int durFrames;
			if(settings.durationUnit.equals("Seconds"))
				{
				durFrames=settings.duration.divide(interval).intValue();
				}
			else
				{
				//Frames
				durFrames=settings.duration.intValue();
				}
			*/
			
			//Check that there are enough parameters
			if(cam!=null && settings.container!=null)
				{
				//Object lockCamera=RecordingResource.blockLiveCamera();
				
				synchronized (RecordingResource.acquisitionLock)
					{

					//If there is no on-triggerer, then enable it first
					semTriggered.drainPermits();
					if(settings.deviceTriggerOn==null)
						listenerOn.triggered();

					System.out.println("1 num permits "+semTriggered.availablePermits());


					try
						{
						//For all repeated burst rounds
						while(shouldContinue())
							{
							//Wait for start trigger
							semTriggered.acquire();
							semTriggered.release();

							//Early stop to avoid creation of object. Not really needed otherwise
							if(toStop)
								break;

							boolean isRGB=false;

							//Set up the imageset
							Imageset imset=new Imageset();
							for(int i=0;;i++)
								{
								String suggestName;
								if(settings.deviceTriggerOn!=null && settings.deviceTriggerOff!=null)
									suggestName="im"+EV.pad(i, 8);
								else
									suggestName="im"+i;
								
								
								if(settings.container.getChild(suggestName)==null)
									{
									settings.container.metaObject.put(suggestName, imset);

									if(isRGB)
										{
										imset.metaObject.put(settings.channelName+"R", new EvChannel());
										imset.metaObject.put(settings.channelName+"G", new EvChannel());
										imset.metaObject.put(settings.channelName+"B", new EvChannel());
										}
									else
										imset.metaObject.put(settings.channelName, new EvChannel());
									break;
									}
								}

							//TODO signal update on the object
							BasicWindow.updateWindows();

							//Start one sequence acquisition
							while(shouldContinue() && semTriggered.availablePermits()>0)
								{
								//Decide starting time using wall-clock
								EvDecimal curFrame=new EvDecimal(System.currentTimeMillis()).divide(new EvDecimal(1000));
								if(firstFrame==null)
									firstFrame=curFrame;
								curFrame=curFrame.subtract(firstFrame);
								
								
								cam.startSequenceAcq(intervalMS);

								//Pull out real interval time from camera
								EvDecimal tmp=cam.getActualSequenceInterval();
								if(tmp!=null)
									actualInt=tmp;
								
								System.out.println("2 num permits "+semTriggered.availablePermits());
								System.out.println("Running sequence");

								//Run acquisition until triggered off
								while(shouldContinue() && semTriggered.availablePermits()>0)
									{
									//Avoid busy loop
									try
										{
										Thread.sleep((long)intervalMS/3);
										}
									catch (Exception e)
										{
										e.printStackTrace();
										}

									//Optional (well, currently not): Use the wall clock time instead of the calculated time using intervals.
									//This can be inaccurate, but the calculated time does not include the time for data transfer so the
									//clock would actually run slower than the real clock.
//									curFrame=new EvDecimal(System.currentTimeMillis()).divide(new EvDecimal(1000));
//									curFrame=curFrame.subtract(firstFrame);
									
									//See if another image is incoming
									CameraImage camIm=cam.snapSequence();
									if(camIm!=null)
										{
										
										System.out.println("burst snap, time: "+curFrame+"   "+FrameControl.formatTime(curFrame));
										
										
										System.out.println("Got image");
										
										if(isRGB)
											{
											//TODO
											
											}
										else
											{
											ResolutionManager.Resolution res=ResolutionManager.getCurrentResolutionNotNull(campath);
											
											EvChannel ch=(EvChannel)imset.getChild(settings.channelName);
											EvStack stack=new EvStack();
											
											stack.setRes(res.x,res.y,1);

											stack.setDisplacement(new Vector3d(
													RecordingResource.getCurrentStageX(),
													RecordingResource.getCurrentStageY(),
													0
													));
											
											EvImage evim=new EvImage(camIm.getPixels()[0]);
											
											System.out.println(camIm.getPixels());
											System.out.println(camIm.getNumComponents());
											
											stack.putInt(0, evim);
											ch.putStack(curFrame, stack);
											
											if(settings.earlySwap)
												SwapImages.hintSwapImage(evim);
											}
											
										//Increase frame and time count
										totalFrameCount++;
										totalSecCount=totalSecCount.add(actualInt);
										curFrame=curFrame.add(actualInt);
										}
									
									}
								System.out.println("Stopping sequence");
								
								cam.stopSequenceAcq();
								
								}
							
							}
						}
					catch (Exception e)
						{
						e.printStackTrace();
						}
					
					
					
					
					}
				
//				RecordingResource.unblockLiveCamera(lockCamera);
				}
			
			//System.out.println("---------stop-----------");
			toStop=false;
			settings.emitAcquisitionEventStopped();
			}
		
		

		public void stopAcquisition()
			{
			toStop=true;
			}
		
		
		private void startAcquisition()
			{
			if(!isRunning())
				{
				toStop=false;
				start();
				}
			}
		
		
		}
	
	
	
	/**
	 * Get acquisition thread that links to this data
	 */
	public AcqThread startAcquisition()
		{
		AcqThread th=new AcqThread(this);
		th.startAcquisition();
		return th;
		}


	@Override
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		}


	@Override
	public String getMetaTypeDesc()
		{
		return "Burst acquisition";
		}


	@Override
	public void loadMetadata(Element e)
		{
		
		// TODO Auto-generated method stub
		
		}


	@Override
	public String saveMetadata(Element e)
		{
		Element eRate=new Element("rate");
		eRate.setAttribute("value",rate.toString());
		eRate.setAttribute("unit",rateUnit);
		e.addContent(eRate);
		
		if(maxDuration)
			{
			Element eDur=new Element("duration");
			eDur.setAttribute("value",duration.toString());
			eDur.setAttribute("unit",durationUnit);
			e.addContent(eDur);
			}
		return metaType;
		}
	

	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,EvBurstAcquisition.class);
		}
	
	}
