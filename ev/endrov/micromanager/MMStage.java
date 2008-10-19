package endrov.micromanager;

import endrov.recording.HWStage;


/**
 * Micro-manager Stage
 * @author Johan Henriksson
 *
 */
public class MMStage extends MMDeviceAdapter implements HWStage
	{

	public MMStage(MicroManager mm, String mmDeviceName)
		{
		super(mm,mmDeviceName);
		}

	
	
	
	}
