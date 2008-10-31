package endrov.util;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Smaller button with an image
 * @author Johan Henriksson
 *
 */
public class JImageButton extends JButton
	{
	static final long serialVersionUID=0;
	
	private static final int imIconW=24;
	public Dimension getPreferredSize()
		{
		Dimension d=super.getPreferredSize();
		d.width=imIconW;
		return d;
		}
	
	public JImageButton(Icon icon)
		{
		super(icon);
		}
	
	}
