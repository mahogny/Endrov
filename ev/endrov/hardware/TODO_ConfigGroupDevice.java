/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;

import java.util.*;

import endrov.recording.HWState;

/**
 * Virtual device that sets properties for other devices
 * @author Johan Henriksson
 *
 */
public class TODO_ConfigGroupDevice implements EvDevice, HWState
	{
	
	public int currentState=0;

	//Name -> Option
	public final List<Option> options=new ArrayList<Option>();
	
	/**
	 * One option group
	 * @author Johan Henriksson
	 *
	 */
	public static class Option
		{
		//Name of option
		public String name;
		
		//Device -> Property -> Value
		public Map<EvDevicePath,Map<String,String>> settings=new TreeMap<EvDevicePath, Map<String,String>>();
		}

	public String getDescName()
		{
		return "ConfigGroup";
		}

	public SortedMap<String, String> getPropertyMap()
		{
		return new TreeMap<String,String>();
		}

	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		return new TreeMap<String,DevicePropertyType>();
		}

	public String getPropertyValue(String prop)
		{
		return null;
		}

	public Boolean getPropertyValueBoolean(String prop)
		{
		return null;
		}

	public void setPropertyValue(String prop, String value)
		{
		}

	public void setPropertyValue(String prop, boolean value)
		{
		}

	
	
	public int getCurrentState()
		{
		return currentState;
		}

	public String getCurrentStateLabel()
		{
		Option o=options.get(currentState);
		if(o!=null)
			return o.name;
		else
			return "";
		}

	public List<String> getStateNames()
		{
		LinkedList<String> names=new LinkedList<String>();
		for(Option o:options)
			names.add(o.name);
		return names;
		}

	
	
	
	public void setCurrentState(int state)
		{
		Option op=options.get(state);
		if(op==null)
			System.out.println("Could not find state #"+state);
		else
			{
			//TODO
			}
		}

	public void setCurrentStateLabel(String label)
		{
		for(int i=0;i<options.size();i++)
			{
			if(options.get(i).equals(label))
				{
				setCurrentState(i);
				return;
				}
			}
		System.out.println("Could not find state "+label);
		}

	public double getResMagX()
		{
		return 1;
		}

	public double getResMagY()
		{
		return 1;
		}

	public boolean hasConfigureDialog(){return false;}
	public void openConfigureDialog(){}


	}
