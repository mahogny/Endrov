package endrov.windowPlateAnalysis.scene;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import endrov.gui.EvColor;

/**
 * Scene element: text
 * 
 * @author Johan Henriksson
 *
 */
public class Scene2DRect implements Scene2DElement
	{
	public int x,y, w,h;
	public EvColor fillColor;
	public EvColor borderColor;
	
	public Scene2DRect(int x, int y, int w, int h)
		{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		}



	public void paintComponent(Graphics g, Scene2DView p)
		{
		Graphics2D g2 = (Graphics2D)g; 			

		if(fillColor!=null)
			{
			g2.setColor(fillColor.getAWTColor());
			g2.fillRect(x, y, w, h);
			}
		if(borderColor!=null)
			{
			g2.setColor(borderColor.getAWTColor());
			g2.drawRect(x, y, w, h);
			}
		
		}



	public Rectangle getBoundingBox()
		{
		return new Rectangle(x-20, y-20, 40,40); //This can be improved
		}
	}