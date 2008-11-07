package endrov.flow.std.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.*;

import javax.swing.JOptionPane;

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
public class FlowUnitInput extends FlowUnit
	{
	
	public String varName;
	public FlowUnit varUnit;
	
	
	public FlowUnitInput(String varName) //unit todo
		{
		this.varName=varName;
		}
	
	public Dimension getBoundingBox()
		{
		int w=fm.stringWidth("In: "+varName);
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
		g.setColor(getBorderColor(panel));
		g.drawRoundRect(x,y,d.width,d.height,arcsize,arcsize);
		g.setColor(getTextColor());
		g.drawString("In: "+varName, x+5, y+fonta);
		
		
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
	
	
	public void editDialog()
		{
		String newVal=JOptionPane.showInputDialog(null,"Enter value",varName);
		if(newVal!=null)
			varName=newVal;
		}

	public void storeXML(Element e)
		{
		e.setAttribute("varname", varName);
		}

	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}
	
	public void evaluate(Flow flow) throws Exception
	{
	//TODO flowunit
	}

	
	}
