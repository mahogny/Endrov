package endrov.recording.recmetBurst;

import java.util.Iterator;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.hardware.EvHardware;
import endrov.recording.CameraImage;
import endrov.recording.HWCamera;
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
	
	private static final String metaType="burstacquisition";
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	public EvDecimal duration;
	public String durationUnit;
	public EvDecimal rate;
	public String rateUnit;
	
	public boolean earlySwap;
	
	public String channel;
	public EvContainer container;
	

	
	
	/**
	 * Thread to perform acquisition
	 */
	public static class AcqThread extends Thread
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
			}
		
		private AcqThread(EvBurstAcquisition settings)
			{
			this.settings=settings;
			}

		
		
		@Override
		public void run()
			{
			//TODO need to choose camera, at least!
			
			Iterator<HWCamera> itcam=EvHardware.getDeviceMapCast(HWCamera.class).values().iterator();
			HWCamera cam=null;
			if(itcam.hasNext())
				cam=itcam.next();
			
			double interval;
			if(settings.rateUnit.equals("ms"))
				interval=settings.rate.doubleValue();
			else
				interval=1000.0/settings.rate.doubleValue();
			
			
			if(cam!=null)
				{
				try
					{
					
					cam.startSequenceAcq(interval);
					
					while(!toStop)
						{

						System.out.println("---------------------");
						
						//Avoid busy loop
						try
							{
							Thread.sleep((long)interval/3);
							}
						catch (Exception e)
							{
							e.printStackTrace();
							}
						
						CameraImage im=cam.snapSequence();
						System.out.println(im);
						
						
						
						
						}
					
					cam.stopSequenceAcq();
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				}
			
			toStop=false;
			//TODO listener
			}
		
		
		public void stopAcquisition()
			{
			toStop=true;
			}
		
		
		public void startAcquisition()
			{
			toStop=false;
			start();
			}
		
		}
	
	
	/**
	 * Get acquisition thread that links to this data
	 */
	public AcqThread getThread()
		{
		return new AcqThread(this);
		}


	@Override
	public void buildMetamenu(JMenu menu)
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
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,EvBurstAcquisition.class);
		}
	
	}
