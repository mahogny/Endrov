/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;


import java.util.Map;

import javax.vecmath.Vector3d;

import endrov.data.EvData;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardware;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.keyBinding.JInputManager;
import endrov.roi.LineIterator;
import endrov.roi.ROI;
import endrov.roi.LineIterator.LineRange;
import endrov.util.EvDecimal;
import endrov.util.EvSound;
import endrov.util.ProgressHandle;

/**
 * Resources associated with controlling microscope hardware 
 * @author Johan Henriksson
 *
 */
public class RecordingResource
	{
	public static EvSound soundCameraSnap;

	/**
	 * Acquisition operations started by the user will lock all hardware to avoid overlap
	 */
	public static Object acquisitionLock=new Object();

	/**
	 * TODO
	 * guess magnification by looking at state label
	 */
	
	public static double magFromLabel(String s)
		{
		return 1;
		}

	/**
	 * Get the total magnification for the entire light path, coming into a given camera
	 * [um/px]
	 */
	public static double getCurrentTotalMagnification(HWCamera cam)
		{
		return cam.getResMagX();
		}
	
	
	public static double getCurrentStageX()
		{
		for(HWStage stage:EvHardware.getDeviceMapCast(HWStage.class).values())
			{
			String[] aname=stage.getAxisName();
			for(int i=0;i<aname.length;i++)
				if(aname[i].equals("x"))
					return stage.getStagePos()[i];
			}
//		System.out.println("No stage for X found");
		return 0;
		}

	
	public static HWAutoFocus getOneAutofocus()
		{
		for(HWAutoFocus af:EvHardware.getDeviceMapCast(HWAutoFocus.class).values())
			return af;
		return null;
		}

	/*
	public static void moveAxis(String s, double dx)
		{
		for(Map.Entry<EvDevicePath,HWStage> e:EvHardware.getDeviceMapCast(HWStage.class).entrySet())
			{
			HWStage stage=e.getValue();
			for(int i=0;i<stage.getNumAxis();i++)
				{
				if(stage.getAxisName()[i].equals(s))
					{
					//TODO should if possible set both axis' at the same time. might make it faster
					double[] da=new double[stage.getNumAxis()];
					da[i]=dx;
					stage.setRelStagePos(da);
					return;
					}
				}
			}
		}*/

	
	
	public static double getCurrentStageY()
		{
		for(HWStage stage:EvHardware.getDeviceMapCast(HWStage.class).values())
			{
			String[] aname=stage.getAxisName();
			for(int i=0;i<aname.length;i++)
				if(aname[i].equals("y"))
					return stage.getStagePos()[i];
			}
//		System.out.println("No stage for Y found");
		return 0;
		}


	public static double getCurrentStageZ()
		{
		for(HWStage stage:EvHardware.getDeviceMapCast(HWStage.class).values())
			{
			String[] aname=stage.getAxisName();
			for(int i=0;i<aname.length;i++)
				if(aname[i].equals("z"))
					return stage.getStagePos()[i];
			}
		return 0;
		}

	public static void setCurrentStageZ(double z)
		{
		for(HWStage stage:EvHardware.getDeviceMapCast(HWStage.class).values())
			{
			String[] aname=stage.getAxisName();
			for(int i=0;i<aname.length;i++)
				if(aname[i].equals("z"))
					{
					double[] pos=stage.getStagePos();
					pos[i]=z;
					}
			}
		}

	
	private static EvData data=new EvData();
	
	
	public static EvData getData()
		{
		return data;
		}

	
	/**
	 * Make a ROI mask for image scanners.
	 * offset is in [um]
	 */
	public static int[] makeScanningROI(HWImageScanner scanner, ROI roi, double stageX, double stageY)
		{
		int w=scanner.getWidth();
		int h=scanner.getHeight();
		
		int[] ret=new int[w*h];
		
		EvPixels p=new EvPixels(EvPixelsType.UBYTE, w, h); //TODO avoid this allocation 
		EvImage image=new EvImage(p);
		EvStack stack=new EvStack();

		double res=getCurrentTotalMagnification(scanner);
		stack.setRes(res,res,1);
		
		String channel="foo";
		EvDecimal frame=EvDecimal.ZERO;
		double z=0;
		
		//TODO how to offset?
		stack.setDisplacement(new Vector3d(stageX, stageY, 0));

		ProgressHandle progh=new ProgressHandle();
		
		//Fill bitmap ROI
		LineIterator it=roi.getLineIterator(progh, stack, image, channel, frame, z);
		while(it.next())
			{
			int y=it.y;
			for(LineRange r:it.ranges)
				for(int x=r.start;x<r.end;x++)  //< or <=?
					ret[y*w+x]=1;
			}
		
		//Debug
		/*
		for(int y=0;y<h;y++)
			{
			for(int x=0;x<w;x++)
				if(ret[y*w+x]!=0)
					System.out.print("#");
				else
					System.out.print(".");
			System.out.println();
			}
		*/
		return ret;
		}
	

	/**
	 * Move relative any device with right axis names
	 */
	public static void setRelStagePos(Map<String, Double> axisDiff)
		{
		for(Map.Entry<EvDevicePath,HWStage> e:EvHardware.getDeviceMapCast(HWStage.class).entrySet())
			{
			HWStage stage=e.getValue();
			boolean foundHere=false;
			double[] da=new double[stage.getNumAxis()];
			for(int i=0;i<stage.getNumAxis();i++)
				{
				Double v=axisDiff.get(stage.getAxisName()[i]);
				if(v!=null)
					{
					foundHere=true;
					da[i]=v;
					}
				}
			if(foundHere)
				stage.setRelStagePos(da);
			}
		}

	
	/**
	 * Move any device with right axis names
	 */
	public static void setStagePos(Map<String, Double> axisPos)
		{
		int found=0;
		for(Map.Entry<EvDevicePath,HWStage> e:EvHardware.getDeviceMapCast(HWStage.class).entrySet())
			{
			HWStage stage=e.getValue();
			boolean foundHere=false;
			double[] da=new double[stage.getNumAxis()];
			for(int i=0;i<stage.getNumAxis();i++)
				{
				Double v=axisPos.get(stage.getAxisName()[i]);
				if(v!=null)
					{
					found++;
					foundHere=true;
					da[i]=v;
					}
				}
			if(foundHere)
				stage.setStagePos(da);
			if(found==axisPos.size())
				return;
			}
		}
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		JInputManager.addGamepadMode("Hardware", new JInputModeRecording(), false);
		soundCameraSnap=new EvSound(RecordingResource.class,"camera_click.ogg");
		}
	

	}
