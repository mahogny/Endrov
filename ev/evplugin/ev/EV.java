package evplugin.ev;

//Personal config probably better as XML

//can be useful to improve performance, not used right now -Dsun.java2d.opengl=true

import java.util.*;
import java.util.prefs.*;
import java.io.*;

import javax.swing.*;

/**
 * Support functions for the EV framework
 * 
 * @author Johan Henriksson
 */
public class EV
	{
	public static final String version="2.2.0";
	public static final String website="http://celegans.sh.se:8020/yawiki/wiki.jz?page=yawiki:";
	public static final boolean debugMode=false;
	public static final String programName="EV";
	public static boolean confirmQuit=true;

	public static HashMap<String,PersonalConfig> personalConfigLoaders=new HashMap<String,PersonalConfig>();

	static
		{
		//This option is not needed on mac but anyway.
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		}
	
	/**
	 * Load personal config file or load defaults if it does not exist
	 */
	public static void loadPersonalConfig()
		{
		Preferences prefs = Preferences.userNodeForPackage (EV.class);
		String s=prefs.get("vwbdata", null);
		if(s!=null)
			{
			try
				{
				//Read settings from file
				//These could actually be sent to the console instead. plugins can register new
				//console commands and so add hooks for below
				ParamParse p=new ParamParse(s);
				while(p.hasData())
					{
					Vector<String> arg=p.nextData();
					if(arg.size()!=0)
						{
						PersonalConfig loader=personalConfigLoaders.get(arg.get(0));
						if(loader!=null)
							loader.loadPersonalConfig(arg);
						}
					}
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		else
			Log.printLog("No personal config file");
		}
	
	/**
	 * Save personal config file
	 */
	public static void savePersonalConfig()
		{
		String s="";
		
		for(PersonalConfig pc:personalConfigLoaders.values())
			s+=pc.savePersonalConfig();
		
		Log.printLog(s);
		
		Preferences prefs = Preferences.userNodeForPackage(EV.class);
		prefs.put("vwbdata", s);
		}

	
	/**
	 * Get a list of all plugins
	 */
	public static Vector<PluginInfo> getPluginList()
		{
		final Vector<PluginInfo> p=new Vector<PluginInfo>();
		File pluginDir=new File("evplugin");
		if(!pluginDir.exists())
			JOptionPane.showMessageDialog(null, "Plugin directory does not exist!");
		else
			for(File subdir:pluginDir.listFiles())
				{
				String plugin="evplugin/"+subdir.getName();
				PluginInfo pi=new PluginInfo(plugin);
				if(pi.exists())
					p.add(pi);
				}
		return p;
		}
	
	/**
	 * Load all plugins from evplugin/
	 */
	public static void loadPlugins()
		{
		for(PluginInfo pi:getPluginList())
			pi.load();
		}
	
	/**
	 * Check if the system is running a mac
	 */
	public static boolean isMac()
		{
		return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
		}

	/**
	 * Format a number to a certain number of digits
	 * @param n The number
	 * @param len The length of the final string
	 */
	public static String pad(int n, int len)
		{
		String s=""+n;
		while(s.length()<len)
			s="0"+s;
		return s;
		}

	
	public static void openExternal(File f)
		{
		try
			{
			if(EV.isMac())
				Runtime.getRuntime().exec(new String[]{"/usr/bin/open",f.getAbsolutePath()});
			else
				JOptionPane.showMessageDialog(null, "Feature only supported on Mac right now");
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	/**
	 * Quit EV. Saves personal config.
	 */
	public static void quit()
		{
  	EV.savePersonalConfig();
  	System.exit(0);
		}
	}
