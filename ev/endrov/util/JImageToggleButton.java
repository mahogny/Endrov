package endrov.util;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JToggleButton;

/**
 * Smaller button with an image
 * @author Johan Henriksson
 *
 */
public class JImageToggleButton extends JToggleButton
	{
	static final long serialVersionUID=0;
	
	private static final int imIconW=24;
	public Dimension getPreferredSize()
		{
		Dimension d=super.getPreferredSize();
		d.width=imIconW;
		return d;
		}
	
	public JImageToggleButton(Icon icon,String tooltip, boolean selected)
		{
		super(icon,selected);
		setToolTipText(tooltip);
		}

	public JImageToggleButton(Icon icon,String tooltip)
		{
		this(icon, tooltip, false);
		}

	}
