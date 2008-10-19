package endrov.micromanager;

import endrov.recording.HWShutter;


/**
 * Micro-manager Shutter
 * 
 * is it really needed? it is a 2-state filter the way I see it
 * can reserve names open and closed
 * 
 * @author Johan Henriksson
 *
 */
public class MMShutter extends MMDeviceAdapter implements HWShutter
	{

	public MMShutter(MicroManager mm, String mmDeviceName)
		{
		super(mm,mmDeviceName);
		}

	
	
	
	}
