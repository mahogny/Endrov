package endrov.recording.frapWindow;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.hardware.EvHardware;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.recording.CameraImage;
import endrov.recording.HWImageScanner;
import endrov.recording.HWImageScannerUtil;
import endrov.roi.ROI;
import endrov.util.EvDecimal;

/**
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class EvFRAPAcquisition extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="frapAcq";
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	
	public EvDecimal getRecoveryTime()
		{
		return recoveryTime;
		}

	public void setRecoveryTime(EvDecimal recoveryTime)
		{
		this.recoveryTime = recoveryTime;
		}

	public EvDecimal getBleachTime()
		{
		return bleachTime;
		}

	public void setBleachTime(EvDecimal bleachTime)
		{
		this.bleachTime = bleachTime;
		}

	public EvDecimal getExpTime()
		{
		return expTime;
		}

	public void setExpTime(EvDecimal expTime)
		{
		this.expTime = expTime;
		}

	public EvDecimal getRate()
		{
		return rate;
		}

	public void setRate(EvDecimal rate)
		{
		this.rate = rate;
		}

	public EvContainer getContainer()
		{
		return container;
		}

	public void setContainer(EvContainer container)
		{
		this.container = container;
		}

	public String getContainerStoreName()
		{
		return containerStoreName;
		}

	public void setContainerStoreName(String containerStoreName)
		{
		this.containerStoreName = containerStoreName;
		}

	public ROI getRoi()
		{
		return roi;
		}

	public void setRoi(ROI roi)
		{
		this.roi = roi;
		}
	
	private EvDecimal recoveryTime;
	private EvDecimal bleachTime;
	private EvDecimal expTime;
	private EvDecimal rate;
	private EvContainer container;
	private String containerStoreName;
	private ROI roi;

	private List<Listener> listeners=new LinkedList<Listener>();
	
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
		private EvFRAPAcquisition settings;
		private boolean toStop=true;

		
		public boolean isRunning()
			{
			return !toStop || isAlive();
			}
		
		public void tryStop()
			{
			toStop=true;
			}
		
		private AcqThread(EvFRAPAcquisition settings)
			{
			this.settings=settings;
			}

		
		
		@Override
		public void run()
			{
			//TODO need to choose camera, at least!
			
			
			acqLoop: do
				{
				Iterator<HWImageScanner> itcam=EvHardware.getDeviceMapCast(HWImageScanner.class).values().iterator();
				HWImageScanner cam=null;
				if(itcam.hasNext())
					cam=itcam.next();
				
				double rate=settings.rate.doubleValue();
				
				/*
				if(settings.rateUnit.equals("ms"))
				else
					interval=1000.0/settings.rate.doubleValue();
				*/
				
				
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

					//TODO signal update on the object
					BasicWindow.updateWindows();

					
					EvDecimal curFrame=new EvDecimal(0);

					
					
					
					
					try
						{
						
						
						//Acquire image before bleaching
						snapOneImage(imset, cam, curFrame);
						
						if(toStop)
							break acqLoop;
						
						//Bleach ROI
						double stageX=0;
						double stageY=0;
						String normalExposureTime=cam.getPropertyValue("Exposure");
						cam.setPropertyValue("Exposure", "1"); //TODO
						int[] roiArray=HWImageScannerUtil.makeROI(cam, roi, stageX, stageY);
						cam.scan(null, null, roiArray);
						cam.setPropertyValue("Exposure", normalExposureTime);
						
						
						
						//TODO
						if(toStop)
							break acqLoop;
						
						
						//Acquire images as the intensity recovers
						for(int i=0;i<recoveryTime.doubleValue()/rate;i++)
							{
							snapOneImage(imset, cam, curFrame);
							
							if(toStop)
								break acqLoop;
							yield(rate/10);
							}
						
						}
					catch (Exception e)
						{
						e.printStackTrace();
						}
					}
			
				}
			while(false);
		
			
			//System.out.println("---------stop-----------");
			toStop=false;
			for(Listener l:listeners)
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
			EvChannel ch=imset.getCreateChannel("ch");
			EvStack stack=ch.getCreateFrame(curFrame);
			//TODO
			stack.resX=1;
			stack.resY=1;
			stack.resZ=EvDecimal.ONE;
			
			CameraImage camIm=cam.snap();
			EvImage evim=new EvImage(camIm.getPixels()[0]);

			System.out.println(camIm.getPixels());
			System.out.println(camIm.getNumComponents());
			
			EvDecimal z=new EvDecimal(0);
			stack.put(z, evim);
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
		EvData.supportedMetadataFormats.put(metaType,EvFRAPAcquisition.class);
		}
	
	}
