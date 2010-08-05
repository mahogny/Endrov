package endrov.recording.recmetMultidim;

import java.util.Iterator;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.hardware.EvHardware;
import endrov.recording.EvAcquisition;
import endrov.recording.HWCamera;
import endrov.recording.RecordingResource;
import endrov.recording.widgets.RecSettingsChannel;
import endrov.recording.widgets.RecSettingsDimensionsOrder;
import endrov.recording.widgets.RecSettingsPositions;
import endrov.recording.widgets.RecSettingsRecDesc;
import endrov.recording.widgets.RecSettingsSlices;
import endrov.recording.widgets.RecSettingsTimes;
import endrov.util.EvDecimal;


/**
 * Simple multidimensional acquisition - positions, times, stacks, channels
 * 
 * @author Johan Henriksson
 *
 */
public class EvMultidimAcquisition extends EvAcquisition
	{

	
	
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="multidimAcq";
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public RecSettingsDimensionsOrder order;
	public RecSettingsChannel channel;
	public RecSettingsRecDesc desc;
	public RecSettingsSlices slices;
	public RecSettingsTimes times;
	public RecSettingsPositions positions;

	
	
	/**
	 * Thread to perform acquisition
	 */
	public class AcqThread extends Thread implements EvAcquisition.AcquisitionThread
		{
		private EvMultidimAcquisition settings;
		private boolean toStop=true;

		
		private int currentFrameNumber=0;
		
		

		//////////////////////////////
		//////////////////////////////
		//////////////////////////////
		
		
		/**
		 * Handle dimensions by recursing
		 */
		private abstract class RecOp
			{
			public RecOp recurse;
			public abstract void exec();
			}

		/**
		 * Takes the actual picture
		 */
		private class RecOpSnap extends RecOp	
			{
			@Override
			public void exec()
				{
				System.out.println("Snap!");
				}
			}
		
		/**
		 * Change channel and recurse
		 */
		private class RecOpChannel extends RecOp
			{
			public void exec()
				{
				for(RecSettingsChannel.OneChannel ch:channel.channels)
					{
					System.out.println("Channel "+ch.name);
					
					recurse.exec();
					}
				}
			
			}

		/**
		 * Slice dimension: move to the next Z, recurse
		 */
		private class RecOpStack extends RecOp
			{
			public void exec()
				{
				if(slices.zType==RecSettingsSlices.ZType.ONEZ)
					{
					//Do not move anything in Z
					recurse.exec();
					}
				else if(slices.zType==RecSettingsSlices.ZType.NUMZ)
					{
					EvDecimal dz;
					int numz;
					if(slices.zType==RecSettingsSlices.ZType.NUMZ)
						{
						dz=slices.end.subtract(slices.start).divide(new EvDecimal(slices.numZ));
						numz=slices.numZ;
						}
					else //DZ
						{
						numz=slices.end.subtract(slices.start).divide(slices.dz).intValue();
						dz=slices.dz;
						}

					//Iterate through planes
					for(int az=0;az<numz;az++)
						{
						RecordingResource.setCurrentStageZ(slices.start.add(dz.multiply(az)).doubleValue());
						System.out.println("move z");
						recurse.exec();
						}
					
					
					
					}
				}
			
			}

		/**
		 * Only makes sure to wait until the next frame
		 */
		private class RecOpTime extends RecOp
			{
			public void exec()
				{
				if(times.tType==RecSettingsTimes.TimeType.ONET)
					{
					recurse.exec();
					}
				else
					{
					if(times.freq==null)
						{
						//Maximum rate - best effort
						for(int i=0;i<100;i++)
							recurse.exec();
						}
					else
						{
						//Controlled rate
						EvDecimal dt=times.freq;
						int numt;
						
						if(times.tType==RecSettingsTimes.TimeType.NUMT)
							numt=times.numT;
						else
							numt=times.sumTime.divide(dt).intValue();
						
						for(int at=0;at<numt;at++)
							{
							long timeBefore=System.currentTimeMillis();
							
							System.out.println("time "+timeBefore);
							
							recurse.exec();
							
							long timeAfter;
							do
								{
								timeAfter=System.currentTimeMillis();
								}while(dt.less(new EvDecimal(timeAfter-timeBefore)));
							}
						
						}
					
					
					}
				
				}
			}

		/**
		 * Move to the next position, recurse
		 */
		private class RecOpPos extends RecOp
			{
			public void exec()
				{
				//TODO
				recurse.exec();
				}
			}
		
		
		//////////////////////////////
		//////////////////////////////
		//////////////////////////////
		

		/**
		 * Build a call stack out of the operations. Returns the first operation.
		 */
		public RecOp chainOps(final RecOp... ops)
			{
			for(int i=0;i<ops.length-1;i++)
				ops[i].recurse=ops[i+1];
			return ops[0];
			}
		
		
		public boolean isRunning()
			{
			return !toStop || isAlive();
			}
		
		public void tryStop()
			{
			toStop=true;
			}
		
		private AcqThread(EvMultidimAcquisition settings)
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
			
			
			//Check that there are enough parameters
			if(cam!=null && container!=null)
				{
	
				
				//Iterator for all different orders!!!! there are 6. function composition possible?
				//Pass an iterator to an iterator to an iterator

				/**
				 * One iterator for each dimensional order
				 */
				RecOp preop[]=new RecOp[4];
				for(int i=0;i<3;i++)
					if(order.entrylist.get(i).id.equals(RecSettingsDimensionsOrder.ID_POSITION))
						preop[i]=new RecOpPos();
					else if(order.entrylist.get(i).id.equals(RecSettingsDimensionsOrder.ID_CHANNEL))
						preop[i]=new RecOpChannel();
					else if(order.entrylist.get(i).id.equals(RecSettingsDimensionsOrder.ID_SLICE))
						preop[i]=new RecOpStack();
				preop[3]=new RecOpSnap();

				/**
				 * -----time refers to----
				 * pos, chan, slice: one position
				 * pos, slice, chan: one position
				 * chan, pos, slice: all positions
				 * chan, slice, pos: all positions
				 * slice, chan, pos: all positions
				 * slice, pos, chan: all positions
				 */
				RecOp timeOp=new RecOpTime();
				RecOp ops[];
				if(order.entrylist.get(0).id.equals(RecSettingsDimensionsOrder.ID_POSITION))
					ops=new RecOp[]{preop[0],timeOp,preop[1],preop[2],preop[3]};
				else
					ops=new RecOp[]{timeOp, preop[0],preop[1],preop[2],preop[3]};
				
				
				
				
				/** ----Autofocus----
				 * 
				 * 
				 * 
				 * ------------
				 * 
				 * 
				 */

				/**
				 * Set up stack and run recording
				 */
				chainOps(ops);
				ops[0].exec();
				
				/*
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

				
				EvDecimal curFrame=new EvDecimal(0);

				
				
				
				
				try
					{
					
					cam.startSequenceAcq(interval);
					
					while(!toStop)
						{

						//Avoid busy loop
						try
							{
							Thread.sleep((long)interval/3);
							}
						catch (Exception e)
							{
							e.printStackTrace();
							}

						//See if another image is incoming
						CameraImage camIm=cam.snapSequence();
						if(camIm!=null)
							{
							EvChannel ch=(EvChannel)imset.getChild(channelName);
							EvStack stack=ch.getCreateFrame(curFrame);

							if(isRGB)
								{
								//TODO
								
								}
							else
								{
								//TODO resolution
								stack.resX=1;
								stack.resY=1;
								stack.resZ=new EvDecimal(1);
								
								//TODO offset from stage?
								
								EvImage evim=new EvImage(camIm.getPixels()[0]);
								
								
								
								System.out.println(camIm.getPixels());
								System.out.println(camIm.getNumComponents());
								
								EvDecimal z=new EvDecimal(0);
								ch.getCreateFrame(curFrame).put(z, evim);
								}
								
								
							
							
							curFrame=curFrame.add(new EvDecimal(interval));
							}
						
						
						}
					
					cam.stopSequenceAcq();
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
					
					*/
				}
			else
				System.out.println("No camera no container");
			
			//System.out.println("---------stop-----------");
			toStop=false;
			for(EvAcquisition.AcquisitionListener l:listeners)
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
	
	
	public void setStoreLocation(EvContainer con, String name)
		{
		container=con;
		containerStoreName=name;
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
		EvData.supportedMetadataFormats.put(metaType,EvMultidimAcquisition.class);
		}

	
	
	
	
	
	
	
	
	}
