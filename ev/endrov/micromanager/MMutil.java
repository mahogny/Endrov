package endrov.micromanager;

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
