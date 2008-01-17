package evplugin.basicWindow;

import java.awt.Color;
import javax.swing.JComboBox;

/**
 * 
 * @author Johan Henriksson
 */
public class ColorCombo extends JComboBox
	{
	static final long serialVersionUID=0;
	public ColorCombo()
		{
		super(new MyColor[]{
				new MyColor("L.Gray",Color.LIGHT_GRAY),
				new MyColor("Gray",Color.GRAY),
				new MyColor("D.Gray",Color.DARK_GRAY),
				new MyColor("Red",new Color(128,0,0)),
				new MyColor("Green",new Color(0,128,0)),
				new MyColor("Blue",new Color(0,0,128)),
				new MyColor("Yellow",new Color(128,128,0)),
				new MyColor("Cyan",new Color(0,128,128))
				});
		}
	
	
	private static class MyColor
		{
		String name;
		Color c;
		public MyColor(String name, Color c)
			{
			this.name=name;
			this.c=c;
			}
		public String toString()
			{
			return name;
			}
		}
	
	public Color getColor()
		{
		return ((MyColor)getSelectedItem()).c;
		}
	}
