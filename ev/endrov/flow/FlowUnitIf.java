package endrov.flow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;

/**
 * Flow unit: input variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitIf extends FlowUnit
	{
	
	
	public Dimension getBoundingBox()
		{
		return new Dimension(30,40);
		}
	
	public void paint(Graphics g, FlowPanel panel)
		{
		Dimension d=getBoundingBox();

//		g.drawRect(x,y,d.width,d.height);

		drawConnPointRight(g,x+d.width,y+d.height/2);

		int y1=y+d.height/2-10;
		int y2=y+d.height/2+10;
		
		drawConnPointLeft(g,x,y+d.height/2);
		drawConnPointLeft(g,x,y1);
		drawConnPointLeft(g,x,y2);

		
		Polygon p=new Polygon(new int[]{x,x+d.width,x}, new int[]{y,y+d.height/2,y+d.height},3);
		g.setColor(new Color(255,255,200));
		g.fillPolygon(p);
		g.setColor(getBorderColor());
		g.drawPolygon(p);
		g.drawString("IF", x+5, y+(d.height+fonta)/2);
		
		g.drawLine(x+3, y1-2, x+7, y1-2);
		g.drawLine(x+5, y1-2, x+5, y1+2);

		g.drawLine(x+3, y2+2, x+7, y2+2);
		g.drawLine(x+5, y2-2, x+5, y2+2);

		}

	
	
	
	}
