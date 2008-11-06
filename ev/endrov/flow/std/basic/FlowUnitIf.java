package endrov.flow.std.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.ui.FlowPanel;

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
	
	private Polygon getPolygon(Dimension d)
		{
		return new Polygon(new int[]{x,x+d.width,x}, new int[]{y,y+d.height/2,y+d.height},3);
		}
	
	public void paint(Graphics g, FlowPanel panel)
		{
		Dimension d=getBoundingBox();

//		g.drawRect(x,y,d.width,d.height);

		panel.drawConnPointRight(g,this,"out",x+d.width,y+d.height/2);

		int y1=y+d.height/2-10;
		int y2=y+d.height/2+10;
		
		panel.drawConnPointLeft(g,this,"cond",x,y+d.height/2);
		panel.drawConnPointLeft(g,this,"true",x,y1);
		panel.drawConnPointLeft(g,this,"false",x,y2);

		
		Polygon p=getPolygon(d);
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

	public boolean mouseHoverMoveRegion(int x, int y)
		{
		Dimension dim=getBoundingBox();
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height &&
			getPolygon(dim).contains(x, y);
		}

	
	
	/** Get types of flows in */
	public SortedMap<String, FlowType> getTypesIn()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("cond", null);
		types.put("true", null);
		types.put("false", null);
		return types;
		}
	/** Get types of flows out */
	public SortedMap<String, FlowType> getTypesOut()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("out", null);
		return types;
		}

	public void editDialog(){}

	public void storeXML(Element e){}

	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	
	public void evaluate(Flow flow) throws Exception
		{
		lastOutput.clear();
		Boolean b=(Boolean)flow.getInputValue(this, "cond");
		if(b)
			lastOutput.put("out", flow.getInputValue(this, "true"));
		else
			lastOutput.put("out", flow.getInputValue(this, "false"));
		}
	
	
	}