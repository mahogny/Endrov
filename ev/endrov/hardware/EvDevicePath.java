/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;

import java.util.*;

/**
 * Path to hardware. Internally a list of strings, for the user seen as a /-separated string
 * @author Johan Henriksson
 */
public class EvDevicePath implements Comparable<EvDevicePath>
	{
	public String[] path;
	
	/**
	 * Construct from string
	 */
	public EvDevicePath(String dotPath)
		{
		List<String> n=new LinkedList<String>();
		StringTokenizer stok=new StringTokenizer(dotPath,"/");
		while(stok.hasMoreElements())
			n.add(stok.nextToken());
		path=n.toArray(new String[]{});
		}

	/**
	 * Construct from raw path
	 */
	public EvDevicePath(String[] path)
		{
		this.path=path;
		}
	
	/**
	 * Make dot-representation
	 */
	public String toString()
		{
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<path.length;i++)
			{
			sb.append(path[i]);
			if(i!=path.length-1)
				sb.append("/");
			}
		return sb.toString();
		}

	/**
	 * Ordering of paths
	 */
	public int compareTo(EvDevicePath o)
		{
		int checkLength=path.length;
		int defret=-1;
		if(o.path.length<checkLength)
			{
			checkLength=o.path.length;
			defret=1;
			}
		else if(path.length==o.path.length)
			defret=0;
		for(int i=0;i<checkLength;i++)
			{
			int cmp=path[i].compareTo(o.path[i]);
			if(cmp!=0)
				return cmp;
			}
		return defret;
		}
	
	@Override
	public boolean equals(Object obj)
		{
		if(obj instanceof EvDevicePath)
			return compareTo((EvDevicePath)obj)==0;
		else
			return false;
		}
	
	/**
	 * Get device for this path
	 */
	public EvDevice getDevice()
		{
		return EvHardware.getDevice(this);
		}

	@Override
	public int hashCode()
		{
		int h=0;
		for(String s:path)
			h^=s.hashCode();
		return h;
		}
	
	/**
	 * testing
	 */
	public static void main(String[] arg)
		{
		EvDevicePath a=new EvDevicePath(new String[]{"a","c"});
		EvDevicePath b=new EvDevicePath(new String[]{"a"});
		System.out.println(a.compareTo(b));
		System.out.println(a);
		}
	
	
	public String getLeafName()
		{
		return path[path.length-1];
		}
	
	
	}
