/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.device;

import endrov.hardware.*;
import endrov.recording.CameraImage;
import endrov.util.EvDecimal;

/**
 * Hardware with recording capabilities
 * @author Johan Henriksson
 */
public interface HWCamera extends EvDevice
	{
	public CameraImage snap();
	

	public void startSequenceAcq(double interval) throws Exception;
	public void stopSequenceAcq();
	public boolean isDoingSequenceAcq();
	public CameraImage snapSequence() throws Exception;
	public EvDecimal getActualSequenceInterval();
	
	public double getSequenceBufferUsed();
	
	public int getCamHeight();
	public int getCamWidth();
	
	
	}
