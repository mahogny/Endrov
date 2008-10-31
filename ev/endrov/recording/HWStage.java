package endrov.recording;

import endrov.hardware.Hardware;


/**
 * Microscope moving stage
 * @author Johan Henriksson
 *
 */
public interface HWStage extends Hardware
	{

	public int getNumAxis();
	
	public String[] getAxisName();

	public double[] getStagePos();
	
	public void setStagePos(double axis[]);
	
	public void setRelStagePos(double axis[]);
	}
