package endrov.hardwareFrivolous.devices;

import java.util.SortedMap;
import java.util.TreeMap;

import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDeviceObserver;
import endrov.recording.device.HWStage;


// TODO Simulate moving stage? takes time to move? 


/**
 * Frivolous XY stage
 * 
 * @author Johan Henriksson, David Johansson, Arvid Johansson
 *
 *
 * TODO: kimno wants to flip XY coordinates to have them correspond to um
 *
 */
class FrivolousXYStage implements HWStage
	{
	
	private FrivolousDeviceProvider frivolous;
	
	public FrivolousXYStage(FrivolousDeviceProvider frivolous)
		{
		this.frivolous=frivolous;
		}
	
	public String[] getAxisName()
		{
		return new String[]{ "X", "Y" };
		}
	
	public int getNumAxis()
		{
		return 2;
		}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public double[] getStagePos()
		{
		return new double[]{ 
				frivolous.stagePos[0], 
				frivolous.stagePos[1] };
		}
	
	public void setRelStagePos(double[] axis)
		{
		setStagePos(new double[]{
				frivolous.stagePos[0] + axis[0],
				frivolous.stagePos[1] + axis[1]});
		}
	
	public void setStagePos(double[] axis)
		{
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
	
		
		for (int i = 0; i<2; i++)
			frivolous.stagePos[i] = axis[i];
		}
	
	public void goHome()
		{
		for (int i = 0; i<2; i++)
			frivolous.stagePos[i] = 0;
		}
	
	public String getDescName()
		{
		return "Frivolous XY stage";
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
