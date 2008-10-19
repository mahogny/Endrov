package endrov.micromanager;

import endrov.recording.HWCamera;
import endrov.recording.CameraImage;

/**
 * Micro-manager camera
 * @author Johan Henriksson
 *
 */
public class MMCamera extends MMDeviceAdapter implements HWCamera
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
