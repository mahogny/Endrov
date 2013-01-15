package endrov.plateWindow.scene;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.vecmath.Vector2d;

public class Scene2DText implements Scene2DElement
	{
	public int x,y;
	public String text;

	
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

		int sw=g2.getFontMetrics().stringWidth(text);

		g2.setColor(Color.RED);
		g2.drawString(text, x-sw/2, y);
		
		
		g2.rotate(-p.rotation);  //This should be in a common class!
		g2.scale(1/p.zoom,1/p.zoom); 
		g2.translate(-trans.x,-trans.y);

		
		
		}
	}