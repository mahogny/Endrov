package endrov.basicWindow;

import java.awt.Color;

/**
 * Colors for EV. They come with a name unlike AWT colors.
 * @author Johan Henriksson
 */
public class EvColor
	{
	public static EvColor[] colorList=new EvColor[]{
			new EvColor("D.Gray",Color.DARK_GRAY),
			new EvColor("M.Gray",Color.GRAY),
			new EvColor("L.Gray",Color.LIGHT_GRAY),
			new EvColor("White",new Color(255,255,255)),
			
			new EvColor("D.Red",new Color(128,0,0)),
			new EvColor("L.Red",new Color(255,0,0)),
			
			new EvColor("D.Green",new Color(0,128,0)),
			new EvColor("L.Green",new Color(0,255,0)),
			
			new EvColor("D.Blue",new Color(0,0,128)),
			new EvColor("L.Blue",new Color(0,0,255)),
			
			new EvColor("D.Yellow",new Color(128,128,0)),
			new EvColor("L.Yellow",new Color(255,255,0)),
			
			new EvColor("D.Cyan",new Color(0,128,128)),
			new EvColor("L.Cyan",new Color(0,255,255)),
			
			new EvColor("D.Magenta",new Color(128,0,128)),
			new EvColor("L.Magenta",new Color(255,0,255)),
			
			new EvColor("Purple",new Color(200,0,255)),
			new EvColor("Orange",new Color(255,128,0)),
			
			new EvColor("Black",new Color(0,0,0)),
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
