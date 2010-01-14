/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ev;

import java.io.*;
//import java.awt.*;
//import java.net.*;

/*
 * Note - you must include the url type -- either "http://" or
 * "file://".
 */
public class BrowserControl
	{
	/**
	 * Display a file in the system browser.  If you want to display a
	 * file, you must include the absolute path name.
	 *
	 * @param url the file's url (the url must start with either "http://"	or "file://").
	 */
	public static void displayURL(String url)
		{
		try
			{
			if(isMacPlatform())
				Runtime.getRuntime().exec("open "+url);
			else if (isWindowsPlatform())
				Runtime.getRuntime().exec(WIN_PATH + " " + WIN_FLAG + " " + url);
			else
				{
				//TODO JAVA6
				/*
				if(java.awt.Desktop.isDesktopSupported())
					{
					System.out.println("Desktop supported!");
					try
						{
						Desktop.getDesktop().browse(new URI(url));
						}
					catch (URISyntaxException e)
						{
						e.printStackTrace();
						}
					}
				else*/
					{
					//Fallback
				
					// cmd = 'netscape -remote openURL(http://www.javaworld.com)'
					String cmd=UNIX_PATH + " " + UNIX_FLAG +" -- "+ url;
					System.out.println("Running command:" + cmd);
					Process p = Runtime.getRuntime().exec(cmd);
					try
						{
						// wait for exit code -- if it's 0, command worked,
						// otherwise we need to start the browser up.
						int exitCode = p.waitFor();
						if (exitCode != 0)
							Runtime.getRuntime().exec(UNIX_PATH + " " + url);
						}
					catch(InterruptedException x)
						{
						System.err.println("Error bringing up browser");
						System.err.println("Caught: " + x);
						}
					}
				}
			}
		catch(IOException x)
			{
			// couldn't exec browser
			System.err.println("Could not invoke browser");
			System.err.println("Caught: " + x);
			}
		}
	/**
	 * Try to determine whether this application is running under Windows
	 * or some other platform by examing the "os.name" property.
	 *
	 * @return true if this application is running under a Windows OS
	 */
	public static boolean isWindowsPlatform()
		{
		String os = System.getProperty("os.name");
		if ( os != null && os.startsWith(WIN_ID))
			return true;
		else
			return false;
		}
	
	/**
	 * Try to determine whether this application is running under Mac
	 * or some other platform by examing the "os.name" property.
	 *
	 * @return true if this application is running under a Windows OS
	 */
	public static boolean isMacPlatform()
		{
		return (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));
		}
	
	// Used to identify the windows platform.
	private static final String WIN_ID = "Windows";
	// The default system browser under windows.
	private static final String WIN_PATH = "rundll32";
	// The flag to display a url.
	private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
	// The default browser under unix.
	private static final String UNIX_PATH = "firefox";
	// The flag to display a url.
	private static final String UNIX_FLAG = "-new-tab";
	}
