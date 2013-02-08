/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.window;

import java.awt.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import endrov.core.EndrovCore;
import endrov.core.EvBuild;
import endrov.core.log.EvLog;


public class EvSplashScreen extends JFrame 
	{
	static final long serialVersionUID=0;
	private static ImageIcon iconSplash=new ImageIcon(EvSplashScreen.class.getResource("splash.png"));
	
	public static boolean isSplashEnabled()
		{
		Preferences prefs = Preferences.userNodeForPackage (EndrovCore.class);
		return prefs.getBoolean("evsplash", true);
		}
	
	public static void setSplashEnabled(boolean b)
		{
		Preferences prefs = Preferences.userNodeForPackage (EndrovCore.class);
		prefs.put("evsplash", ""+b);
		}
	
	private JLabel logLabel=new JLabel("");
	private JLabel lversion=new JLabel("Version "+EvBuild.version);

	private EvLog log=new EvLog(){
		public void listenDebug(String s)
			{
			logLabel.setText(s);
			}
		public void listenError(String s, Throwable e)
			{
			logLabel.setText(s);
			}
		public void listenLog(String s)
			{
			logLabel.setText(s);
			}
	};
	
	public EvSplashScreen()
		{
		Color white=new Color(1.0f,1.0f,1.0f);
		setBackground(white);
		setForeground(white);
		add(new JLabel(iconSplash), BorderLayout.CENTER);
		
		logLabel.setOpaque(true);
		lversion.setOpaque(true);
		
		JPanel p=new JPanel(new GridLayout(2,1));
		p.add(lversion);
		p.add(logLabel);
		
		lversion.setBackground(white);
		logLabel.setBackground(white);
		
		add(p, BorderLayout.SOUTH);
				
		setUndecorated(true);
		pack();
		setLocationRelativeTo(null);
		
		setVisible(true);
		toFront();
		
		EvLog.addListener(log);
		}
	
	public void disableLog()
		{
		EvLog.removeListener(log);
		}
	
	
	
	
	}
