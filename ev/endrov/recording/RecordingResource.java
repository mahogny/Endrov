/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;


import endrov.data.EvData;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.keyBinding.JInputManager;
import endrov.roi.LineIterator;
import endrov.roi.ROI;
import endrov.roi.LineIterator.LineRange;
import endrov.roi.primitive.BoxROI;
import endrov.util.EvDecimal;
import endrov.util.EvSound;

/**
 * Resources associated with controlling microscope hardware 
 * @author Johan Henriksson
 *
 */
public class RecordingResource
	{
	public static EvSound soundCameraSnap;
	
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
	
	
	private static EvData data=new EvData();
	
	//TODO
	static
	{
	BoxROI roi=new BoxROI();
	roi.regionX.set(new EvDecimal(110), new EvDecimal(150));
	roi.regionY.set(new EvDecimal(110), new EvDecimal(150));
	data.addMetaObject(roi);

	}
	
	public static EvData getData()
		{
		return data;
		}

	
	/**
	 * Make a ROI mask for image scanners
	 */
	public static int[] makeScanningROI(HWImageScanner scanner, ROI roi, double stageX, double stageY)
		{
		int w=scanner.getWidth();
		int h=scanner.getHeight();
		
		int[] ret=new int[w*h];
		
		EvPixels p=new EvPixels(EvPixelsType.UBYTE, w, h); //TODO avoid this allocation 
		EvImage image=new EvImage(p);
		EvStack stack=new EvStack();

		
		//TODO invert resmag?
		
		stack.resX=getCurrentTotalMagnification(scanner);
		stack.resY=getCurrentTotalMagnification(scanner);

		System.out.println("res for scannerutil " + getCurrentTotalMagnification(scanner));
		
//		stack.resX=1.0/scanner.getResMagX();  //um/px //TODO seems wrong! wrong way!!!!!
//		stack.resY=1.0/scanner.getResMagY();
//		stack.resX=scanner.getResMagX();  //um/px //TODO seems wrong! wrong way!!!!!
//		stack.resY=scanner.getResMagY();
		stack.resZ=EvDecimal.ONE;
		
		String channel="foo";
		EvDecimal frame=EvDecimal.ZERO;
		EvDecimal z=EvDecimal.ZERO;
		
		//TODO how to offset?
		//there is offset!!!!
		
		//Fill bitmap ROI
		LineIterator it=roi.getLineIterator(stack, image, channel, frame, z);
		System.out.println("----roi start");
		while(it.next())
			{
			int y=it.y;
			for(LineRange r:it.ranges)
				for(int x=r.start;x<r.end;x++)  //< or <=?
					ret[y*w+x]=1;
			System.out.println("one line");
			}
		System.out.println("----roi end");
		
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



	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		JInputManager.addGamepadMode("Hardware", new JInputModeRecording(), false);
		soundCameraSnap=new EvSound(RecordingResource.class,"13658__LS__camera_click.wav");
		}

	}
