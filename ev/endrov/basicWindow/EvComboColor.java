package endrov.basicWindow;

import java.awt.Color;
import javax.swing.JComboBox;

/**
 * Combo select for colors
 * @author Johan Henriksson
 */
public class EvComboColor extends JComboBox
	{
	static final long serialVersionUID=0;
	public EvComboColor()
		{
		super(EvColor.colorList);
		}
	

	public Color getColor()
		{
		return ((EvColor)getSelectedItem()).c;
		}
	}
