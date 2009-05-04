package endrov.recording;

import endrov.hardware.*;

/**
 * Hardware with recording capabilities
 * @author Johan Henriksson
 */
public interface HWCamera extends Device
	{
	public CameraImage snap();
	}
