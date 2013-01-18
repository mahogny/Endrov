package endrov.plateWindow.scene;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Scene element: text
 * 
 * @author Johan Henriksson
 *
 */
public class Scene2DText implements Scene2DElement
	{
	public int x,y;
	public String text;
	public Font font;
	public Alignment alignment=Alignment.Left;
	
	public enum Alignment
		{
		Left, Center, Right
		}
	
	public Scene2DText(int x, int y, String text)
		{
		this.x = x;
		this.y = y;
		this.text = text;
		}



	public void paintComponent(Graphics g, Scene2DView p)
		{
		Graphics2D g2 = (Graphics2D)g; 			

		g2.setColor(Color.RED);
		if(font!=null)
			g.setFont(font);
		
		int sw=g2.getFontMetrics().stringWidth(text);
		int sh=g2.getFontMetrics().getHeight();

		
		int ax=x;
		if(alignment==Alignment.Center)
			ax-=sw/2;
		else if(alignment==Alignment.Right)
			ax-=sw;
		
		int ay=y+sh/3;

		
		g2.drawString(text, ax, ay);
		}



	public Rectangle getBoundingBox()
		{
		return new Rectangle(x-20, y-20, 40,40); //This can be improved
		}
	}