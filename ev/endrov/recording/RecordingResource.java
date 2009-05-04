package endrov.recording;


import endrov.util.EvSound;

public class RecordingResource
	{
	public static EvSound soundCameraSnap=new EvSound(RecordingResource.class,"13658__LS__camera_click.wav");
	
	//This forces loading of static values to be done at startup
	public static void initPlugin() {}
	static
		{
		
		}
	}
