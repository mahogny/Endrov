package endrov.hardwareFrivolous.devices;

import java.util.SortedMap;
import java.util.TreeMap;

import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDeviceObserver;
import endrov.recording.device.HWStage;


// TODO Simulate moving stage? takes time to move? 


/**
 * Frivolous XYZ stage
 * 
 * @author Johan Henriksson, David Johansson, Arvid Johansson
 *
 */
class FrivolousStage implements HWStage
	{
	
	private FrivolousDeviceProvider frivolous;
	
	public FrivolousStage(FrivolousDeviceProvider frivolous)
		{
		this.frivolous=frivolous;
		}
	
	public String[] getAxisName()
		{
		return new String[]{ "X", "Y", "Z" };
		}
	
	public int getNumAxis()
		{
		return 3;
		}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public double[] getStagePos()
		{
		return new double[]{ -frivolous.stagePos[0], -frivolous.stagePos[1], -frivolous.stagePos[2] };
		}
	
	public void setRelStagePos(double[] axis)
		{
		double[] tmp = frivolous.stagePos.clone();
		for (int i = 0; i<3; i++)
			tmp[i] += axis[i];
		setStagePos(tmp);
		}
	
	public void setStagePos(double[] axis)
		{
		double oldZ = frivolous.stagePos[2];
		if (axis[2]<-10000)
			axis[2]=-10000;
		else if(axis[2]>10000)
			axis[2]=10000;
		
		//TODO connect to magnification
		/*
		for (int i = 0; i<2; i++)
			if (axis[i]<-512*.1)
				axis[i]=-512*.1;
			else if (axis[i]>512*.1)
				axis[i]=512*.1;
				*/
		
		for (int i = 0; i<2; i++)
			if (axis[i]<-512*frivolous.resolution)
				axis[i]=-512*frivolous.resolution;
			else if (axis[i]>512*frivolous.resolution)
				axis[i]=512*frivolous.resolution;	
	
		
		for (int i = 0; i<3; i++)
			frivolous.stagePos[i] = -axis[i];
	
		frivolous.model.getSettings().offsetZ = frivolous.stagePos[2];
		if(frivolous.stagePos[2]!=oldZ)
			frivolous.model.updatePSF();
		}
	
	public void goHome()
		{
		for (int i = 0; i<3; i++)
			frivolous.stagePos[i] = 0;
		}
	
	public String getDescName()
		{
		return "Frivolous stage";
		}
	
	public SortedMap<String, String> getPropertyMap()
		{
		return new TreeMap<String, String>();
		}
	
	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		return new TreeMap<String, DevicePropertyType>();
		}
	
	public String getPropertyValue(String prop)
		{
		return null;
		}
	
	public Boolean getPropertyValueBoolean(String prop)
		{
		return null;
		}
	
	public void setPropertyValue(String prop, boolean value)
		{
		}
	
	public void setPropertyValue(String prop, String value)
		{
		}
	
	public boolean hasConfigureDialog()
		{
		return false;
		}
	
	public void openConfigureDialog()
		{
		}
	
	
	public EvDeviceObserver event=new EvDeviceObserver();
	public void addDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.addWeakListener(listener);
		}
	public void removeDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.remove(listener);
		}
	
	public boolean hasSampleLoadPosition(){return false;}
	public void setSampleLoadPosition(boolean b){}
	public boolean getSampleLoadPosition(){return false;};
	
	public void stop()
		{
		}
	
	}
