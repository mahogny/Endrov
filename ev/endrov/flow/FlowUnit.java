/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flow;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.*;

import org.jdom.Element;

import endrov.flowWindow.FlowView;

//think of how to do sub-flows later

/**
 * One component in a flow
 */
public abstract class FlowUnit
	{
	protected final static Font font = Font.decode("Dialog PLAIN");
	protected final static int fonth, fonta;
	protected final static FontMetrics fm;

	static
		{
		fm = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics()
				.getFontMetrics(font);
		fonth = fm.getHeight();
		fonta = fm.getAscent();
		}

	// TODO should consider working with swing components only, dropping the
	// custom paint method
	// editDialog() can be totally deprecated in that case

	/** Absolute coordinate, not relative to container */
	public int x, y;

	// is this the way to do it?
	boolean isTarget;
	boolean isContinuous;

	public FlowUnit()
		{
		}

	public abstract Dimension getBoundingBox(Component comp, Flow flow);

	public abstract void paint(Graphics g, FlowView panel, Component comp);

	/** Get types of flows in 
	 * @param flow TODO*/
	protected abstract void getTypesIn(Map<String, FlowType> types, Flow flow);

	/** Get types of flows out 
	 * @param flow TODO*/
	protected abstract void getTypesOut(Map<String, FlowType> types, Flow flow);

	
	public int getTypesInCount(Flow flow)
		{
		return getTypesIn(flow).size();
		}
	public int getTypesOutCount(Flow flow)
		{
		return getTypesOut(flow).size();
		}

	
	
	public Map<String, FlowType> getTypesIn(Flow flow)
		{
		Map<String, FlowType> map = new HashMap<String, FlowType>();
		getTypesIn(map, flow);
		return map;
		}

	public Map<String, FlowType> getTypesOut(Flow flow)
		{
		Map<String, FlowType> map = new HashMap<String, FlowType>();
		getTypesOut(map, flow);
		return map;
		}

	public abstract void evaluate(Flow flow, FlowExec exec) throws Exception;

	/**
	 * Evaluate flow top-bottom with this component as the top
	 */
	/*
	public void updateTopBottom(Flow flow, FlowExec exec) throws Exception
		{
		// TODO cache. how to say if a component is done?
		Set<FlowUnit> toUpdate = new HashSet<FlowUnit>();
		for (String arg : getTypesIn(flow).keySet())
			toUpdate.add(flow.getInputUnit(this, arg));
		for (FlowUnit u : toUpdate)
			if (u!=null)
				u.updateTopBottom(flow, exec);
		evaluate(flow, exec);
		}*/

	public abstract Collection<FlowUnit> getSubUnits(Flow flow);

	// is this the way to do it? keep them ordered as input arguments? separate in
	// & out?

	public abstract boolean mouseHoverMoveRegion(int x, int y, Component comp,
			Flow flow);

	/**
	 * Color to use for border
	 */
	protected Color getBorderColor(FlowView p)
		{
		if (p.selectedUnits.contains(this))
			return Color.MAGENTA;
		else
			return Color.BLACK;
		}

	/**
	 * Color to be used for text strings
	 */
	protected Color getTextColor()
		{
		return Color.BLACK;
		}

	/**
	 * For the purpose of placing a component, calculate mid position 
	 * TODO consider for swing integ to let another point be used
	 */
	public Point getMidPos(Component c, Flow flow)
		{
		Dimension dim = getBoundingBox(c, flow);
		return new Point(x+dim.width/2, y+dim.height/2);
		}

	public abstract String toXML(Element e);

	public abstract void fromXML(Element e);

	public abstract Component getGUIcomponent(FlowView p);

	public abstract int getGUIcomponentOffsetX();

	public abstract int getGUIcomponentOffsetY();

	public abstract void editDialog();
	


	public void checkNotNull(Object... list) throws Exception
		{
		for(Object o:list)
			if(o==null)
				throw new BadTypeFlowException("Check failed, null values");
		}
	
	public void errUnsupportedTypes(Map<String,Object> lastOutput) throws Exception
		{
//		StringBuffer sb;
		
		throw new BadTypeFlowException("Unsupported numerical types");
		}
	
	public void errNullValues(Map<String,Object> lastOutput) throws Exception
		{
		throw new BadTypeFlowException("Null values "+lastOutput);
		}
	

	public static double toDouble(Object o) throws Exception
		{
		if(o instanceof Double)
			return (Double)o;
		else if(o instanceof Integer)
			return (Integer)o;
		else throw new BadTypeFlowException("Not a numerical type "+o.getClass());
		}
	
	
	
	
	
	
	
	/**
	 * Draw connectors for a box-like flow unit
	 */
	protected void helperDrawConnectors(Graphics g, FlowView panel, Component comp, Dimension d)
		{
		int cntIn=1;
		if(cntIn<getTypesInCount(panel.getFlow())) cntIn=getTypesInCount(panel.getFlow());
		int i=0;
		for(Map.Entry<String, FlowType> entry:getTypesIn(panel.getFlow()).entrySet())
			{
			double py=y+(i+1)*d.height/(cntIn+1);
			panel.drawConnPointLeft(g, this, entry.getKey(), x, (int)py);
			i++;
			}
		
		int cntOut=1;
		if(cntOut<getTypesOutCount(panel.getFlow())) cntOut=getTypesOutCount(panel.getFlow());
		i=0;
		for(Map.Entry<String, FlowType> entry:getTypesOut(panel.getFlow()).entrySet())
			{
			double py=y+(i+1)*d.height/(cntOut+1);
			panel.drawConnPointRight(g, this, entry.getKey(), x+d.width, (int)py);
			i++;
			}
		
		}
	
	}
