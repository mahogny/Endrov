/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package mmc.old;

import java.awt.*;
import javax.swing.*;

/**
 * 
 * @author Johan Henriksson
 */
public class ClientFrame extends JFrame
	{
	private JLabel screen;
	static final long serialVersionUID=0;	
	
	public ClientFrame()
		{
		setTitle("JVNC");
		setBackground(Color.WHITE);
		screen = new JLabel();
		getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		getContentPane().add(screen);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		}

	public void updateScreen(final ImageIcon i)
		{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
			{
			public void run()
				{
				screen.setIcon(i);
				screen.repaint();
				}
			});
		
		
		}

	public JLabel getLabel()
		{
		return screen;
		}
	}
