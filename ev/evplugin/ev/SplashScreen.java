package evplugin.ev;

import java.awt.*;
import java.util.prefs.Preferences;

import javax.swing.*;


public class SplashScreen extends JFrame
	{
	static final long serialVersionUID=0;
	private static ImageIcon iconSplash=new ImageIcon(SplashScreen.class.getResource("splash.png"));
	
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
	
	
	public SplashScreen()
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
