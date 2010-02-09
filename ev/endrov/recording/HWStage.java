/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;

import endrov.hardware.Device;


/**
 * Microscope moving stage
 * @author Johan Henriksson
 *
 */
public interface HWStage extends Device
	{

	public int getNumAxis();
	
	public String[] getAxisName();

	public double[] getStagePos();
	
	public void setStagePos(double axis[]);
	
	public void setRelStagePos(double axis[]);
	
	public void goHome();
	}
