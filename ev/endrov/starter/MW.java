/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.starter;

import endrov.core.*;
import endrov.core.dbus.EndrovDBUS;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogFile;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.data.gui.EvDataGUI;
import endrov.flow.FlowExec;
import endrov.gui.RepeatingKeyEventsFixer;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.BasicWindowExtensionExitLast;
import endrov.gui.window.EvRegistrationDialog;
import endrov.gui.window.EvSplashScreen;
import endrov.windowViewer2D.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.swing.JOptionPane;

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
	public static void main(final String[] args)
		{
		for(int argi=0;argi<args.length;argi++)
			{
			if(args[argi].equals("--runflow"))
				{
				try
					{
					String fname=args[argi+1];
					String flowname=args[argi+2];
					
					EvData data=EvData.loadFile(new File(fname));
					EvPath path=EvPath.parse(data, flowname);
					FlowExec flowExec=new FlowExec(data, path);

					flowExec.evaluateAll();
					}
				catch (Exception e)
					{
					e.printStackTrace();
					System.exit(1);
					}
				
				System.exit(0);
				}
			}
		
		
		
		//If there is a file to open, try send it to existing session
		if(args.length!=0)
			{
			if(EndrovDBUS.openFile(Arrays.asList(args)))
				System.exit(0);
			}
		
		
		//This reduces the effect of one VERY annoying swing bug
		new RepeatingKeyEventsFixer().install();

		EvLog.addListener(new EvLogStdout());
		EvLog.addListener(new EvLogFile(EvSystemUtil.getLogFileName()));

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
		
		/*
		//if(!PluginInfo.storedInJar())
			{
			String javalib=System.getProperty("java.library.path");
			File javalibfile=new File(javalib);
			Log.printLog("Loading native libraries from "+javalibfile.getAbsolutePath());
			}
			*/
		
		try
			{
			if(EndrovDBUS.startServer())
				System.out.println("Started DBUS server");
			else
				System.out.println("Could not start DBUS server, skipping");
			
			EndrovCore.loadPlugins();
			BasicWindowExtensionExitLast.integrate();
			EndrovCore.loadPersonalConfig();		
			EndrovCore.setHasStartedUp();
			if(EvBasicWindow.getWindowList().size()==0)
				{
				//Make sure at least one window is open
				EvLog.printLog("Opening up first window");
				new Viewer2DWindow();
				}
			
			//Close the splash screen
			if(ss!=null)
				{
				ss.disableLog();
				ss.dispose();
				}
			
			//Bring up registration dialog if needed
			if(!EvRegistrationDialog.hasRegistered())
				{
				EvRegistrationDialog.runDialog();
				EvRegistrationDialog.connectAndRegister(true);
				}
			else
				EvRegistrationDialog.connectAndRegister(false);
			
			//Load files specified on command line
			new Thread(new Runnable()
				{ 
				public void run()
					{ 
					EndrovCore.waitUntilStartedUp();

					for(String s:args)
						{
						File f=new File(s);
						System.out.println("To be loaded from command line: "+f);
	
						EvData d=EvData.loadFile(f);
						if(d==null)
							JOptionPane.showMessageDialog(null, "Failed to open "+f);
						else
							{
							EvDataGUI.registerOpenedData(d);
							EvBasicWindow.updateLoadedFile(d);
							}
						}
					}}).start(); 

				
				
			
			}
		catch (Exception e)
			{
			EvLog.printError("EVGUI", e);
			}
		
		//Help memory debugging; remove dead objects
		System.gc();
		}
	}
