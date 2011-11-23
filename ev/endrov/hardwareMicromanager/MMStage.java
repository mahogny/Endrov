/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareMicromanager;

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
			if(isXY)
				mm.core.setXYPosition(mmDeviceName, axis[0], axis[1]);
			else
				mm.core.setPosition(mmDeviceName, axis[0]);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	public void setRelStagePos(double axis[])
		{
		try
			{
			if(isXY)
				mm.core.setRelativeXYPosition(mmDeviceName, axis[0], axis[1]);
			else
				mm.core.setRelativePosition(mmDeviceName, axis[0]);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}

		}
	
	public void goHome()
		{
		try
			{
			mm.core.home(mmDeviceName);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	
	public boolean hasSampleLoadPosition()
		{
		try
			{
			return MMutil.convVector(mm.core.getDevicePropertyNames(mmDeviceName)).contains("loadSample");
			}
		catch (Exception e)
			{
			return false;
			}
		}
	
	
	public void setSampleLoadPosition(boolean b)
		{
		setPropertyValue("loadSample", b ? "1":"0");
		}
	
	public boolean getSampleLoadPosition()
		{
		return getPropertyValue("loadSample").equals("1");
		};

	public void stop()
		{
		try
			{
			mm.core.stop(mmDeviceName);
			}
		catch (Exception e)
			{
			}
		}

	
	//Not in interface yet
	/*
	public void setOrigin()
		{
		mm.core.setOriginXY(mmDeviceName);
		}
	
	public void stop()
		{
		mm.core.stop(mmDeviceName);
		}
	*/
	
	}
