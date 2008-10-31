package endrov.micromanager;

import endrov.recording.HWStage;


/**
 * Micro-manager Stage
 * @author Johan Henriksson
 *
 */
public class MMStage extends MMDeviceAdapter implements HWStage
	{
	private boolean isXY;
	
	public MMStage(MicroManager mm, String mmDeviceName, boolean isXY)
		{
		super(mm,mmDeviceName);
		this.isXY=isXY;
		}

	public int getNumAxis()
		{
		return isXY ? 2 : 1;
		}
	
	public String[] getAxisName()
		{
		if(isXY)
			return new String[]{"X","Y"};
		else
			return new String[]{"Z"};
		}
	
	public double[] getStagePos()
		{
		try
			{
			if(isXY)
				{
				double x=mm.core.getXPosition(mmDeviceName);
				double y=mm.core.getYPosition(mmDeviceName);
				return new double[]{x,y};
				}
			else
				{
				double z=mm.core.getPosition(mmDeviceName);
				return new double[]{z};
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
		
		
		}
	
	public void setStagePos(double axis[])
		{
		try
			{
			mm.core.setXYPosition(mmDeviceName, axis[0], axis[1]);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	}
