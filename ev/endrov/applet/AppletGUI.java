/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.applet;

import java.io.File;
import java.io.IOException;

import endrov.core.*;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.gui.window.EvBasicWindow;
import endrov.starter.Start;
import endrov.windowViewer3D.Viewer3DWindow;

import javax.swing.JApplet;

//http://lopica.sourceforge.net/faq.html#nosandbox
//System.setSecurityManager(null)

/**
 * Applet for model window and lineage. can be run stand-alone.
 * @author Johan Henriksson
 */
public class AppletGUI extends JApplet
	{
	static final long serialVersionUID=0; 
	
	/** Entry point when run as applet */
	public void init()
		{
		init(this);
		}
	
	/** Entry point when run stand-alone */
	public static void main(String[] args)
		{
		//Generate JAR-listing. Do not include OS-specific JARs
		try
			{
			int basepathlen=new File(".").getCanonicalPath().length()+1;
			for(String s:getJars(""))
				{
				File f=new File(s);
				String fs=f.getCanonicalPath().substring(basepathlen);
				System.out.println(fs+",");
				}
			}
		catch (IOException e){e.printStackTrace();}
		//Run
		EvLog.addListener(new EvLogStdout());
		AppletGUI.init(null);
		}

	/** Actual code for running EV */
	public static void init(JApplet applet)
		{
		try
			{
			EvPluginManager.readFromList=true;
			EndrovCore.loadPlugins();
			
//			JTextField fi=new JTextField();
	//		getContentPane().add(fi);

//			JOptionPane.showMessageDialog(applet, "foo");
			
			
			EvWindowManagerApplet manager=new EvWindowManagerApplet(2,applet==null);
			EvBasicWindow.windowManager=manager;
			new endrov.windowLineage.LineageWindow();
			new Viewer3DWindow();
			
			//Set display settings
			
			if(applet!=null)
				applet.getContentPane().add(manager.totalPane);
				
			}
		catch (Exception e)
			{
			EvLog.printError("EVGUI", e);
			}
		}
	
	
	public static String[] getJars(String path) 
		{
		Start sg=new Start();
		sg.collectSystemInfo(path);
		String[] s=new String[sg.jarfiles.size()];
		for(int i=0;i<s.length;i++)
			s[i]=sg.jarfiles.get(i);
		return s;
		}
	
	}
