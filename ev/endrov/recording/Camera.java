package endrov.recording;

import endrov.hardware.*;

/**
 * Hardware with recording capabilities
 * @author Johan Henriksson
 */
public interface Camera extends Hardware
	{
	public CameraImage snap();
	}
