package endrov.plateWindow.scene;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import endrov.basicWindow.EvColor;

/**
 * Scene element: text
 * 
 * @author Johan Henriksson
 *
 */
public class Scene2DHistogram implements Scene2DElement
	{
	public int x,y;
	public int barw;
	public int[] barh;
	
	public EvColor fillColor=EvColor.white;
	
	public Scene2DHistogram(int x, int y, int barw, int[] barh)
		{
		this.x = x;
		this.y = y;
		this.barw = barw;
		this.barh = barh;
		}



	public void paintComponent(Graphics g, Scene2DView p)
		{
		Graphics2D g2 = (Graphics2D)g; 			

		g2.setColor(fillColor.getAWTColor());
		for(int i=0;i<barh.length;i++)
			{
			int h=barh[i];
			g2.fillRect(x+i*barw, y-h, barw, h);
			}
		}



	public Rectangle getBoundingBox()
		{
		return new Rectangle(x-20, y-20, 40,40); //This can be improved
		}
	}