/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
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
