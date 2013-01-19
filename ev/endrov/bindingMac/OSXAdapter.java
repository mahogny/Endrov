/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.bindingMac;

import java.io.File;
import javax.swing.JOptionPane;
//import javax.swing.SwingUtilities;

import com.apple.eawt.*;
import endrov.core.EndrovCore;
import endrov.data.EvData;
import endrov.gui.window.BasicWindow;


public class OSXAdapter extends ApplicationAdapter implements MRJOpenDocumentHandler
	{
	
	//pseudo-singleton model; no point in making multiple instances
	//of the EAWT application or our adapter
	private static com.apple.eawt.Application theApplication;
	

	/**
	 * The main entry-point for this functionality.  This is the only method
	 * that needs to be called at runtime, and it can easily be done using
	 * reflection
	 */
	@SuppressWarnings("deprecation") public static void registerMacOSXApplication() 
		{
		if (theApplication == null)
			{
			theApplication = new com.apple.eawt.Application();
			OSXAdapter theAdapter = new OSXAdapter();
			theApplication.addApplicationListener(theAdapter);
			MRJApplicationUtils.registerOpenDocumentHandler(theAdapter);
			}
		}
	
	/**
	 * Another static entry point for EAWT functionality.  Enables the 
	 * "Preferences..." menu item in the application menu. 
	 */ 
	public static void enablePrefs(boolean enabled) 
		{
		if (theApplication == null) 
			theApplication = new com.apple.eawt.Application();
		theApplication.setEnabledPreferencesMenu(enabled);
		}
	
	/**
	 * Invoked on Apple -> About
	 */
	public void handleAbout(ApplicationEvent ae) 
		{
		ae.setHandled(true);
		BasicWindow.dialogAbout();
		}
	
	/**
	 * Invoked on Apple -> Preferences
	 */
	public void handlePreferences(ApplicationEvent ae)
		{
		ae.setHandled(true);
		}
	
	/**
	 * Invoked on Apple -> Quit
	 */
	public void handleQuit(ApplicationEvent ae) 
		{
		ae.setHandled(false);
		BasicWindow.dialogQuit();
		}

	//Note that DnD will not work if starter for next app is used.
	//For no reason at all, openfile is not opened on startup
	
	/**
	 * Invoked on file drag to bundle
	 */
	public void handleOpenFile(final File f)
		{
//		JOptionPane.showMessageDialog(null, "open file");
		//used to be done with swingutils.
		new Runnable() { 
			public void run()
				{ 
				EndrovCore.waitUntilStartedUp();
				
				EvData d=EvData.loadFile(f);
				if(d==null)
					JOptionPane.showMessageDialog(null, "Failed to open "+f);
				else
					{
					EvData.registerOpenedData(d);
					BasicWindow.updateLoadedFile(d);
					}
				}}.run(); 
		}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		registerMacOSXApplication();
		//enablePrefs(true);
		}

	}
