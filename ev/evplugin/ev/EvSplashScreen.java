package evplugin.ev;

import java.awt.*;
import java.util.prefs.Preferences;

import javax.swing.*;


public class EvSplashScreen extends JFrame
	{
	static final long serialVersionUID=0;
	private static ImageIcon iconSplash=new ImageIcon(EvSplashScreen.class.getResource("splash.png"));
	
	public static boolean isSplashEnabled()
		{
		Preferences prefs = Preferences.userNodeForPackage (EV.class);
		return prefs.getBoolean("evsplash", true);
		}
	
	public static void setSplashEnabled(boolean b)
		{
		Preferences prefs = Preferences.userNodeForPackage (EV.class);
		prefs.put("evsplash", ""+b);
		}
	
	
	public EvSplashScreen()
		{
		add(new JLabel(iconSplash), BorderLayout.CENTER);
		add(new JLabel("Version "+EV.version), BorderLayout.SOUTH);
				
		setUndecorated(true);
		pack();
		setLocationRelativeTo(null);
		
		setVisible(true);
		toFront();
		}
	
	}
