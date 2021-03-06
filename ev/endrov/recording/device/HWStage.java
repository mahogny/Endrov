/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.device;

import endrov.hardware.EvDevice;


/**
 * Microscope moving stage
 * @author Johan Henriksson
 *
 */
public interface HWStage extends EvDevice
	{

	public int getNumAxis();
	
	public String[] getAxisName();

	public double[] getStagePos();
	
	public void setStagePos(double axis[]);
	
	public void setRelStagePos(double axis[]);
	
	public void goHome();
	
	/**
	 * Stop current movement
	 */
	public void stop();
	
	public boolean hasSampleLoadPosition();
	public void setSampleLoadPosition(boolean b);
	public boolean getSampleLoadPosition();
	}
