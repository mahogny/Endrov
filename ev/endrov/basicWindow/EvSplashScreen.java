package endrov.basicWindow;

import java.awt.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import endrov.ev.EV;
import endrov.ev.EvBuild;
import endrov.ev.Log;


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
	
	private JLabel logLabel=new JLabel("");
	
	private Log log=new Log(){
		public void listenDebug(String s)
			{
			logLabel.setText(s);
			}
		public void listenError(String s, Exception e)
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
		add(new JLabel(iconSplash), BorderLayout.CENTER);
		
		JPanel p=new JPanel(new GridLayout(2,1));
		p.add(new JLabel("Version "+EvBuild.version));
		p.add(logLabel);
		
		add(p, BorderLayout.SOUTH);
				
		setUndecorated(true);
		pack();
		setLocationRelativeTo(null);
		
		setVisible(true);
		toFront();
		
		Log.listeners.add(log);
		}
	
	public void disableLog()
		{
		Log.listeners.remove(log);
		}
	
	
	
	
	}
