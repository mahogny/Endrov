/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.starter;

import endrov.core.*;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.BasicWindowExtensionExitLast;
import endrov.gui.window.EvSplashScreen;
import endrov.windowViewer3D.Viewer3DWindow;

import java.io.*;
import java.lang.reflect.Method;

//http://lopica.sourceforge.net/faq.html#nosandbox
//System.setSecurityManager(null)

/**
 * Start lw C.E viewer
 * @author Johan Henriksson
 */
public class CEviewer
	{
	
	/**
	 * Entry point
	 * @param args Command line arguments
	 */
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());

		//Log.listeners.add(new SwingLog());

		//This is a hack over the plugin system. For some reason the application must
		//be registered really early of DnD will fail if the application is not initially
		//open.
		if(EvSystemUtil.isMac())
			{
			try
				{
				Class<?> c=Class.forName("endrov.macBinding.OSXAdapter");
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
			EvLog.printLog("Loading native libraries from "+javalibfile.getAbsolutePath());
			}
		
		try
			{
			EndrovCore.loadPlugins();
			BasicWindowExtensionExitLast.integrate();
			EndrovCore.loadPersonalConfig();		
			EndrovCore.setHasStartedUp();
			if(EvBasicWindow.getWindowList().size()==0)
				{
				new endrov.windowLineage.LineageWindow();
				new Viewer3DWindow();
				}
			EvData.registerOpenedData(EvData.loadFile(new File("angler.ost")));
			EvData.registerOpenedData(EvData.loadFile(new File("ce2008.ost")));
			if(ss!=null)
				{
				ss.disableLog();
				ss.dispose();
				}
			}
		catch (Exception e)
			{
			EvLog.printError("EVGUI", e);
			}
		
		//Help memory debugging; remove dead objects
		System.gc();
		}
	}
