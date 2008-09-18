package endrov.recording.mm;

import endrov.recording.Camera;
import endrov.recording.CameraImage;

/**
 * Micro-manager camera
 * @author Johan Henriksson
 *
 */
public class MMCamera extends MMDeviceAdapter implements Camera
	{

	public MMCamera(MicroManager mm, String mmDeviceName)
		{
		super(mm,mmDeviceName);
		}

	public CameraImage snap()
		{
		try
			{
			return MMutil.snap(mm.core);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
		}
	
	
	
	
	}
