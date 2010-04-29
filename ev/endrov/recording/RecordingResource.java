/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;


import endrov.data.EvData;
import endrov.keyBinding.JInputManager;
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

	
	
	private static EvData data=new EvData();
	
	//TODO
	static
	{
	BoxROI roi=new BoxROI();
	roi.regionX.set(new EvDecimal(10), new EvDecimal(50));
	roi.regionY.set(new EvDecimal(10), new EvDecimal(50));
	data.addMetaObject(roi);

	}
	
	public static EvData getData()
		{
		return data;
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
