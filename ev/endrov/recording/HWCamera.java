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
public interface HWCamera extends EvDevice
	{
	public CameraImage snap();
	

	public void startSequenceAcq(/*Integer numImages, */double interval) throws Exception;
	public void stopSequenceAcq();
	public boolean isDoingSequenceAcq();
	public CameraImage snapSequence() throws Exception;
	//public int numSequenceLeft();
	public double getSequenceCapacityFree();
	
	}
