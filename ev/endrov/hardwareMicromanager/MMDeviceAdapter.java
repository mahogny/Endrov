/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareMicromanager;

import java.util.*;

import endrov.hardware.*;

//could preload list of properties

//mm virtual property: state. map to setstate


/**
 * Micro manager generic device adapter, mapped to Endrov hardware
 * @author Johan Henriksson
 *
 */
public class MMDeviceAdapter implements EvDevice
	{
	protected MicroManager mm;
	protected String mmDeviceName;

		
	public MMDeviceAdapter(MicroManager mm, String mmDeviceName)
		{
		this.mm=mm;
		this.mmDeviceName=mmDeviceName;
		}
	
	public String getDescName()
		{
		try
			{
			if(mmDeviceName.equals("Core"))
				return "uManager virtual device";
			else
				return mm.core.getProperty(mmDeviceName, "Description");
			}
		catch (Exception e)
			{
			return "<mm exception>";
			}
		}

	
	public SortedMap<String, String> getPropertyMap()
		{
		try
			{
			return MMutil.getPropMap(mm.core,mmDeviceName);
			}
		catch (Exception e)
			{
			return new TreeMap<String, String>();
			}
		}

	
	
	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		TreeMap<String, DevicePropertyType> map=new TreeMap<String, DevicePropertyType>();
		try
			{
			for(String propName:MMutil.convVector(mm.core.getDevicePropertyNames(mmDeviceName)))
				{
				DevicePropertyType p=new DevicePropertyType();
				List<String> allowedValues=MMutil.convVector(mm.core.getAllowedPropertyValues(mmDeviceName, propName));
				for(int i=0;i<allowedValues.size();i++)
					p.categories.add(allowedValues.get(i));
				
				p.readOnly=mm.core.isPropertyReadOnly(mmDeviceName, propName);
				
				p.hasRange=mm.core.hasPropertyLimits(mmDeviceName, propName);
				p.rangeLower=mm.core.getPropertyLowerLimit(mmDeviceName, propName);
				p.rangeUpper=mm.core.getPropertyUpperLimit(mmDeviceName, propName);

				if(p.categories.size()==2 && p.categories.contains("0") && p.categories.contains("1"))
					p.isBoolean=true;
				
				map.put(propName,p);
				}
			
			return map;
			}
		catch (Exception e)
			{
			return new TreeMap<String, DevicePropertyType>();
			}
		}

	
	
	public String getPropertyValue(String prop)
		{
		try
			{
			return mm.core.getProperty(mmDeviceName, prop);
			}
		catch (Exception e)
			{
			return "<mm exception>";
			}
		}
	
	public Boolean getPropertyValueBoolean(String prop)
		{
		return getPropertyValue(prop).equals("1");
		}
	
	public void setPropertyValue(String prop, String value)
		{
		try
			{
			mm.core.setProperty(mmDeviceName, prop, value);
			event.emit(null, this);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	public void setPropertyValue(String prop, boolean value)
		{
		setPropertyValue(prop, value ? "1" : "0");
		}

	
	public boolean hasConfigureDialog()
		{
		return false;
		}
	
	public void openConfigureDialog(){}

	
	public EvDeviceObserver event=new EvDeviceObserver();
	public void addDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.addWeakListener(listener);
		}
	public void removeDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.remove(listener);
		}

	}
