/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Vector3d;

import endrov.core.observer.GeneralObserver;
import endrov.data.EvData;
import endrov.gui.keybinding.JInputManager;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvHardware;
import endrov.recording.device.HWAutoFocus;
import endrov.recording.device.HWImageScanner;
import endrov.recording.device.HWStage;
import endrov.roi.LineIterator;
import endrov.roi.ROI;
import endrov.roi.LineIterator.LineRange;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.typeImageset.EvStack;
import endrov.util.EvSound;
import endrov.util.ProgressHandle;
import endrov.util.math.EvDecimal;

/**
 * Resources associated with controlling microscope hardware
 * 
 * @author Johan Henriksson
 */
public class RecordingResource
	{
	public interface PositionListListener
		{
		public void positionsUpdated();
		}

	public static GeneralObserver<PositionListListener> posListListeners = new GeneralObserver<RecordingResource.PositionListListener>();

	public static LinkedList<StoredStagePosition> posList = new LinkedList<StoredStagePosition>();

	public static void posListUpdated()
		{
		for (PositionListListener list : posListListeners.getListeners())
			list.positionsUpdated();
		}

	public static EvSound soundCameraSnap;

	/**
	 * Acquisition operations started by the user will lock all hardware to avoid
	 * overlap
	 */
	public static Object acquisitionLock = new Object();

	private static EvData data = new EvData();

	public static String getUnusedPosName()
		{
		// Find all names in use
		Set<String> usedNames = new HashSet<String>();
		for (StoredStagePosition pos : RecordingResource.posList)
			usedNames.add(pos.getName());

		// Generate an unused name
		String newName;
		int posi = 0;
		do
			{
			newName = "POS"+posi;
			posi++;
			} while (usedNames.contains(newName));
		return newName;
		}

	public static EvData getData()
		{
		return data;
		}

	public static double getCurrentStageX()
		{
		HWStage stage = EvHardware.getCoreDevice().getCurrentXYStage();
		if(stage!=null)
			{
			return stage.getStagePos()[0]; //Not general enough?
			}
		else
			return 0;
		}



	public static double getCurrentStageY()
		{
		HWStage stage = EvHardware.getCoreDevice().getCurrentXYStage();
		if(stage!=null)
			{
			return stage.getStagePos()[1]; //Not general enough?
			}
		else
			return 0;
		}

	public static double getCurrentStageZ()
		{
		HWStage stage = EvHardware.getCoreDevice().getCurrentFocus();
		if(stage!=null)
			{
			return stage.getStagePos()[0]; //Not general enough?
			}
		else
			return 0;
		}

	public static void setCurrentStageZ(double z)
		{
		HWStage stage = EvHardware.getCoreDevice().getCurrentXYStage();
		if(stage!=null)
			{
			stage.setStagePos(new double[]{z});
			}
		}

	/**
	 * Make a ROI mask for image scanners. offset is in [um]
	 */
	public static int[] makeScanningROI(EvDevicePath scannerpath,
			HWImageScanner scanner, ROI roi, double stageX, double stageY)
		{
		int w = scanner.getWidth();
		int h = scanner.getHeight();

		int[] ret = new int[w*h];

		EvPixels p = new EvPixels(EvPixelsType.UBYTE, w, h); // TODO avoid this allocation
		EvImagePlane image = new EvImagePlane(p);
		EvStack stack = new EvStack();

		ResolutionManager.Resolution res = ResolutionManager.getCurrentResolutionNotNull(scannerpath);
		stack.setRes(res.x, res.y, 1);

		String channel = "foo";
		EvDecimal frame = EvDecimal.ZERO;
		double z = 0;

		// TODO how to offset?
		stack.setDisplacement(new Vector3d(stageX, stageY, 0));

		ProgressHandle progh = new ProgressHandle();

		// Fill bitmap ROI
		LineIterator it = roi.getLineIterator(progh, stack, image, channel, frame, z);
		while (it.next())
			{
			int y = it.y;
			for (LineRange r : it.ranges)
				for (int x = r.start; x<r.end; x++)
					// < or <=?
					ret[y*w+x] = 1;
			}

		// Debug
		/*
		 * for(int y=0;y<h;y++) { for(int x=0;x<w;x++) if(ret[y*w+x]!=0)
		 * System.out.print("#"); else System.out.print("."); System.out.println();
		 * }
		 */
		return ret;
		}

	/**
	 * Move relative any device with right axis names
	 */
	public static void setRelStagePos(Map<String, Double> axisDiff)
		{
		HWStage pathXY=EvHardware.getCoreDevice().getCurrentXYStage();
		HWStage pathZ=EvHardware.getCoreDevice().getCurrentFocus();
		
		if(pathXY!=null)
			setRelStagePos(pathXY, axisDiff);
		if(pathZ!=null)
			setRelStagePos(pathZ, axisDiff);
		/*
		
		for (Map.Entry<EvDevicePath, HWStage> e : EvHardware.getDeviceMapCast(
				HWStage.class).entrySet())
			{
			HWStage stage = e.getValue();
			boolean foundHere = false;
			double[] da = new double[stage.getNumAxis()];
			for (int i = 0; i<stage.getNumAxis(); i++)
				{
				Double v = axisDiff.get(stage.getAxisName()[i]);
				if (v!=null)
					{
					foundHere = true;
					da[i] = v;
					}
				}
			if (foundHere)
				stage.setRelStagePos(da);
			}
			*/
		}
	
	
	public static void setRelStagePos(HWStage stage, Map<String, Double> axisDiff)
		{
		boolean foundHere = false;
		double[] da = new double[stage.getNumAxis()];
		for (int i = 0; i<stage.getNumAxis(); i++)
			{
			Double v = axisDiff.get(stage.getAxisName()[i]);
			if (v!=null)
				{
				foundHere = true;
				da[i] = v;
				}
			}
		if (foundHere)
			stage.setRelStagePos(da);
		}
	

	/**
	 * Move any device with right axis names
	 */
	public static void setStagePos(Map<String, Double> axisPos)
		{
		HWStage pathXY=EvHardware.getCoreDevice().getCurrentXYStage();
		HWStage pathZ=EvHardware.getCoreDevice().getCurrentFocus();
		
		if(pathXY!=null)
			setStagePos(pathXY, axisPos);
		if(pathZ!=null)
			setStagePos(pathZ, axisPos);
		
		//Alternative way, where each device must provide unique axis labels. Dropped in favour of the MM way
		/*
		int found = 0;
		for (Map.Entry<EvDevicePath, HWStage> e : EvHardware.getDeviceMapCast(
				HWStage.class).entrySet())
			{
			HWStage stage = e.getValue();
			boolean foundHere = false;
			double[] da = new double[stage.getNumAxis()];
			for (int i = 0; i<stage.getNumAxis(); i++)
				{
				Double v = axisPos.get(stage.getAxisName()[i]);
				if (v!=null)
					{
					found++;
					foundHere = true;
					da[i] = v;
					}
				}
			if (foundHere)
				stage.setStagePos(da);
			if (found==axisPos.size())
				return;
			}
			*/
		}
	private static void setStagePos(HWStage stage, Map<String, Double> axisPos)
		{
		boolean found=false;
		double[] da = new double[stage.getNumAxis()];
		for (int i = 0; i<stage.getNumAxis(); i++)
			{
			Double v = axisPos.get(stage.getAxisName()[i]);
			if (v!=null)
				{
				found=true;
				da[i] = v;
				}
			}
		if (found)
			stage.setStagePos(da);
		}
	
	

	public static void goToHome()
		{
		for (Map.Entry<EvDevicePath, HWStage> e : EvHardware.getDeviceMapCast(
				HWStage.class).entrySet())
			{
			HWStage stage = e.getValue();
			stage.goHome();
			}

		}

	
	
	

	public static void savePosList(File f) 
		{
		try
			{
			StoredStagePosition[] anArray = new StoredStagePosition[RecordingResource.posList.size()];
			for(int i=0;i<RecordingResource.posList.size();i++)
				anArray[i]=RecordingResource.posList.get(i);

			OutputStream file = new FileOutputStream(f);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			output.writeInt(1); //Version
			output.writeObject(anArray);
			output.close();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			throw new RuntimeException("Could not save positions: "+e.getMessage());
			}
		}

	
	
	
	
	
	public static void loadPosList(File f) throws IOException
		{
		StoredStagePosition[] anArray = null;

		try
			{
			InputStream file = new FileInputStream(f);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);

			/*int version = */input.readInt(); 
			anArray = (StoredStagePosition[]) input.readObject();
			input.close();
			
			RecordingResource.posList.clear();
			for (int i = 0; i<anArray.length; i++)
				RecordingResource.posList.add(anArray[i]);
			RecordingResource.posListUpdated();
			}
		catch (Exception e2)
			{
			e2.printStackTrace();
			throw new IOException("Could not read the file: "+e2.getMessage());
			}
		}
	
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()
		{
		}

	static
		{
		JInputManager.addGamepadMode("Hardware", new JInputModeRecording(), false);
		soundCameraSnap = new EvSound(RecordingResource.class, "camera_click.ogg");
		}

	public static void autofocus()
	{
	HWAutoFocus af=EvHardware.getCoreDevice().getCurrentAutofocus();
	if(af==null)
		throw new RuntimeException("No autofocus device found");
	else
		{
		try
			{
			af.fullFocus();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			throw new RuntimeException("Failed to focus");
			}
		}
	}

	}
