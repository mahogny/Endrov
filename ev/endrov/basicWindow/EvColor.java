package endrov.basicWindow;

import java.awt.Color;

/**
 * Colors for EV. They come with a name unlike AWT colors.
 * @author Johan Henriksson
 */
public class EvColor
	{
	public static EvColor black=new EvColor("Black",new Color(0,0,0));
	public static EvColor white=new EvColor("White",new Color(255,255,255));
	
	public static Color makeCol(double r, double g, double b, double mul)
		{
		Color c=new Color((float)(r*mul),(float)(g*mul),(float)(b*mul));
		return c;
		}
	
	public static EvColor[] colorList=new EvColor[]{
			black,
			
			new EvColor("Blue/Dark",new Color(0,0,128)),
			new EvColor("Blue/Medium",new Color(0,0,255)),
			new EvColor("Blue/Bright",new Color(128,128,255)),

			new EvColor("Cyan/Dark",new Color(0,128,128)),
			new EvColor("Cyan/Bright",new Color(128,255,255)),

			new EvColor("Gray/Dark",Color.DARK_GRAY),
			new EvColor("Gray/Medium",Color.GRAY),
			new EvColor("Gray/Bright",Color.LIGHT_GRAY),

			new EvColor("Green/Dark",new Color(0,128,0)),
			new EvColor("Green/Bright",new Color(0,255,0)),
			new EvColor("Green/Bright",new Color(128,255,128)),

			new EvColor("Magenta/Dark",new Color(128,0,128)),
			new EvColor("Magenta/Medium",new Color(255,0,255)),
			new EvColor("Magenta/Bright",new Color(255,128,255)),
			
			new EvColor("Orange",new Color(255,128,0)),
			new EvColor("Purple",new Color(200,0,255)),
			
			new EvColor("Red/Dark",new Color(128,0,0)),
			new EvColor("Red/Medium",new Color(255,0,0)),
			new EvColor("Red/Bright",new Color(255,128,128)),
			
			new EvColor("Yellow/Dark",new Color(128,128,0)),
			new EvColor("Yellow/Bright",new Color(255,255,0)),
			
			
			white,
			};
	
	public String name;
	public Color c;
	public EvColor(String name, Color c)
		{
		this.name=name;
		this.c=c;
		}
	public EvColor(String name, double r, double g, double b, double mul)
		{
		this.name=name;
		this.c=makeCol(r, g, b, mul);
		}
	public String toString()
		{
		return name;
		}
	
	}
