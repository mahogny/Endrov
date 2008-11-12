package endrov.flow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.*;


import org.jdom.Element;

import endrov.flow.ui.FlowPanel;

//think of how to do sub-flows later


public abstract class FlowUnit
	{

	/** Absolute coordinate, not relative to container */
	public int x,y;

	public Map<String,Object> lastOutput=new HashMap<String, Object>();

	//private static Object staticSynch="";
	//private static int seqID=0;
	//private int ID;

	
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
	
	public abstract Dimension getBoundingBox();
	public abstract void paint(Graphics g, FlowPanel panel);
	
	/** Get types of flows in */
	public abstract Map<String, FlowType> getTypesIn();
	/** Get types of flows out */
	public abstract Map<String, FlowType> getTypesOut();
	
	public abstract void evaluate(Flow flow) throws Exception;

	/**
	 * Evaluate flow top-bottom with this component as the top 
	 */
	public void updateTopBottom(Flow flow) throws Exception
		{
		//TODO cache
		Set<FlowUnit> toUpdate=new HashSet<FlowUnit>();
		for(String arg:getTypesIn().keySet())
			toUpdate.add(flow.getInputUnit(this, arg));
		for(FlowUnit u:toUpdate)
			u.updateTopBottom(flow);
		evaluate(flow);
		}
	
	
	
//is this the way to do it? keep them ordered as input arguments? separate in & out?
	
	public abstract boolean mouseHoverMoveRegion(int x, int y);

	public abstract void editDialog();
	
	public abstract Collection<FlowUnit> getSubUnits(Flow flow);

	
	public abstract String storeXML(Element e);
		
	
	
	protected Color getBorderColor(FlowPanel p)
		{
		if(p.selectedUnits.contains(this))
			return Color.MAGENTA;
		else
			return Color.BLACK;
		}
	
	protected Color getTextColor()
		{
		return Color.BLACK;
		}

	public Point getMidPos()
		{
		Dimension dim=getBoundingBox();
		return new Point(x+dim.width/2,y+dim.height/2);
		}
	
	}
