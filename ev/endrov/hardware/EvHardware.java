package endrov.hardware;

import java.util.*;
import org.jdom.Element;


//TODO: when is hardware initiated? when is config read? requires that all drivers are loaded, by far not obvious.
//one dirty solution is to keep class name in config and use reflection.

/**
 * Manager of all existing hardware.  
 * @author Johan Henriksson
 *
 */
public class EvHardware
	{
	
	
	public static DeviceRoot root=new DeviceRoot();
	
	
	/**
	 * Get device or null if it does not exist
	 */
	public static Device getDevice(DevicePath name)
		{
		Device next=root;
		for(String s:name.path)
			{
			if(next instanceof DeviceProvider)
				{
				next=((DeviceProvider)next).hw.get(s);
				}
			else
				return null;
			}
		return next;
		}
	

	/**
	 * Get list of all installed hardware
	 */
	public static Set<DevicePath> getDeviceList()
		{
		return getDeviceMap().keySet();
		}
	
	
	
	/**
	 * Get map of all installed hardware
	 */
	public static TreeMap<DevicePath,Device> getDeviceMap()
		{
		TreeMap<DevicePath,Device> map=new TreeMap<DevicePath,Device>();
		getDeviceMap(root, new LinkedList<String>(), map);
		return map;
		}
	private static void getDeviceMap(Device root, List<String> path,TreeMap<DevicePath,Device> map)
		{
		if(root instanceof DeviceProvider)
			{
			DeviceProvider p=(DeviceProvider)root;
			for(Map.Entry<String, Device> e:p.hw.entrySet())
				{
				LinkedList<String> npath=new LinkedList<String>(path);
				npath.add(e.getKey());
				getDeviceMap(e.getValue(), npath, map);
				}
			}
		if(!(root instanceof DeviceRoot))
			map.put(new DevicePath(path.toArray(new String[]{})), root);
		}


	
	/**
	 * Get list of all installed hardware of a specific type
	 */
	public static Map<DevicePath,Device> getDeviceMap(Class<?> hw)
		{
		TreeMap<DevicePath,Device> hwlist2=new TreeMap<DevicePath,Device>();
		for(Map.Entry<DevicePath, Device> hwe:getDeviceMap().entrySet())
			{
			boolean is=false;
			for(Class<?> intf:hwe.getValue().getClass().getInterfaces())
				if(intf==hw)
					is=true;
			if(is)
				hwlist2.put(hwe.getKey(),hwe.getValue());
			}
		return hwlist2;
		}

	
	/**
	 * Get current configuration as XML
	 */
	public Element getConfig()
		{
		Element root=new Element("hwconf");
		return root;
		}

	/**
	 * Set current config
	 */
	public void setConfig(Element root)
		{
		}
	
	//
	
	
	
	}
