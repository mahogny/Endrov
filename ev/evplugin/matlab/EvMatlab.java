package evplugin.matlab;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import evgui.StartGUI;

/**
 * Endrov - Matlab interface
 * 
 * @author Johan Henriksson
 */
public class EvMatlab
	{
	public static String[] getJars(String path) 
		{
		StartGUI sg=new StartGUI();
		sg.collectSystemInfo(path);
		String[] s=new String[sg.jarfiles.size()];
		for(int i=0;i<s.length;i++)
			s[i]=sg.jarfiles.get(i);
		return s;
		}
	
	public static String currentPath() throws IOException
		{
		String basedir=new File(".").getCanonicalPath()+"/";
		return basedir;
		}
	
	public static int[] keySetInt(Map<Integer,Object> map)
		{
		Integer[] ci=map.keySet().toArray(new Integer[0]);
		int[] ia=new int[ci.length];
		for(int i=0;i<ia.length;i++)
			ia[i]=ci[i];
		return ia;
		}
	}
