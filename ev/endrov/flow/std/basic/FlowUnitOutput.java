package endrov.flow.std.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.ui.FlowPanel;

/**
 * Flow unit: output variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitOutput extends FlowUnit
	{
	
	public String varName;
	public FlowUnit varUnit;
	
	public FlowUnitOutput(String varName) //unit todo
		{
		this.varName=varName;
		}
	
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
		
		
		panel.drawConnPointLeft(g,this, "in",x,y+d.height/2);
		
		}

	public boolean mouseHoverMoveRegion(int x, int y)
		{
		Dimension dim=getBoundingBox();
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}

	
	
	/** Get types of flows in */
	public Map<String, FlowType> getTypesIn()
		{
		Map<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("in", null);
		return types;
		}
	/** Get types of flows out */
	public Map<String, FlowType> getTypesOut()
		{
		return Collections.emptyMap();
		}

	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	
	}
