/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;

import java.util.*;

/**
 * Hardware device property type
 * @author Johan Henriksson
 */
public class DevicePropertyType
	{
	public boolean readOnly=false;
	public boolean isBoolean=false;
	public boolean isString=false;
	public TreeSet<String> categories=new TreeSet<String>();
	
	public boolean hasRange=false;
	public double rangeLower, rangeUpper;
	
	
	
	
	public static DevicePropertyType getEditableBooleanState()
		{
		DevicePropertyType p=new DevicePropertyType();
		p.categories.add("0");
		p.categories.add("1");
		p.isBoolean=true;
		return p;
		}

	
	public static DevicePropertyType getEditableStringState()
		{
		DevicePropertyType p=new DevicePropertyType();
		p.isString=true;
		return p;
		}
	
	public static DevicePropertyType getEditableIntState(int min,int max)
		{
		DevicePropertyType p=new DevicePropertyType();
		for(int i=min;i<=max;i++)
			p.categories.add(""+i);
		return p;
		}

	public static DevicePropertyType getEditableIntState(int[] state)
		{
		DevicePropertyType p=new DevicePropertyType();
		for(int s:state)
			p.categories.add(""+s);
		return p;
		}

	public static DevicePropertyType getEditableCategoryState(String[] stateName)
		{
		DevicePropertyType p=new DevicePropertyType();
		p.categories.addAll(Arrays.asList(stateName));
		return p;
		}

	
	
	}
