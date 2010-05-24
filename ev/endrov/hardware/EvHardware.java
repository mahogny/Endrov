/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;

import java.util.*;

import org.jdom.Element;


//TODO: when is hardware initiated? when is config read? requires that all drivers are loaded, by far not obvious.
//one dirty solution is to keep class name in config and use reflection.

/**
 * Manager of all existing hardware
 * 
 * @author Johan Henriksson
 *
 */
public class EvHardware
	{
	
	
	public static EvDeviceRoot root=new EvDeviceRoot();
	
	
	/**
	 * Get device or null if it does not exist
	 */
	public static EvDevice getDevice(EvDevicePath name)
		{
		EvDevice next=root;
		for(String s:name.path)
			{
			if(next instanceof EvDeviceProvider)
				{
				next=((EvDeviceProvider)next).hw.get(s);
				}
			else
				return null;
			}
		return next;
		}
	

	/**
	 * Get list of all installed hardware
	 */
	public static Set<EvDevicePath> getDeviceList()
		{
		return getDeviceMap().keySet();
		}
	
	
	
	/**
	 * Get map of all installed hardware
	 */
	public static TreeMap<EvDevicePath,EvDevice> getDeviceMap()
		{
		TreeMap<EvDevicePath,EvDevice> map=new TreeMap<EvDevicePath,EvDevice>();
		getDeviceMap(root, new LinkedList<String>(), map);
		return map;
		}
	private static void getDeviceMap(EvDevice root, List<String> path,TreeMap<EvDevicePath,EvDevice> map)
		{
		if(root instanceof EvDeviceProvider)
			{
			EvDeviceProvider p=(EvDeviceProvider)root;
			for(Map.Entry<String, EvDevice> e:p.hw.entrySet())
				{
				LinkedList<String> npath=new LinkedList<String>(path);
				npath.add(e.getKey());
				getDeviceMap(e.getValue(), npath, map);
				}
			}
		if(!(root instanceof EvDeviceRoot))
			map.put(new EvDevicePath(path.toArray(new String[]{})), root);
		}


	
	/**
	 * Get list of all installed hardware of a specific type
	 */
	public static Map<EvDevicePath,EvDevice> getDeviceMap(Class<?> hw)
		{
		TreeMap<EvDevicePath,EvDevice> hwlist2=new TreeMap<EvDevicePath,EvDevice>();
		for(Map.Entry<EvDevicePath, EvDevice> hwe:getDeviceMap().entrySet())
			{
			
			/*
			boolean is=false;
			for(Class<?> intf:hwe.getValue().getClass().getInterfaces())
				if(intf==hw)
					is=true;
			if(is)
				hwlist2.put(hwe.getKey(),hwe.getValue());*/
			if(hw.isInstance(hwe.getValue()))
				hwlist2.put(hwe.getKey(),hwe.getValue());
			}
		return hwlist2;
		}

	@SuppressWarnings("unchecked")
	public static <E> Map<EvDevicePath,E> getDeviceMapCast(Class<E> hw)
		{
		return (Map<EvDevicePath, E>) getDeviceMap(hw);
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
