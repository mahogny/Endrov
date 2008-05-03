package evgui;

import evplugin.basicWindow.*;
import evplugin.ev.*;
import evplugin.imageWindow.*;

import java.io.*;
import java.lang.reflect.Method;

//http://lopica.sourceforge.net/faq.html#nosandbox
//System.setSecurityManager(null)

/**
 * Start graphical user interface, free-floating windows
 * @author Johan Henriksson
 */
public class MW
	{
	
	/**
	 * Entry point
	 * @param args Command line arguments
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());

		//Log.listeners.add(new SwingLog());

		//This is a hack over the plugin system. For some reason the application must
		//be registered really early of DnD will fail if the application is not initially
		//open.
		if(EV.isMac())
			{
			try
				{
				Class c=Class.forName("evplugin.macBinding.OSXAdapter");
				Method m=c.getDeclaredMethod("registerMacOSXApplication", new Class[]{});
				m.invoke(null, new Object[]{});
				System.out.println("invoked");
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		
		//A window showing initialization could be useful here
		
		EvSplashScreen ss=null;
		if(EvSplashScreen.isSplashEnabled())
			ss=new EvSplashScreen();
		
		
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
			EV.setHasStartedUp();
			if(BasicWindow.getWindowList().size()==0)
				{
				//Make sure at least one window is open
				new ImageWindow();
				}
			if(ss!=null)
				{
				ss.disableLog();
				ss.dispose();
				}
			}
		catch (Exception e)
			{
			Log.printError("EVGUI", e);
			}
		
		//Help memory debugging; remove dead objects
		System.gc();
		}
	}
