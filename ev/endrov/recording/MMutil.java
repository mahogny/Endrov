package endrov.recording;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public static Map<String,String> getPropMap(CMMCore core, String device) throws Exception
		{
		Map<String,String> map=new HashMap<String, String>();
		for(String key:convVector(core.getDevicePropertyNames(device)))
			map.put(key,core.getProperty(device, key));
		return map;
		}
	
	
	
	}
