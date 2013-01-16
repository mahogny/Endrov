package endrov.plateWindow.scene;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.vecmath.Vector2d;

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

		Vector2d trans=p.transformPointW2S(new Vector2d(0, 0));  //This should be in a common class!
		g2.translate(trans.x,trans.y);
		g2.scale(p.zoom, p.zoom);  
		g2.rotate(p.rotation);

		g2.setColor(fillColor.getAWTColor());
		for(int i=0;i<barh.length;i++)
			{
			int h=barh[i];
			g2.fillRect(x+i*barw, y-h, barw, h);
			}
		
		g2.rotate(-p.rotation);  //This should be in a common class!
		g2.scale(1/p.zoom,1/p.zoom); 
		g2.translate(-trans.x,-trans.y);
		}



	public Rectangle getBoundingBox()
		{
		return new Rectangle(x-20, y-20, 40,40); //This can be improved
		}
	}