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
public class Scene2DScatter implements Scene2DElement
	{
//	public int x,y;
	public int[] listX;
	public int[] listY;
//	public int barw;
//	public int[] barh;
	
	public EvColor fillColor=EvColor.white;
	
	public Scene2DScatter(/*int x, int y,*/ int[] listX, int[] listY)
		{/*
		this.x = x;
		this.y = y;*/
		this.listX = listX;
		this.listY = listY;
		}



	public void paintComponent(Graphics g, Scene2DView p)
		{
		Graphics2D g2 = (Graphics2D)g; 			

		g2.setColor(fillColor.getAWTColor());
		for(int i=0;i<listX.length;i++)
			{
			int ax=listX[i];
			int ay=listY[i];
			g2.drawLine(ax,ay, ax,ay);
			}
		}



	public Rectangle getBoundingBox()
		{
		return null;
//		return new Rectangle(x-20, y-20, 40,40); //This can be improved
		}
	}