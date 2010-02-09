/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.control;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;

/**
 * Flow unit: input variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitIf extends FlowUnit
	{
	private static final String metaType="if";
	private static ImageIcon icon=new ImageIcon(FlowUnitIf.class.getResource("jhIf.png"));

	
	public Dimension getBoundingBox(Component comp, Flow flow)
		{
		return new Dimension(30,40);
		}
	
	private Polygon getPolygon(Dimension d)
		{
		return new Polygon(new int[]{x,x+d.width,x}, new int[]{y,y+d.height/2,y+d.height},3);
		}
	
	public void paint(Graphics g, FlowPanel panel, Component comp)
		{
		Dimension d=getBoundingBox(comp, panel.getFlow());

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
		g.setColor(getBorderColor(panel));
		g.drawPolygon(p);
		g.setColor(getTextColor());
		g.drawString("IF", x+5, y+(d.height+fonta)/2);
		
		g.drawLine(x+3, y1-2, x+7, y1-2);
		g.drawLine(x+5, y1-2, x+5, y1+2);

		g.drawLine(x+3, y2+2, x+7, y2+2);
		g.drawLine(x+5, y2-2, x+5, y2+2);

		}

	public boolean mouseHoverMoveRegion(int x, int y, Component comp, Flow flow)
		{
		Dimension dim=getBoundingBox(comp, flow);
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height &&
			getPolygon(dim).contains(x, y);
		}

	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("cond", FlowType.TBOOLEAN);
		types.put("true", null); //TODO should be the same
		types.put("false", null);
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", null);
		}

	public void editDialog(){}

	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	

	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		Boolean b=(Boolean)flow.getInputValue(this, exec, "cond");
		if(b)
			lastOutput.put("out", flow.getInputValue(this, exec, "true"));
		else
			lastOutput.put("out", flow.getInputValue(this, exec, "false"));
		}
	
	public Component getGUIcomponent(FlowPanel p){return null;}
	public int getGUIcomponentOffsetX(){return 0;}
	public int getGUIcomponentOffsetY(){return 0;}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"If",metaType,FlowUnitIf.class, icon,"Conditional flow"));
		}

	}
