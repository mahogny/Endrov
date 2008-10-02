package endrov.recording.mm;

import endrov.recording.HWMagnifier;


/**
 * Micro-manager Magnifier
 * @author Johan Henriksson
 *
 */
public class MMMagnifier extends MMDeviceAdapter implements HWMagnifier
	{

	public MMMagnifier(MicroManager mm, String mmDeviceName)
		{
		super(mm,mmDeviceName);
		}

	
	
	
	}
