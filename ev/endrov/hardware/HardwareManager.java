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
public class HardwareManager
	{
	
	
	public static HardwareRoot root=new HardwareRoot();
	
	
	/**
	 * Get device or null if it does not exist
	 */
	public static Hardware getHardware(HardwarePath name)
		{
		Hardware next=root;
		for(String s:name.path)
			{
			if(next instanceof HardwareProvider)
				{
				next=((HardwareProvider)next).hw.get(s);
				}
			else
				return null;
			}
		return next;
		}
	

	/**
	 * Get list of all installed hardware
	 */
	public static Set<HardwarePath> getHardwareList()
		{
		return getHardwareMap().keySet();
		}
	
	
	
	/**
	 * Get map of all installed hardware
	 */
	public static TreeMap<HardwarePath,Hardware> getHardwareMap()
		{
		TreeMap<HardwarePath,Hardware> map=new TreeMap<HardwarePath,Hardware>();
		getHardwareMap(root, new LinkedList<String>(), map);
		return map;
		}
	private static void getHardwareMap(Hardware root, List<String> path,TreeMap<HardwarePath,Hardware> map)
		{
		if(root instanceof HardwareProvider)
			{
			HardwareProvider p=(HardwareProvider)root;
			for(Map.Entry<String, Hardware> e:p.hw.entrySet())
				{
				LinkedList<String> npath=new LinkedList<String>(path);
				npath.add(e.getKey());
				getHardwareMap(e.getValue(), npath, map);
				}
			}
		if(!(root instanceof HardwareRoot))
			map.put(new HardwarePath(path.toArray(new String[]{})), root);
		}


	
	/**
	 * Get list of all installed hardware of a specific type
	 */
	public static Map<HardwarePath,Hardware> getHardwareMap(Class<?> hw)
		{
		TreeMap<HardwarePath,Hardware> hwlist2=new TreeMap<HardwarePath,Hardware>();
		for(Map.Entry<HardwarePath, Hardware> hwe:getHardwareMap().entrySet())
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
