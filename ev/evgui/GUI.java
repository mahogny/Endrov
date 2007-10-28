package evgui;


import evplugin.basicWindow.*;
import evplugin.ev.*;
import evplugin.imageWindow.*;

import java.io.*;

//http://lopica.sourceforge.net/faq.html#nosandbox
//System.setSecurityManager(null)

/**
 * Start graphical user interface
 * @author Johan Henriksson
 */
public class GUI
	{
	/**
	 * Entry point
	 * @param args Command line arguments
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());

		//Log.listeners.add(new SwingLog());

		//A window showing initialization could be useful here
		
		SplashScreen ss=new SplashScreen();
		
		
		//if(!PluginInfo.storedInJar())
			{
			String javalib=System.getProperty("java.library.path");
			File javalibfile=new File(javalib);
			Log.printLog("Loading native libraries from "+javalibfile.getAbsolutePath());
			}
		
		try
			{
			EV.loadPlugins();
			BasicWindowExitLast.integrate();
			EV.loadPersonalConfig();		
			if(BasicWindow.windowList.size()==0)
				{
				//Make sure at least one window is open
				new ImageWindow();
				}
			ss.dispose();
			}
		catch (Exception e)
			{
			Log.printError("EVGUI", e);
			}
		}
	}
