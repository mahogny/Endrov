/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.device;

import java.io.IOException;

import endrov.hardware.EvDevice;

/**
 * 
 * 
 * @author Johan Henriksson
 *
 */
public interface HWAutoFocus extends EvDevice
	{
	public double getLastFocusScore();
	public double getCurrentFocusScore();
	public void fullFocus() throws IOException;
	public void incrementalFocus() throws IOException; //Another exception?
	public void setAutoFocusOffset (double offset);// throw (CMMError)
	public double getAutoFocusOffset ();// throw (CMMError)

	
	public void enableContinuousFocus(boolean enable);
	public boolean isContinuousFocusEnabled();
	public boolean isContinuousFocusLocked();
	}
