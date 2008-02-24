package evplugin.ev;

import java.awt.BorderLayout;
import javax.swing.*;

/**
 * Various GUI related helper functions
 * @author Johan Henriksson
 */
public class EvSwingTools
	{

	/**
	 * Add a label to the left of a swing component 
	 */
	public static JPanel withLabel(String s, JComponent c)
		{
		JPanel p=new JPanel(new BorderLayout());
		p.add(new JLabel(s),BorderLayout.WEST);
		p.add(c,BorderLayout.CENTER);
		return p;
		}

	}
