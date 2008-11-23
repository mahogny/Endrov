package endrov.recording.driver;

import java.util.*;

import org.jdom.Element;

import endrov.hardware.Hardware;
import endrov.hardware.HardwareManager;
import endrov.hardware.HardwareProvider;
import endrov.hardware.PropertyType;

public class EvNativeHardware extends HardwareProvider implements Hardware
	{
	private static Map<String, Class<? extends Hardware>> hardwareProvided=new TreeMap<String, Class<? extends Hardware>>();
	
	
	public static void initPlugin() {}
	static
		{
		HardwareManager.root.hw.put("ev",new EvNativeHardware());
		//TODO synchronize needed?
		
		hardwareProvided.put("OlympusIX", OlympusIX.class);
		}
	
	public EvNativeHardware()
		{
		OlympusIX core=new OlympusIX();
		
		hw.put("IX", core);
		
		
		}
		
	
	
	public Set<Hardware> autodetect()
		{
		return null;
		}
	public void getConfig(Element root)
		{
		}

	public List<String> provides()
		{
		return null;
		}
	public Hardware newProvided(String s)
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


	public boolean getPropertyValueBoolean(String prop)
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
