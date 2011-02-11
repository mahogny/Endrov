package endrov.recording.flipWindow;

import java.util.Iterator;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.flow.Flow;
import endrov.flow.FlowConn;
import endrov.flowBasic.objects.FlowUnitObjectIO;
import endrov.hardware.EvHardware;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.recording.CameraImage;
import endrov.recording.EvAcquisition;
import endrov.recording.HWImageScanner;
import endrov.recording.RecordingResource;
import endrov.recording.frapWindow.FlowUnitShowGraph;
import endrov.roi.ROI;
import endrov.util.EvDecimal;

/**
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class EvFLIPAcquisition extends EvAcquisition
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="flipAcq";
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public EvDecimal recoveryTime;
	public EvDecimal bleachTime;
	public EvDecimal rate;
	public int numRepeats;
	public ROI roiBleach;
	public ROI roiObserve;
	
	/**
	 * Thread to perform acquisition
	 */
	public class AcqThread extends Thread implements EvAcquisition.AcquisitionThread
		{
		private EvFLIPAcquisition settings;
		private boolean toStop=true;

		
		public boolean isRunning()
			{
			return !toStop || isAlive();
			}
		
		public void tryStop()
			{
			toStop=true;
			}
		
		private AcqThread(EvFLIPAcquisition settings)
			{
			this.settings=settings;
			}

		
		
		@Override
		public void run()
			{
			//TODO need to choose camera, at least!
			
			
			
			acqLoop: 
			do
				{
				Iterator<HWImageScanner> itcam=EvHardware.getDeviceMapCast(HWImageScanner.class).values().iterator();
				HWImageScanner cam=null;
				if(itcam.hasNext())
					cam=itcam.next();
				
				//Check that there are enough parameters
				if(cam!=null && container!=null)
					{
					synchronized (RecordingResource.acquisitionLock)
						{
					//Object lockCamera=RecordingResource.blockLiveCamera();
						
						
						Imageset imset=new Imageset();
						for(int i=0;;i++)
							if(container.getChild(containerStoreName+i)==null)
								{
								container.metaObject.put(containerStoreName+i, imset);
								imset.metaObject.put("ch", new EvChannel());
								break;
								}

						ROI copyRoiBleach=(ROI)roiBleach.cloneBySerialize();
						imset.metaObject.put("roiBleach",copyRoiBleach);

						////// Build flow to analyze this experiment
						if(roiObserve!=null)
							{
							ROI copyRoiObserve=(ROI)roiObserve.cloneBySerialize();
							
							Flow flow=new Flow();
							
							FlowUnitSumIntensityROI unitCalc=new FlowUnitSumIntensityROI();
							flow.units.add(unitCalc);
							
							FlowUnitObjectIO unitGetChan=new FlowUnitObjectIO(new EvPath("ch"));
							FlowUnitObjectIO unitGetRoiObserve=new FlowUnitObjectIO(new EvPath("roiObserve"));
							FlowUnitShowGraph unitShowSeries=new FlowUnitShowGraph();
							
							flow.units.add(unitGetChan);
							flow.units.add(unitGetRoiObserve);
							flow.units.add(unitShowSeries);

							flow.conns.add(new FlowConn(unitGetChan,"out",unitCalc,"ch"));
							flow.conns.add(new FlowConn(unitGetRoiObserve,"out",unitCalc,"roi"));
							flow.conns.add(new FlowConn(unitCalc,"series",unitShowSeries,"in"));
							
							unitCalc.x=150;
							
							unitGetRoiObserve.y=0;
							unitGetChan.y=30;

							unitShowSeries.x=420;
							unitShowSeries.y=0;

							
							imset.metaObject.put("roiObserve",copyRoiObserve);
							imset.metaObject.put("flow",flow);
							}

						//TODO signal update on the object
						BasicWindow.updateWindows();

						EvDecimal curFrame=new EvDecimal(0);
						try
							{
							//Acquire image before bleaching
							snapOneImage(imset, cam, curFrame);
							BasicWindow.updateWindows();
							
							//Acquire images as the intensity recovers
							for(int i=0;i<settings.numRepeats;i++)
								{
								long startTime=System.currentTimeMillis();

								for(EvAcquisition.AcquisitionListener l:listeners)
									l.newAcquisitionStatus("Doing repeat "+(i+1));

								if(toStop)
									break acqLoop;

								//Bleach ROI
								double stageX=RecordingResource.getCurrentStageX();
								double stageY=RecordingResource.getCurrentStageY();
								String normalExposureTime=cam.getPropertyValue("Exposure");
								cam.setPropertyValue("Exposure", ""+bleachTime);
								int[] roiArray=RecordingResource.makeScanningROI(cam, copyRoiBleach, stageX, stageY);
								cam.scan(null, null, roiArray);
								cam.setPropertyValue("Exposure", normalExposureTime);
								
								if(toStop)
									break acqLoop;

								//Acquire an image for quantification
								snapOneImage(imset, cam, curFrame);
								BasicWindow.updateWindows();
								yield(settings.rate.doubleValue()/10);
								
								waitInTotal(startTime, settings.rate.doubleValue());
								curFrame=curFrame.add(settings.rate); //If frames are missed then this will suck. better base it on real time 
								}
							
							
							
							}
						catch (Exception e)
							{
							e.printStackTrace();
							}
						
						//RecordingResource.unblockLiveCamera(lockCamera);
						
						BasicWindow.updateWindows();
						}
						
					}
			

				}
			while(false);
		
		
		
			toStop=false;
			for(EvAcquisition.AcquisitionListener l:listeners)
				l.acqStopped();
			}
		
		/**
		 * To avoid busy loops
		 */
		private void yield(double t)
			{
			try
				{
				Thread.sleep((long)t);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		
			
		private void snapOneImage(Imageset imset, HWImageScanner cam, EvDecimal curFrame)
			{
			CameraImage camIm=cam.snap();
			EvImage evim=new EvImage(camIm.getPixels()[0]);
			
			EvChannel ch=imset.getCreateChannel("ch");
			EvStack stack=new EvStack();//.getCreateFrame(curFrame);
			ch.putStack(curFrame, stack);
			stack.resX=RecordingResource.getCurrentTotalMagnification(cam);
			stack.resY=RecordingResource.getCurrentTotalMagnification(cam);
			stack.resZ=1;

			stack.setDisplacement(new Vector3d(
					RecordingResource.getCurrentStageX(),
					RecordingResource.getCurrentStageY(),
					0
					));
			
			stack.putInt(0, evim);
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
		
		

		/**
		 * Wait at least a certain time
		 */
		public void waitInTotal(long startTime, double totalDuration)
			{
			for(;;)
				{
				long currentTime=System.currentTimeMillis();
				long dt=startTime+(long)(totalDuration*1000)-currentTime;
				if(dt>0 && !toStop)
					{
					if(dt>10)
						dt=10;
					try
						{
						Thread.sleep(dt);
						}
					catch (InterruptedException e)
						{
						}
					}
				else
					break;
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
	public void buildMetamenu(JMenu menu)
		{
		}


	@Override
	public String getMetaTypeDesc()
		{
		return "FRAP acquisition";
		}


	@Override
	public void loadMetadata(Element e)
		{
		
		// TODO Auto-generated method stub
		
		}


	@Override
	public String saveMetadata(Element e)
		{
		//TODO
		/*
		Element eRate=new Element("rate");
		eRate.setAttribute("value",rate.toString());
		eRate.setAttribute("unit",rateUnit);
		e.addContent(eRate);
		
		Element eDur=new Element("duration");
		eDur.setAttribute("value",duration.toString());
		eDur.setAttribute("unit",durationUnit);
		e.addContent(eDur);
		*/
		return metaType;
		}
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,EvFLIPAcquisition.class);
		}
	
	}
