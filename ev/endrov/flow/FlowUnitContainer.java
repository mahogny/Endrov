package endrov.flow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Flow unit of type container
 * @author Johan Henriksson
 *
 */
public abstract class FlowUnitContainer extends FlowUnit
	{
	public abstract String getContainerName();

	int contw=150, conth=80;
	
	
	public Dimension getBoundingBox()
		{
		Dimension d=new Dimension(contw,conth);
		return d;
		}
	
	public void paint(Graphics g, FlowPanel panel)
		{
		g.setColor(getBorderColor());
		g.drawRect(x,y,contw,conth);
		g.drawRect(x+2,y+2,contw-4,conth-4);
		
		int barh=fonth+4;
		g.setColor(Color.WHITE);
		g.fillRect(x+5, y-barh/2, fm.stringWidth(getContainerName())+10, barh);
		
		g.setColor(getBorderColor());
		g.drawRect(x+5, y-barh/2, fm.stringWidth(getContainerName())+10, barh);
		g.drawString(getContainerName(), x+10, y+(barh-fonta)/2);
		
		drawConnThrough(g, x, y+conth/2);
		drawConnThrough(g, x+contw-2, y+conth/2);
	/*	
		int yleft=y+conth/2;
		drawConnPointLeft(g, x, yleft);
		drawConnPointRight(g, x+2, yleft);
*/

		}

	private void drawConnThrough(Graphics g, int x, int y)
		{
		drawConnPointLeft(g, x, y);
		drawConnPointRight(g, x+2, y);
		}
	
	
	}
