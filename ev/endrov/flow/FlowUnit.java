package endrov.flow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.*;

import endrov.flow.ui.FlowPanel;

//think of how to do sub-flows later


public abstract class FlowUnit
	{
	/** Absolute coordinate, not relative to container */
	public int x,y;
	
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
	
	
	
//is this the way to do it? keep them ordered as input arguments? separate in & out?
	
	public abstract boolean mouseHoverMoveRegion(int x, int y);
		
	
	
	protected Color getBorderColor()
		{
		return Color.BLACK;
		}
	
	
	}
