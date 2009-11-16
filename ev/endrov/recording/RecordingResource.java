package endrov.recording;


import endrov.keyBinding.JInputManager;
import endrov.util.EvSound;

/**
 * Resources associated with controlling microscope hardware 
 * @author Johan Henriksson
 *
 */
public class RecordingResource
	{
	public static EvSound soundCameraSnap;
	
	//This forces loading of static values to be done at startup
	public static void initPlugin() {}
	static
		{
		JInputManager.addGamepadMode("Hardware", new JInputModeRecording(), false);
		soundCameraSnap=new EvSound(RecordingResource.class,"13658__LS__camera_click.wav");
		}
	
	/**
	 * TODO
	 * guess magnification by looking at state label
	 */
	public static double magFromLabel(String s)
		{
		return 1;
		}
	
	}
