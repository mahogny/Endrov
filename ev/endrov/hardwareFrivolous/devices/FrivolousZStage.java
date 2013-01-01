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
 * bug: I think kimno reverted the axis here, in a very bad way. verify relative movement!
 * 
 * TODO flip XY axis in frivolous code instead
 *
 */
class FrivolousZStage implements HWStage
	{
	
	private FrivolousDeviceProvider frivolous;
	
	public FrivolousZStage(FrivolousDeviceProvider frivolous)
		{
		this.frivolous=frivolous;
		}
	
	public String[] getAxisName()
		{
		return new String[]{"Z" };
		}
	
	public int getNumAxis()
		{
		return 1;
		}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public double[] getStagePos()
		{
		return new double[]{ frivolous.stagePos[2] };
		}
	
	public void setRelStagePos(double[] axis)
		{
		setStagePos(new double[]{
				frivolous.stagePos[2] + axis[0]
				});
		}
	
	public void setStagePos(double[] axis)
		{
		double newZ=axis[0];
		double oldZ = frivolous.stagePos[2];
		if (newZ<-10000)
			newZ=-10000;
		else if(newZ>10000)
			newZ=10000;
		
		frivolous.stagePos[2] = newZ;
		frivolous.model.getSettings().offsetZ = newZ;
		
		if(newZ!=oldZ)
			frivolous.model.updatePSF();
		}
	
	public void goHome()
		{
		frivolous.stagePos[2] = 0;
		}
	
	public String getDescName()
		{
		return "Frivolous Z stage";
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
