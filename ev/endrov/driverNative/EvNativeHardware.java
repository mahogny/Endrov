/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverNative;

import java.util.*;

import org.jdom.Element;

import endrov.hardware.EvDevice;
import endrov.hardware.EvDeviceObserver;
import endrov.hardware.EvHardware;
import endrov.hardware.EvDeviceProvider;
import endrov.hardware.DevicePropertyType;

/**
 * Native device drivers
 * @author Johan Henriksson
 *
 */
public class EvNativeHardware extends EvDeviceProvider implements EvDevice
	{
	private static Map<String, Class<? extends EvDevice>> hardwareProvided=new TreeMap<String, Class<? extends EvDevice>>();
	
	
	public EvNativeHardware()
		{
		//hw.put("IX", new OlympusIX());
		
		//hw.put("ITK", new ITKCorvus());
		hardwareProvided.put("OlympusIX", OlympusIX.class);
		//hardwareProvided.put("ITKCorvus", ITKCorvus.class);
		hardwareProvided.put("Demo", DemoScope.class);
		
		}
		
	
	
	public Set<EvDevice> autodetect()
		{
		return null;
		}
	public void getConfig(Element root)
		{
		}

	public List<String> provides()
		{
		return Arrays.asList("IXvirtual");
		}
	public EvDevice newProvided(String s)
		{
		try
			{
			return hardwareProvided.get(s).newInstance();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
		}

	public void setConfig(Element root)
		{
		
		}

	public String getDescName()
		{
		return "Endrov native hardware";
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
		return false;
		}


	public void setPropertyValue(String prop, boolean value)
		{
		}


	public void setPropertyValue(String prop, String value)
		{
		}
	
	
	public boolean hasConfigureDialog()
		{
		return true;
		}
	public void openConfigureDialog()
		{
		hw.put("demo", new DemoScope());
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

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvHardware.root.hw.put("ev",new EvNativeHardware());
		
		//TODO synchronize needed?
		
		}

	}
