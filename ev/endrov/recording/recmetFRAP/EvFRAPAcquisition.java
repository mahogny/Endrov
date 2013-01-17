package endrov.recording.recmetFRAP;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.flow.Flow;
import endrov.flow.FlowConn;
import endrov.flowBasic.constants.FlowUnitConstEvDecimal;
import endrov.flowBasic.control.FlowUnitShow;
import endrov.flowBasic.objects.FlowUnitObjectIO;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardware;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.recording.CameraImage;
import endrov.recording.EvAcquisition;
import endrov.recording.RecordingResource;
import endrov.recording.ResolutionManager;
import endrov.recording.device.HWImageScanner;
import endrov.roi.ROI;
import endrov.util.EvDecimal;

/**
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class EvFRAPAcquisition extends EvAcquisition
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="frapAcq";
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	
	public EvDecimal recoveryTime;
	public EvDecimal bleachTime;
	public EvDecimal rate;
	public ROI roi;

	/**
	 * Thread to perform acquisition
	 */
	public class AcqThread extends Thread implements EvAcquisition.AcquisitionThread
		{
		private EvFRAPAcquisition settings;
		private boolean toStop=true;

		public boolean isRunning()
			{
			return !toStop || isAlive();
			}
		
		private AcqThread(EvFRAPAcquisition settings)
			{
			this.settings=settings;
			}
		
		@Override
		public void run()
			{
			//TODO need to choose camera, at least!
			
			synchronized (RecordingResource.acquisitionLock)
				{

				acqLoop: 
				do
					{
					EvDevicePath campath=EvHardware.getCoreDevice().getCurrentDevicePathImageScanner();
					HWImageScanner cam=EvHardware.getCoreDevice().getCurrentImageScanner();

					
					//Check that there are enough parameters
					if(cam!=null && container!=null)
						{

						Imageset imset=new Imageset();
						for(int i=0;;i++)
							if(container.getChild(containerStoreName+i)==null)
								{
								container.metaObject.put(containerStoreName+i, imset);
								imset.metaObject.put("ch", new EvChannel());
								break;
								}

						ROI copyROI=(ROI)roi.cloneEvObjectRecursive();


						////// Build flow to analyze this experiment
						Flow flow=new Flow();
						
						FlowUnitCalcFRAP unitCalc=new FlowUnitCalcFRAP();
						flow.units.add(unitCalc);
						
						FlowUnitObjectIO unitGetChan=new FlowUnitObjectIO("ch");
						FlowUnitObjectIO unitGetROI=new FlowUnitObjectIO("roi");
						FlowUnitConstEvDecimal unitFrame=new FlowUnitConstEvDecimal(EvDecimal.ZERO);
						FlowUnitShow unitShowLifetime=new FlowUnitShow();
						FlowUnitShow unitShowMobile=new FlowUnitShow();
						FlowUnitShowGraph unitShowSeries=new FlowUnitShowGraph();
						
						flow.units.add(unitGetChan);
						flow.units.add(unitGetROI);
						flow.units.add(unitFrame);
						flow.units.add(unitShowLifetime);
						flow.units.add(unitShowMobile);
						flow.units.add(unitShowSeries);

						flow.conns.add(new FlowConn(unitGetChan,"out",unitCalc,"ch"));
						flow.conns.add(new FlowConn(unitGetROI,"out",unitCalc,"roi"));
						flow.conns.add(new FlowConn(unitFrame,"out",unitCalc,"t1"));
						flow.conns.add(new FlowConn(unitFrame,"out",unitCalc,"t2"));
						flow.conns.add(new FlowConn(unitCalc,"lifetime",unitShowLifetime,"in"));
						flow.conns.add(new FlowConn(unitCalc,"mobile",unitShowMobile,"in"));
						flow.conns.add(new FlowConn(unitCalc,"series",unitShowSeries,"in"));
						
						unitCalc.x=150;
						
						unitFrame.y=0;
						unitGetROI.y=30;
						unitGetChan.y=60;

						unitShowLifetime.x=400;
						unitShowMobile.x=400;
						unitShowSeries.x=420;
						
						unitShowMobile.y=30;
						unitShowSeries.y=60;

						imset.metaObject.put("roi",copyROI);
						imset.metaObject.put("flow",flow);

						//TODO signal update on the object
						BasicWindow.updateWindows();
						
						EvDecimal curFrame=new EvDecimal(0);
						try
							{
							for(EvAcquisition.AcquisitionListener l:listeners)
								l.acquisitionEventStatus("Snap reference");
							
							//Acquire image before bleaching
							snapOneImage(imset, campath, cam, curFrame);
							BasicWindow.updateWindows();

							for(EvAcquisition.AcquisitionListener l:listeners)
								l.acquisitionEventStatus("Bleaching");

							if(toStop)
								break acqLoop;
							
							//Bleach ROI
							double stageX=RecordingResource.getCurrentStageX();
							double stageY=RecordingResource.getCurrentStageY();
							String normalExposureTime=cam.getPropertyValue("Exposure");
							cam.setPropertyValue("Exposure", ""+bleachTime);
							int[] roiArray=RecordingResource.makeScanningROI(campath, cam, copyROI, stageX, stageY);
							cam.scan(null, null, roiArray);
							cam.setPropertyValue("Exposure", normalExposureTime);
							curFrame=curFrame.add(settings.rate); //If frames are missed then this will suck. better base it on real time 
							//TODO also, just bleach time
							

							//Acquire images as the intensity recovers
							for(int i=0;i<settings.recoveryTime.doubleValue()/settings.rate.doubleValue();i++)
								{
								long startTime=System.currentTimeMillis();
								if(toStop)
									break acqLoop;
								
								for(EvAcquisition.AcquisitionListener l:listeners)
									l.acquisitionEventStatus("Recover #"+(i+1));
								
								curFrame=curFrame.add(settings.rate); //If frames are missed then this will suck. better base it on real time 
								
								snapOneImage(imset, campath, cam, curFrame);
								BasicWindow.updateWindows();
								
								waitInTotal(startTime, settings.rate.doubleValue());
								}
							
							}
						catch (Exception e)
							{
							e.printStackTrace();
							}
						
						BasicWindow.updateWindows();
						}
				

					}
				while(false);
			
//				RecordingResource.unblockLiveCamera(lockCamera);
				
				toStop=false;
				for(EvAcquisition.AcquisitionListener l:listeners)
					l.acquisitionEventStopped();
				}
//			Object lockCamera=RecordingResource.blockLiveCamera();
			
			
			}
			
		private void snapOneImage(Imageset imset, EvDevicePath campath, HWImageScanner cam, EvDecimal curFrame)
			{
			CameraImage camIm=cam.snap();
			EvImage evim=new EvImage(camIm.getPixels()[0]);
			
			EvChannel ch=imset.getCreateChannel("ch");
			EvStack stack=new EvStack();//.getCreateFrame(curFrame);
			ch.putStack(curFrame, stack);
			
			ResolutionManager.Resolution res=ResolutionManager.getCurrentResolutionNotNull(campath);
			stack.setRes(res.x,res.y,1);
			stack.setDisplacement(new Vector3d(
					RecordingResource.getCurrentStageX(),
					RecordingResource.getCurrentStageY(),
					0));
			
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
	public EvAcquisition.AcquisitionThread startAcquisition()
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
		return "Acquisition: FRAP";
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
		EvData.supportedMetadataFormats.put(metaType,EvFRAPAcquisition.class);
		}
	
	}
