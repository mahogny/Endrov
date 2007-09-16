package evgui;


import evplugin.basicWindow.*;
import evplugin.ev.*;
import evplugin.imageWindow.*;

import java.io.*;

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

		String javalib=System.getProperty("java.library.path");
		File javalibfile=new File(javalib);
		Log.printLog("Loading native libraries from "+javalibfile.getAbsolutePath());
		
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
			}
		catch (Exception e)
			{
			Log.printError("EVGUI", e);
			}
		}
	}
