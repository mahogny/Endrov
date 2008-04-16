package evplugin.basicWindow;

import java.awt.Color;
import javax.swing.JComboBox;

/**
 * Combo select for colors
 * @author Johan Henriksson
 */
public class ColorCombo extends JComboBox
	{
	static final long serialVersionUID=0;
	public ColorCombo()
		{
		super(EvColor.colorList);
		}
	

	public Color getColor()
		{
		return ((EvColor)getSelectedItem()).c;
		}
	}
