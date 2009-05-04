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
