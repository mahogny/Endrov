package endrov.recording;

import endrov.hardware.*;

/**
 * Hardware with recording capabilities
 * @author Johan Henriksson
 */
public interface HWCamera extends Device, HWMagnifier
	{
	public CameraImage snap();
	}
