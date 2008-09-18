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
	public static List<HardwareProvider> provider=new Vector<HardwareProvider>();
	
	
	public static void registerHardwareProvider(HardwareProvider hp)
		{
		provider.add(hp);
		
		//Load config here
		
		}
	
	
	/**
	 * Get device or null if it does not exist
	 */
	public static Hardware getHardware(String name)
		{
		for(HardwareProvider p:provider)
			{
			Hardware h=p.hw.get(name);
			if(h!=null)
				return h;
			}
		return null;
		}

	/**
	 * Get list of all installed hardware
	 */
	public static Set<String> getHardwareList()
		{
		TreeSet<String> hwlist=new TreeSet<String>();
		for(HardwareProvider p:provider)
			hwlist.addAll(p.hw.keySet());
		return hwlist;
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
