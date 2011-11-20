package endrov.driverNative;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDeviceObserver.DeviceListener;
import endrov.recording.HWTrigger;

/**
 * Device: Software triggerer based on unix pipes
 * 
 * @author Johan Henriksson
 *
 */
public class DevicePipeTrigger implements HWTrigger
	{

	private List<TriggerListener> triggerListeners=new LinkedList<TriggerListener>();
	private List<DeviceListener> deviceListeners=new LinkedList<DeviceListener>();
	
	
	private String pipepath="";
	
	public void addTriggerListener(TriggerListener listener)
		{
		triggerListeners.add(listener);
		}

	public void removeTriggerListener(TriggerListener listener)
		{
		triggerListeners.remove(listener);
		}

	
	
	
	public void addDeviceListener(DeviceListener listener)
		{
		deviceListeners.add(listener);
		}
	

	public void removeDeviceListener(DeviceListener listener)
		{
		deviceListeners.remove(listener);
		}
	

	public String getDescName()
		{
		return "Pipe triggerer";
		}

	public SortedMap<String, String> getPropertyMap()
		{
		SortedMap<String,String> properties=new TreeMap<String, String>();
		properties.put("path",pipepath);
		return properties;
		}

	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		SortedMap<String, DevicePropertyType> types=new TreeMap<String, DevicePropertyType>();
		types.put("path", DevicePropertyType.getEditableStringState());
		return types;
		}

	public String getPropertyValue(String prop)
		{
		if(prop.equals("path"))
			return pipepath;
		else
			return null;
		}

	public Boolean getPropertyValueBoolean(String prop)
		{
		return null;
		}

	public boolean hasConfigureDialog()
		{
		return false;
		}

	public void openConfigureDialog()
		{
		}


	public void setPropertyValue(String prop, String value)
		{
		if(prop.equals("path"))
			pipepath=value;
		}

	public void setPropertyValue(String prop, boolean value)
		{
		}

	}
