/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.matlab;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import endrov.starter.Start;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;

/**
 * Endrov - Matlab interface
 * 
 * @author Johan Henriksson
 */
public class EvMatlab
	{
	public static String[] getJars(String path, String matlabroot, String arch) 
		{
		Start sg=new Start();
		sg.collectSystemInfo(path);
		
		if(installJNIpath(sg, matlabroot))
			System.out.println("Installed new shared objects. Need to restart matlab");
		
		installJavaopts(sg, matlabroot, arch);
		
		
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
	
	
	public static EvDecimal[] keySetEvDecimal(Map<EvDecimal,Object> map)
		{
		EvDecimal[] ci=map.keySet().toArray(new EvDecimal[0]);
		EvDecimal[] ia=new EvDecimal[ci.length];
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
	
	public static boolean installJNIpath(Start start, String matlabroot)
		{
		File root=new File(matlabroot);
		File libraryPath=new File(new File(new File(root,"toolbox"),"local"),"librarypath.txt");

		try
			{
			String content=EvFileUtil.readFile(libraryPath);

			List<String> missing=new LinkedList<String>();
			for(String s:start.binfiles)
				{
				if(!content.contains(s))
					missing.add(s);
				}
			
			if(!missing.isEmpty())
				{
				for(String s:missing)
					content+=s+"\n";
				EvFileUtil.writeFile(libraryPath, content);
				
				return true;
				}
			else
				return false;
			}
		catch (IOException e)
			{
			e.printStackTrace();
			return false;
			}
		//-Djava.library.path=/home/tbudev3/javaproj/ev/./libs/umanager_inc/bin_linux:/usr/lib/jni:/usr/lib/micro-manager:/home/tbudev3/javaproj/ev/./libs/bin_linux
		}
	
	
	public static void installJavaopts(Start start, String matlabroot, String arch)
		{
		File root=new File(matlabroot);
		File optsPath=new File(new File(new File(root,"bin"),arch),"java.opts");

		if(!optsPath.exists())
			{
			System.out.println("You might want to edit "+optsPath+" and add -Xmx1000m or similar");
			System.out.println("http://www.mathworks.com/support/solutions/data/1-18I2C.html");
			}
		
		
		}
	
	//can one generate static-private binding? different class loader, auto-generate code.
	
	}
