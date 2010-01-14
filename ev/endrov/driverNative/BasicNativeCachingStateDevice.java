/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverNative;

import java.util.*;

import endrov.hardware.PropertyType;
import endrov.recording.HWState;

/**
 * Device mapping state to property "state". State will be cached.
 * @author Johan Henriksson
 *
 */
public abstract class BasicNativeCachingStateDevice implements HWState
	{
	private int cachedState;

	private PropertyType stateProp;
	private TreeMap<Integer, String> stateName=new TreeMap<Integer, String>();

	public BasicNativeCachingStateDevice()
		{
		PropertyType.getEditableBooleanState();
		cachedState=getCurrentStateHW();
		}
	
	public BasicNativeCachingStateDevice(int min,int max)
		{
		stateProp=PropertyType.getEditableIntState(min, max);
		for(int i=0;i<=max;i++)
			stateName.put(i,""+i);
		cachedState=getCurrentStateHW();
		}

	public BasicNativeCachingStateDevice(int[] state, String[] stateLabel)
		{
		stateProp=PropertyType.getEditableIntState(state);
		for(int i=0;i<state.length;i++)
			stateName.put(state[i],stateLabel[i]);
		cachedState=getCurrentStateHW();
		}

	public List<String> getStateNames()
		{
		List<String> list=new LinkedList<String>();
		list.addAll(stateName.values());
		return list;
		}

	public abstract int getCurrentStateHW();
	public abstract void setCurrentStateHW(int state);
		
	
	public int getCurrentState()
		{
		return cachedState;
		}
	public void setCurrentState(int state)
		{
		setCurrentStateHW(state);
		cachedState=state;
		}
	public String getCurrentStateLabel()
		{
		return stateName.get(cachedState);
		}
	public void setCurrentStateLabel(String label)
		{
		for(Map.Entry<Integer, String> e:stateName.entrySet())
			if(e.getValue().equals(label))
				setCurrentState(e.getKey());
		}


	
	public SortedMap<String, String> getPropertyMap()
		{
		TreeMap<String, String> m=new TreeMap<String, String>();
		m.put("state", ""+getCurrentState());
		return m;
		}


	public String getPropertyValue(String prop)
		{
		if(prop.equals("state"))
			return ""+getCurrentState();
		return null;
		}

	
	public Boolean getPropertyValueBoolean(String prop)
		{
		if(prop.equals("state"))
			return getCurrentState()!=0;
		else
			return false;
		}

	public void setPropertyValue(String prop, boolean value)
		{
		if(prop.equals("state"))
			setCurrentState(value ? 1 : 0);
		}

	public void setPropertyValue(String prop, String value)
		{
		if(prop.equals("state"))
			setCurrentState(Integer.parseInt(value));
		}

	public SortedMap<String, PropertyType> getPropertyTypes()
		{
		TreeMap<String, PropertyType> m=new TreeMap<String, PropertyType>();
		m.put("state", stateProp);
		return m;
		}
	
	
	
	}
