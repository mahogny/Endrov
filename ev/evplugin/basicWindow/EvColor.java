package evplugin.basicWindow;

import java.awt.Color;

/**
 * Colors for EV. They come with a name unlike AWT colors.
 * @author Johan Henriksson
 */
public class EvColor
	{
	public static EvColor[] colorList=new EvColor[]{
			new EvColor("L.Gray",Color.LIGHT_GRAY),
			new EvColor("Gray",Color.GRAY),
			new EvColor("D.Gray",Color.DARK_GRAY),
			new EvColor("Red",new Color(128,0,0)),
			new EvColor("Green",new Color(0,128,0)),
			new EvColor("Blue",new Color(0,0,128)),
			new EvColor("Yellow",new Color(128,128,0)),
			new EvColor("Cyan",new Color(0,128,128))
			};
	
	public String name;
	public Color c;
	public EvColor(String name, Color c)
		{
		this.name=name;
		this.c=c;
		}
	public String toString()
		{
		return name;
		}
	
	}
