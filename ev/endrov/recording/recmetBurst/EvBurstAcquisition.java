package endrov.recording.recmetBurst;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.FrameControl;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardware;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.recording.CameraImage;
import endrov.recording.RecordingResource;
import endrov.recording.ResolutionManager;
import endrov.recording.device.HWCamera;
import endrov.recording.device.HWTrigger;
import endrov.recording.device.HWTrigger.TriggerListener;
import endrov.util.EvDecimal;

/**
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class EvBurstAcquisition extends EvObject
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
	
	public EvDecimal rate;
	public String rateUnit;
	
	public boolean earlySwap;
	
	public String channelName;
	public EvContainer container;
	
	public EvDevicePath deviceTriggerOn;
	public EvDevicePath deviceTriggerOff;
	
	private List<Listener> listeners=new LinkedList<Listener>();

	
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
	 * Thread activity listener
	 */
	public interface Listener
		{
		public void acqStopped();
		}
	
	
	public void addListener(Listener l)
		{
		listeners.add(l);
		}

	public void removeListener(Listener l)
		{
		listeners.remove(l);
		}
	
	
	/**
	 * Thread to perform acquisition
	 */
	public class AcqThread extends Thread
		{
		private EvBurstAcquisition settings;
		private boolean toStop=true;

		
		public boolean isRunning()
			{
			return !toStop || isAlive();
			}
		
		public void tryStop()
			{
			toStop=true;
			semTriggered.release();
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
			if(deviceTriggerOn!=null)
				((HWTrigger)deviceTriggerOn.getDevice()).addTriggerListener(listenerOn);
			if(deviceTriggerOff!=null)
				((HWTrigger)deviceTriggerOff.getDevice()).addTriggerListener(listenerOff);
			
			
			//TODO need to choose camera, at least!
			
			Map<EvDevicePath, HWCamera> cams=EvHardware.getDeviceMapCast(HWCamera.class);
			Iterator<EvDevicePath> itcam=cams.keySet().iterator();
			EvDevicePath campath=null;
			HWCamera cam=null;
			if(itcam.hasNext())
				{
				campath=itcam.next();
				cam=cams.get(campath);
				}
			
			
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
			
			EvDecimal firstFrame=null;//new EvDecimal(System.currentTimeMillis()).divide(new EvDecimal(1000));

			
			//Check that there are enough parameters
			if(cam!=null && container!=null)
				{
				//Object lockCamera=RecordingResource.blockLiveCamera();
				
				synchronized (RecordingResource.acquisitionLock)
					{

					boolean isRGB=false;
					
					Imageset imset=new Imageset();
					for(int i=0;;i++)
						if(container.getChild("im"+i)==null)
							{
							container.metaObject.put("im"+i, imset);
							
							if(isRGB)
								{
								imset.metaObject.put(channelName+"R", new EvChannel());
								imset.metaObject.put(channelName+"G", new EvChannel());
								imset.metaObject.put(channelName+"B", new EvChannel());
								}
							else
								imset.metaObject.put(channelName, new EvChannel());
							break;
							}

					//TODO signal update on the object
					BasicWindow.updateWindows();
					
					try
						{
						//If there is no on-triggerer, then enable it first
						semTriggered.drainPermits();
						
						if(deviceTriggerOn==null)
							listenerOn.triggered();
						
						System.out.println("1 num permits "+semTriggered.availablePermits());
						
						while(!toStop)
							{
							//Wait for start trigger
							semTriggered.acquire();
							semTriggered.release();
							
							EvDecimal curFrame=new EvDecimal(System.currentTimeMillis()).divide(new EvDecimal(1000));
							if(firstFrame==null)
								firstFrame=curFrame;
							curFrame=curFrame.subtract(firstFrame);

							cam.startSequenceAcq(intervalMS);

							System.out.println("2 num permits "+semTriggered.availablePermits());
							System.out.println("Running sequence");

							while(!toStop && semTriggered.availablePermits()>0)
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


								System.out.println("time: "+curFrame+"   "+FrameControl.formatTime(curFrame));
								
								//See if another image is incoming
								CameraImage camIm=cam.snapSequence();
								if(camIm!=null)
									{
									
									
									
									System.out.println("Got image");
									
									if(isRGB)
										{
										//TODO
										
										}
									else
										{
										ResolutionManager.Resolution res=ResolutionManager.getCurrentResolutionNotNull(campath);
										
										EvChannel ch=(EvChannel)imset.getChild(channelName);
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
										}
										
									curFrame=curFrame.add(interval);
									}
								
								}
							System.out.println("Stopping sequence");
							
							cam.stopSequenceAcq();
							
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
			for(Listener l:listeners)
				l.acqStopped();
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
		
		Element eDur=new Element("duration");
		eDur.setAttribute("value",duration.toString());
		eDur.setAttribute("unit",durationUnit);
		e.addContent(eDur);
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
