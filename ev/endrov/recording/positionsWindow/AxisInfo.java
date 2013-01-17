package endrov.recording.positionsWindow;

import endrov.recording.device.HWStage;

/**
 * Stores axis information
 * 
 * @author Kim Nordlöf, Erik Vernersson
 */

public class AxisInfo
	{
	private HWStage device;
	private int axis;
	private double value;

	public AxisInfo(HWStage stage, int axis, double value)
		{
		this.device = stage;
		this.axis = axis;
		this.value = value;
		}

	public HWStage getDevice()
		{
		return device;
		}

	public int getAxis()
		{
		return axis;
		}

	public double getValue()
		{
		return value;
		}

	public String toString()
		{
		return device.getAxisName()[axis]+":"+(int) value;
		}

	}
