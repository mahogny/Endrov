/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.basicWindow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * Colors for EV. They come with a name unlike AWT colors.
 * @author Johan Henriksson
 */
public class EvColor
	{
	public static EvColor black=new EvColor("Black",new Color(0,0,0));
	public static EvColor white=new EvColor("White",new Color(255,255,255));
	public static EvColor red=new EvColor("Red",new Color(255,0,0));
	public static EvColor magenta=new EvColor("Magenta",new Color(255,0,255));
	public static EvColor yellow=new EvColor("Yellow",new Color(255,255,0));
	public static EvColor green=new EvColor("Green",new Color(0,255,0));
	public static EvColor cyan=new EvColor("Cyan",new Color(0,255,255));
	public static EvColor blue=new EvColor("Blue",new Color(0,0,255));
	
	
	public static EvColor redMedium=new EvColor("Red/Medium",new Color(255,0,0));
	
	public static EvColor grayDark=new EvColor("Gray/Dark",Color.DARK_GRAY);
	public static EvColor grayMedium=new EvColor("Gray/Medium",Color.GRAY);
	public static EvColor grayBright=new EvColor("Gray/Bright",Color.LIGHT_GRAY);
	
	public static Color makeCol(double r, double g, double b, double mul)
		{
		Color c=new Color((float)(r*mul),(float)(g*mul),(float)(b*mul));
		return c;
		}
	
	
	public static EvColor[] colorList=new EvColor[]{
			black,
			
			new EvColor("Blue/Dark",new Color(0,0,128)),
			new EvColor("Blue/Medium",new Color(0,0,255)),
			new EvColor("Blue/Bright",new Color(0,128,255)),

			new EvColor("Cyan/Dark",new Color(0,128,128)),
			new EvColor("Cyan/Bright",new Color(128,255,255)),

			grayDark,
			grayMedium,
			grayBright,

			new EvColor("Green/Dark",new Color(0,128,0)),
			new EvColor("Green/Medium",new Color(0,255,0)),
			new EvColor("Green/Bright",new Color(128,255,128)),

			new EvColor("Magenta/Dark",new Color(128,0,128)),
			new EvColor("Magenta/Medium",new Color(255,0,255)),
			new EvColor("Magenta/Bright",new Color(255,128,255)),
			
			new EvColor("Orange",new Color(255,128,0)),
			new EvColor("Purple",new Color(200,0,255)),
			
			new EvColor("Red/Dark",new Color(128,0,0)),
			redMedium,
			new EvColor("Red/Bright",new Color(255,128,128)),
			new EvColor("Red/Green64",new Color(255,64,0)),
			new EvColor("Red/Blue64",new Color(255,0,64)),
			
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
	
	public double getRedDouble()
		{
		return c.getRed()/255.0;
		}
	public double getGreenDouble()
		{
		return c.getGreen()/255.0;
		}
	public double getBlueDouble()
		{
		return c.getBlue()/255.0;
		}

	public float getRedFloat()
		{
		return (float)(c.getRed()/255.0);
		}
	public float getGreenFloat()
		{
		return (float)(c.getGreen()/255.0);
		}
	public float getBlueFloat()
		{
		return (float)(c.getBlue()/255.0);
		}

	
	public Color getAWTColor()
		{
		return c;
		}
	
	public interface ColorMenuListener
		{
		public void setColor(EvColor c); 
		}
	
	public BufferedImage getSampleIcon()
		{
		BufferedImage bim=new BufferedImage(16,16,BufferedImage.TYPE_INT_BGR);
		Graphics g=bim.getGraphics();
		g.setColor(c);
		g.fillRect(0, 0, 16, 16);
		return bim;
		}
	
	public static void addColorMenuEntries(JMenu menu, final ColorMenuListener list)
		{
		for(final EvColor c:EvColor.colorList)
			{
			BufferedImage bim=c.getSampleIcon();
			JMenuItem mi = new JMenuItem(c.name,new ImageIcon(bim));
			mi.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					list.setColor(c);
					}
				});
			menu.add(mi);
			}
		
		
		
		}

	@Override
	public boolean equals(Object obj)
		{
		if(obj instanceof EvColor)
			{
			EvColor oc=(EvColor)obj;
			return oc.c.equals(c);
			}
		else
			return false;
		}
	
	}
