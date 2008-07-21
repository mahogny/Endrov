package endrov.flow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Flow unit: output variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitOutput extends FlowUnit
	{
	
	public String varName="channel";
	public FlowUnit varUnit;
	
	
	
	public Dimension getBoundingBox()
		{
		int w=fm.stringWidth("Out: "+varName);
		Dimension d=new Dimension(w+15,fonth);
		return d;
		}
	
	public void paint(Graphics g, FlowPanel panel)
		{
		Dimension d=getBoundingBox();

//		g.drawRect(x,y,d.width,d.height);
		
		int arcsize=8;
		
		
		g.setColor(Color.lightGray);
		g.fillRoundRect(x,y,d.width,d.height,arcsize,arcsize);
		g.setColor(Color.black);
		g.drawRoundRect(x,y,d.width,d.height,arcsize,arcsize);
		
		g.drawString("Out: "+varName, x+5, y+fonta);
		
		
		drawConnPointLeft(g,x,y+d.height/2);
		
		}

	
	
	}
