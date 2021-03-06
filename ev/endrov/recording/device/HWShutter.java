/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.device;

import endrov.hardware.EvDevice;


public interface HWShutter extends EvDevice
	{
	public void setOpen(boolean b);
	public boolean isOpen();
	}
