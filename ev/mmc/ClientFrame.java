package mmc;

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
