/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareFrivolous.devices;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Element;

import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDeviceObserver;
import endrov.hardware.EvDeviceProvider;
import endrov.hardware.EvHardware;
import endrov.hardwareFrivolous.FrivolousModel;

/**
 * Device provider for Frivolous virtual microscope
 * 
 * @author David Johansson, Arvid Johansson, Johan Henriksson
 */
public class FrivolousDeviceProvider extends EvDeviceProvider implements EvDevice
	{
	private Map<String, Class<? extends EvDevice>> hardwareProvided = new TreeMap<String, Class<? extends EvDevice>>();
	public EvDeviceObserver event=new EvDeviceObserver();
	
	
	double resolution=1;
	int height=512;
	int width=512;
	FrivolousModel model;
	public double[] stagePos = new double[]{ 0, 0, 0 };

		


	public FrivolousDeviceProvider()
		{
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
		return Arrays.asList("frivolous");
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
		return "Frivolous";
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
		new FrivolousConfigWindow(this);
		}

	


	
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

	
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	static
		{
		EvHardware.getRoot().hw.put("fr", new FrivolousDeviceProvider());
		EvHardware.updateAvailableDevices();
		}
	public static void initPlugin()
		{
		}
	
	}
