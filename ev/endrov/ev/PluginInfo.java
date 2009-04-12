package endrov.ev;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;

import endrov.util.EvFileUtil;

/**
 * Load plugins. Every plugin is located in evplugin/.../ and has a PLUGIN.java describing how to load it.
 * 
 * @author Johan Henriksson
 */
public class PluginInfo
	{
	public String classPath;
	public PluginDef pdef=null;
	
	/** Set to true if pluginlist.txt should be used instead of scanning the plugin directories */
	public static boolean readFromList=false;

	
	/**
	 * Check if there was a plugin in the directory
	 */
	public boolean exists()
		{
		return pdef!=null;
		}

	/**
	 * Read directory for plugin information
	 */
	public PluginInfo(String classPath)
		{
	//	this.filename=filename;
		this.classPath=classPath;
		pdef=null;
		try
			{
		//	String classPath=(new File(filename)).getAbsolutePath().
			//substring(-1+(new File(".")).getAbsolutePath().length()).replace('/', '.').replace('\\','.');
			Class<?> theClass=Class.forName(classPath+".PLUGIN");
			Constructor<?> constr=theClass.getConstructor(new Class<?>[]{});
			pdef=(PluginDef)constr.newInstance(new Object[]{});
			}
		catch (Exception e)
			{
			JOptionPane.showMessageDialog(null, "Error loading plugin "+classPath+": "+e.getMessage());
			System.out.println("Error loading plugin "+classPath+": "+e.getMessage());
			System.exit(0);
			}
		}
	
	
	/**
	 * Load the plugin
	 */
	public void load()
		{
		if(exists())
			{
			if(pdef.systemSupported())
				{
				Log.printLog("Loading plugin "+pdef.getPluginName());
				for(Class<?> foo:pdef.getInitClasses())
					{
					try
						{
						Method m=foo.getDeclaredMethod("initPlugin", new Class[]{});
						m.invoke(foo, new Object[]{});
						}
					catch (Exception e)
						{
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Could not load EV; plugin "+pdef.getPluginName()+" broken\n"+
								MemoryLog.logPrintString(e));
						}
					}
				}
			else
				Log.printLog("Skipping unsupported plugin "+pdef.getPluginName());
			}
		}
	


	
	public String toString()
		{
		if(pdef!=null)
			return pdef.getPluginName();
		else
			return classPath;
		}

	/**
	 * Get a list of all plugins
	 */
	public static List<PluginInfo> getPluginList()
		{
		final List<PluginInfo> p=new LinkedList<PluginInfo>();
		if(readFromList)
			{
			try
				{
				InputStream input = EV.class.getResourceAsStream("pluginlist.txt");
				BufferedReader br = new BufferedReader(new InputStreamReader(input));
				String strLine;
				while ((strLine = br.readLine()) != null)
					p.add(new PluginInfo(strLine.replace('/', '.').replace('\\','.')));
				}
			catch (Exception e)
				{
				JOptionPane.showMessageDialog(null, "Problem reading plugin listing: "+e.getMessage());
				}
			}
		else
			{
			File pluginDir=EvFileUtil.getFileFromURL(PluginInfo.class.getResource(".")).getParentFile();
			if(!pluginDir.exists())
				{
				JOptionPane.showMessageDialog(null, "Plugin directory does not exist!");
				System.out.println("Plugin directory does not exist! "+pluginDir);
				}
			else
				for(File subdir:pluginDir.listFiles())
						{
						if(new File(subdir,"PLUGIN.class").exists())
							{
							String classPath=subdir.getAbsolutePath().substring(pluginDir.getParent().length());
							classPath=classPath.replace('/', '.').replace('\\','.');
							if(classPath.startsWith("."))
								classPath=classPath.substring(1);
							
							PluginInfo pi=new PluginInfo(classPath);
							if(pi.exists())
								p.add(pi);
							}
						}
			}
//		JOptionPane.showMessageDialog(null, "now here "+p.size());
		return p;
		}

	/**
	 * Check if EV is stored in a JAR-file
	 */
	/*
	public static boolean storedInJar()
		{
		System.out.println("readpluginlist: "+System.getProperty("ev.readpluginlist"));
		
		
		URL url=EV.class.getResource("pluginlist.txt");
		return url!=null && url.toString().indexOf(".jar!")!=-1;
		}
	*/

	}
