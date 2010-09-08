package endrov.recording.recmetMultidim;

import java.util.Iterator;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.flowBasic.math.EvOpImageAddImage;
import endrov.flowBasic.math.EvOpImageDivScalar;
import endrov.hardware.EvHardware;
import endrov.hardware.EvHardwareConfigGroup;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.recording.CameraImage;
import endrov.recording.EvAcquisition;
import endrov.recording.HWCamera;
import endrov.recording.RecordingResource;
import endrov.recording.widgets.RecSettingsChannel;
import endrov.recording.widgets.RecSettingsDimensionsOrder;
import endrov.recording.widgets.RecSettingsPositions;
import endrov.recording.widgets.RecSettingsRecDesc;
import endrov.recording.widgets.RecSettingsSlices;
import endrov.recording.widgets.RecSettingsTimes;
import endrov.recording.widgets.RecSettingsTimes.TimeType;
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

		private Imageset imset=new Imageset();
		private HWCamera cam=null;
		private int currentFrameCount;
		private EvDecimal currentFrame;
		
		private int currentZCount;
		private EvDecimal dz;
		
		private RecSettingsChannel.OneChannel currentChannel;
		
		
		

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
		 * Acquire one plane
		 */
		private class RecOpSnap extends RecOp	
			{
			@Override
			public void exec()
				{
				//Check if this frame should be included
				if(currentChannel.z0>=currentZCount &&
						(currentZCount-currentChannel.z0)%currentChannel.zInc==0 &&
						currentFrameCount%currentChannel.tinc==0)
					{
					//Snap image, average if needed
					CameraImage camIm=cam.snap();
					EvPixels pix=camIm.getPixels()[0];
					if(currentChannel.averaging!=1)
						{
						for(int i=1;i<currentChannel.averaging;i++)
							{
							camIm=cam.snap();
							EvPixels pix2=camIm.getPixels()[0];
							pix=new EvOpImageAddImage().exec1(pix,pix2);
							}
						pix=new EvOpImageDivScalar(currentChannel.averaging).exec1(pix);
						}
					EvImage evim=new EvImage(pix);

					//Get a stack, fill in metadata
					EvChannel ch=imset.getCreateChannel("ch");
					EvStack stack=ch.getCreateFrame(currentFrame);

					stack.resX=RecordingResource.getCurrentTotalMagnification(cam);
					stack.resY=RecordingResource.getCurrentTotalMagnification(cam);
					stack.resZ=dz.multiply(currentChannel.zInc).doubleValue();
					stack.dispX=-RecordingResource.getCurrentStageX();   //always do this?
					stack.dispY=-RecordingResource.getCurrentStageY();
					stack.dispZ=dz.multiply(currentChannel.z0).doubleValue(); //scary!!!
					
					
					int zpos=(currentZCount-currentChannel.z0)/currentChannel.zInc;
					
					stack.putInt(zpos,evim);   //Need to account for the possibility to skip slices!!! and offset!!!
					//int zpos=currentZCount-currentChannel.z0;
					//stack.putInt(zpos, evim);
					
					//Update the GUI
					BasicWindow.updateWindows(); //TODO use hooks
					for(AcquisitionListener listener:listeners)
						listener.newAcquisitionStatus(""+currentChannel.name+"/"+currentFrameCount+"/"+dz.multiply(currentZCount));
					
					}
				
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
					currentChannel=ch;

					//TODO test with proper groups
					EvHardwareConfigGroup.groups.get(channel.metaStateGroup).states.get(ch.name).activate();
					
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
					//Do not move along Z
					currentZCount=0;
					dz=EvDecimal.ONE;
					//currentZ=EvDecimal.ZERO;
					recurse.exec();
					}
				else if(slices.zType==RecSettingsSlices.ZType.NUMZ)
					{
					//Figure out number of slices and spacing
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
						currentZCount=az;
						//currentZ=dz.multiply(az);
						recurse.exec();
						if(toStop)
							return;
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
					currentFrameCount=0;
					currentFrame=EvDecimal.ZERO;
					recurse.exec();
					}
				else
					{
					if(times.freq==null)
						{
						//Run at maximum rate - best effort
						long startTime=System.currentTimeMillis();
						for(int i=0;;i++)
							{
							long thisTime=System.currentTimeMillis();
							currentFrame=new EvDecimal(thisTime-startTime).divide(1000);
							currentFrameCount=i;
							
							if((times.tType==RecSettingsTimes.TimeType.NUMT && i==times.numT) ||
									(times.tType==TimeType.SUMT && currentFrame.greaterEqual(currentFrame)) ||
									toStop)
								return;

							recurse.exec();
							}
						}
					else
						{
						//Run at fixed controlled rate
						EvDecimal dt=times.freq;
						int numt;
						
						if(times.tType==RecSettingsTimes.TimeType.NUMT)
							numt=times.numT;
						else
							numt=times.sumTime.divide(dt).intValue();
						
						for(int at=0;at<numt;at++)
							{
							long timeBefore=System.currentTimeMillis();
							currentFrameCount=at;
							currentFrame=dt.multiply(at);

							recurse.exec();
							
							//Wait until next frame
							long timeAfter;
							do
								{
								timeAfter=System.currentTimeMillis();
								if(toStop)
									return;
								}while((new EvDecimal(timeAfter-timeBefore)).less(dt.multiply(1000)));
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
			if(itcam.hasNext())
				cam=itcam.next();
			
			
			try
				{
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
					 * Prepare object etc
					 */
					String channelName=settings.containerStoreName;
					boolean isRGB=false;
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

					
					
					
					/**
					 * Set up stack and run recording
					 */
					chainOps(ops);
					ops[0].exec();
					

					
					}
				else
					System.out.println("No camera no container");
				
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			
			
			
			
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
