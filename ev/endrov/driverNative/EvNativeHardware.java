package endrov.driverNative;

import java.util.*;

import org.jdom.Element;

import endrov.hardware.Device;
import endrov.hardware.EvHardware;
import endrov.hardware.DeviceProvider;
import endrov.hardware.PropertyType;

/**
 * Native device drivers
 * @author Johan Henriksson
 *
 */
public class EvNativeHardware extends DeviceProvider implements Device
	{
	private static Map<String, Class<? extends Device>> hardwareProvided=new TreeMap<String, Class<? extends Device>>();
	
	
	public static void initPlugin() {}
	static
		{
		EvHardware.root.hw.put("ev",new EvNativeHardware());
		
		//TODO synchronize needed?
		
		hardwareProvided.put("OlympusIX", OlympusIX.class);
		//hardwareProvided.put("ITKCorvus", ITKCorvus.class);
		hardwareProvided.put("Demo", DemoScope.class);
		}
	
	public EvNativeHardware()
		{
		//hw.put("IX", new OlympusIX());
		hw.put("demo", new DemoScope());
		
		//hw.put("ITK", new ITKCorvus());
		
		}
		
	
	
	public Set<Device> autodetect()
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
	public Device newProvided(String s)
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


	public SortedMap<String, PropertyType> getPropertyTypes()
		{
		return new TreeMap<String, PropertyType>();
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
	
	}
