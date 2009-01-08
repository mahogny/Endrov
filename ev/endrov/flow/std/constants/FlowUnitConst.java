package endrov.flow.std.constants;

import java.awt.Color;
import java.awt.Component;
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

public abstract class FlowUnitConst extends FlowUnit
	{

	
	

	protected abstract String getLabel();
	
	public Dimension getBoundingBox(Component comp)
		{
		int w=fm.stringWidth(getLabel());
		Dimension d=new Dimension(3+w+3+comp.getWidth(),comp.getHeight());
		return d;
		}
	
	public void paint(Graphics g, FlowPanel panel, Component comp)
		{
		Dimension d=getBoundingBox(comp);
		
		g.setColor(Color.GREEN);
		g.fillRect(x,y,d.width,d.height);
		g.setColor(getBorderColor(panel));
		g.drawRect(x,y,d.width,d.height);
		g.setColor(getTextColor());
		g.drawString(getLabel(), x+3, y+d.height/2+fonta/2);
		
		panel.drawConnPointRight(g,this,"out",x+d.width,y+d.height/2);
		}

	public boolean mouseHoverMoveRegion(int x, int y, Component comp)
		{
		Dimension dim=getBoundingBox(comp);
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
		types.put("out", getConstType());
		return types;
		}
	
	protected abstract FlowType getConstType();
	
	public void editDialog()
		{
		}

	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	
	
	
	
	public int getGUIcomponentOffsetX()
		{
		int w=fm.stringWidth(getLabel());
		return 3+w+3;
		}
	public int getGUIcomponentOffsetY(){return 0;}

	
	}
