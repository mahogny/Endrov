package endrov.plateWindow.scene;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.vecmath.Vector2d;

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

		Vector2d trans=p.transformPointW2S(new Vector2d(0, 0));  //This should be in a common class!
		g2.translate(trans.x,trans.y);
		g2.scale(p.zoom, p.zoom);  
		g2.rotate(p.rotation);

		if(font!=null)
			g.setFont(font);
		
		int sw=g2.getFontMetrics().stringWidth(text);
		int sh=g2.getFontMetrics().getHeight();

		g2.setColor(Color.RED);
		int ax=x;
		int ay=y+sh/3;
		if(alignment==Alignment.Center)
			ax-=sw/2;
		else if(alignment==Alignment.Right)
			ax-=sw;
		g2.drawString(text, ax, ay);
		
		
		g2.rotate(-p.rotation);  //This should be in a common class!
		g2.scale(1/p.zoom,1/p.zoom); 
		g2.translate(-trans.x,-trans.y);

		
		
		}



	public Rectangle getBoundingBox()
		{
		return new Rectangle(x-20, y-20, 40,40); //This can be improved
		}
	}