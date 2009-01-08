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

import endrov.basicWindow.FlowExec;
import endrov.flow.ui.FlowPanel;

//think of how to do sub-flows later

/**
 * One component in a flow
 */
public abstract class FlowUnit
	{
	//TODO should consider working with swing components only, dropping the custom paint method
	//editDialog() can be totally deprecated in that case
	
	/** Absolute coordinate, not relative to container */
	public int x,y;


	
	protected final static Font font=Font.decode("Dialog PLAIN");
	protected final static int fonth,fonta;
	protected final static FontMetrics fm;

	static
		{
		fm=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB).getGraphics().getFontMetrics(font);
		fonth=fm.getHeight();
		fonta=fm.getAscent();
		}
	
	
	//is this the way to do it?
	boolean isTarget;
	boolean isContinuous;
	
	public FlowUnit()
		{
/*
		synchronized (staticSynch)
			{
			ID=seqID;
			seqID++;
			}*/
		}
	
	/**
	 * Get unique ID for this unit. Never changes once component is made.
	 */
	/*
	public int getUniqueID()
		{
		return ID;
		}*/
	
	public abstract Dimension getBoundingBox(Component comp);
	public abstract void paint(Graphics g, FlowPanel panel, Component comp);
	
	/** Get types of flows in */
	public abstract Map<String, FlowType> getTypesIn();
	/** Get types of flows out */
	public abstract Map<String, FlowType> getTypesOut();
	
	public abstract void evaluate(Flow flow,FlowExec exec) throws Exception;

	/**
	 * Evaluate flow top-bottom with this component as the top 
	 */
	public void updateTopBottom(Flow flow, FlowExec exec) throws Exception
		{
		//TODO cache
		Set<FlowUnit> toUpdate=new HashSet<FlowUnit>();
		for(String arg:getTypesIn().keySet())
			toUpdate.add(flow.getInputUnit(this, arg));
		for(FlowUnit u:toUpdate)
			if(u!=null)
				u.updateTopBottom(flow,exec);
		evaluate(flow,exec);
		}

	public abstract Collection<FlowUnit> getSubUnits(Flow flow);

	
	
//is this the way to do it? keep them ordered as input arguments? separate in & out?
	
	public abstract boolean mouseHoverMoveRegion(int x, int y, Component comp);

	

	
		
	
	/**
	 * Color to use for border
	 */
	protected Color getBorderColor(FlowPanel p)
		{
		if(p.selectedUnits.contains(this))
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
	public Point getMidPos(Component c)
		{
		Dimension dim=getBoundingBox(c);
		return new Point(x+dim.width/2,y+dim.height/2);
		}
	
	
	public abstract String toXML(Element e);
	public abstract void fromXML(Element e);
	
	
	public abstract Component getGUIcomponent(FlowPanel p);
	public abstract int getGUIcomponentOffsetX();
	public abstract int getGUIcomponentOffsetY();
	public abstract void editDialog();
	}
