package endrov.starter;

import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.ev.*;
import endrov.lineageWindow.LineageWindow;
import endrov.modelWindow.ModelWindow;

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
		Log.listeners.add(new StdoutLog());

		//Log.listeners.add(new SwingLog());

		//This is a hack over the plugin system. For some reason the application must
		//be registered really early of DnD will fail if the application is not initially
		//open.
		if(EV.isMac())
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
				new LineageWindow();
				new ModelWindow();
				}
			EvData.registerOpenedData(EvData.loadFile(new File("angler.ostxml")));
			EvData.registerOpenedData(EvData.loadFile(new File("ce2008.ostxml")));
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
