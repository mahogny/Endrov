/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverMicromanager;

import java.util.*;

import endrov.recording.CameraImage;

import mmcorej.*;

/**
 * Micro-manager utility functions
 * @author Johan Henriksson
 *
 */
public class MMutil
	{
	public static List<String> convVector(StrVector v)
		{
		ArrayList<String> al=new ArrayList<String>((int)v.size());
		for (int j=0; j<v.size(); j++)
			al.add(v.get(j));
		return al;
		}
	
	public static String convVector(CharVector v)
		{
		StringBuffer bf=new StringBuffer();
		for (int j=0; j<v.size(); j++)
			bf.append(v.get(j));
		return bf.toString();
		}
	
	public static CharVector convString(String s)
		{
		CharVector v=new CharVector();
		for(int i=0;i<s.length();i++)
			v.add(s.charAt(i));
		return v;
		}
	
	public static List<String> getLoadedDevices(CMMCore core)
		{
		return MMutil.convVector(core.getLoadedDevices());
		}
	
	public static SortedMap<String,String> getPropMap(CMMCore core, String device) throws Exception
		{
		SortedMap<String,String> map=new TreeMap<String, String>();
		for(String key:convVector(core.getDevicePropertyNames(device)))
			map.put(key,core.getProperty(device, key));
		return map;
		}
	
	public static CameraImage snap(CMMCore core) throws Exception
		{
		CameraImage im=new CameraImage();
		core.snapImage();
		im.w = (int)core.getImageWidth();
		im.h = (int)core.getImageHeight();
		im.bytesPerPixel=(int)core.getBytesPerPixel();
		im.pixels=core.getImage();
		return im;
		}
	
	}
