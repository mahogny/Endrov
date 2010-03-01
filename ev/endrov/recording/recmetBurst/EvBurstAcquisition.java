package endrov.recording.recmetBurst;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.hardware.EvHardware;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.Imageset;
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
			
			//System.out.println("-----------start------------");
			
			Iterator<HWCamera> itcam=EvHardware.getDeviceMapCast(HWCamera.class).values().iterator();
			HWCamera cam=null;
			if(itcam.hasNext())
				cam=itcam.next();
			
			double interval;
			if(settings.rateUnit.equals("ms"))
				interval=settings.rate.doubleValue();
			else
				interval=1000.0/settings.rate.doubleValue();
			
			
			Imageset imset=new Imageset();
			for(int i=0;;i++)
				if(container.getChild("im"+i)==null)
					{
					container.metaObject.put("im"+i, imset);
					imset.metaObject.put("ch0", new EvChannel());
					break;
					}

			EvDecimal curFrame=new EvDecimal(0);
			
			
			if(cam!=null)
				{
				try
					{
					
					cam.startSequenceAcq(interval);
					
					while(!toStop)
						{

						//System.out.println("---------------------");
						
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
					//	System.out.println(im);
						

						EvImage evim=new EvImage();
						evim.setMemoryImage(im.getPixels()[0]);
						
						EvDecimal frame=new EvDecimal("0");
						EvDecimal z=new EvDecimal(0);


						EvChannel ch=(EvChannel)imset.getChild("ch0");
						ch.getCreateFrame(frame).put(z, evim);
						
						
						curFrame=curFrame.add(new EvDecimal(interval));
						
						}
					
					cam.stopSequenceAcq();
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
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
