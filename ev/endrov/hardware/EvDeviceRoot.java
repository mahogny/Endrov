/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.jdom.Element;

/**
 * The special device "root". Doesn't do anything, just there to form a complete tree
 * @author Johan Henriksson
 *
 */
public class EvDeviceRoot extends EvDeviceProvider implements EvDevice
	{

	public Set<EvDevice> autodetect(){return null;}

	public void getConfig(Element root)
		{
		}


	public List<String> provides(){return null;}
	public EvDevice newProvided(String s){return null;}

	public void setConfig(Element root)
		{
		}

	public String getDescName()
		{
		return "root";
		}

	public SortedMap<String, String> getPropertyMap()
		{
		return null;
		}

	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		return null;
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
	
	
	public boolean hasConfigureDialog(){return false;}
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
