package evplugin.ev;

import java.awt.*;
import javax.swing.*;


public class SplashScreen extends JFrame
	{
	static final long serialVersionUID=0;
	private static ImageIcon iconSplash=new ImageIcon(SplashScreen.class.getResource("splash.png"));
	
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
