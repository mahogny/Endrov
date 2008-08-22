package endrov.matlab;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import endrov.starter.Start;

/**
 * Endrov - Matlab interface
 * 
 * @author Johan Henriksson
 */
public class EvMatlab
	{
	public static String[] getJars(String path) 
		{
		Start sg=new Start();
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
	
	public ClassLoader instanceGetClassLoader(String urlpath)
		{
		return getClassLoader(urlpath);
		}
	public static ClassLoader getClassLoader(String urlpath)
		{
		try
			{
			return new URLClassLoader(new URL[]{new URL(urlpath)});
			}
		catch (MalformedURLException e)
			{
			e.printStackTrace();
			return null;
			}
		}
	
	//can one generate static-private binding? different class loader, auto-generate code.
	
	}
