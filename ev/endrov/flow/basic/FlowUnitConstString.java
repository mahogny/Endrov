package endrov.flow.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.*;

import endrov.flow.FlowUnit;
import endrov.flow.type.FlowType;
import endrov.flow.ui.FlowPanel;

/**
 * Flow unit: input variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitConstString extends FlowUnit
	{
	
	public String var;
	
	
	public FlowUnitConstString(String var) 
		{
		this.var=var;
		}
	
	public Dimension getBoundingBox()
		{
		int w=fm.stringWidth("\""+var+"\"");
		Dimension d=new Dimension(w+25,fonth);
		return d;
		}
	
	public void paint(Graphics g, FlowPanel panel)
		{
		Dimension d=getBoundingBox();

		
		g.setColor(Color.WHITE);
		g.fillRect(x,y,d.width,d.height);
		g.setColor(Color.black);
		g.drawRect(x,y,d.width,d.height);
		g.drawLine(x+2,y,x+2,y+d.height);
		g.drawLine(x+d.width-2,y,x+d.width-2,y+d.height);
		g.drawString("\""+var+"\"", x+8, y+fonta);
		
		
		panel.drawConnPointRight(g,this,"out",x+d.width,y+d.height/2);
		}

	public boolean mouseHoverMoveRegion(int x, int y)
		{
		Dimension dim=getBoundingBox();
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}


	/** Get types of flows in */
	public Map<String, FlowType> getTypesIn()
		{
		return Collections.emptyMap();
		}
	/** Get types of flows out */
	public Map<String, FlowType> getTypesOut()
		{
		Map<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("out", null);
		return types;
		}
	
	
	}
