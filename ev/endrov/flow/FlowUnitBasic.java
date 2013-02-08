/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flow;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Collections;

import javax.swing.ImageIcon;

import endrov.flowBasic.RendererFlowUtil;
import endrov.util.math.EvMathUtil;
import endrov.windowFlow.FlowView;

/**
 * Basic shape flow unit
 * @author Johan Henriksson
 *
 */
public abstract class FlowUnitBasic extends FlowUnit
	{
	protected static final int TEXTLEFT=0;
	protected static final int TEXTABOVE=1;
	
	protected int textPosition=TEXTLEFT;
	
	private static final int oneMargin=4;
	
	/**
	 * Name to be shown on box
	 */
	public abstract String getBasicShowName();
	public abstract ImageIcon getIcon();
	public abstract Color getBackground();
	protected boolean hasComponent=false; 
	
	public FlowUnitBasic()
		{
		}
	public FlowUnitBasic(boolean hasComponent)
		{
		this.hasComponent=hasComponent;
		}
	
	/**
	 * Calculate how big the box need to be
	 */
	public Dimension getBoundingBox(Component comp, Flow flow)
		{
		ImageIcon ico=getIcon();

		//Height by default should be at least to cover in/out pins
		int cnt=EvMathUtil.maxAllint(
				1,
				getTypesInCount(flow),
				getTypesOutCount(flow)
				);
		
		/*1;
		if(cnt<getTypesInCount(flow)) cnt=getTypesInCount(flow);
		if(cnt<getTypesOutCount(flow)) cnt=getTypesOutCount(flow);*/
		int h=fonth*cnt;
		
		//Title is on the left
		if(textPosition==TEXTLEFT)
			{
			int w=4; //Left margin
			
			//Text space
			w+=fm.stringWidth(getBasicShowName());
			
			//Icon space
			if(ico!=null)
				w+=ico.getIconWidth()+oneMargin;

			//Component space
			if(comp!=null)
				{
				if(fonta>h)
					h=fonta;
				if(ico!=null)
					if(ico.getIconHeight()>h)
						h=ico.getIconHeight();
				if(comp!=null)
					{
					if(comp.getHeight()>h)
						h=comp.getHeight();
					w+=comp.getWidth()+oneMargin;
					}
				}
			
			//Right,bottom margin
			w+=4;
			h+=2;
			return new Dimension(w,h);
			}
		else if(textPosition==TEXTABOVE)//Title is above
			{
			int w=oneMargin+fm.stringWidth(getBasicShowName())+oneMargin;
			int fh=fonta;
			if(ico!=null)
				{
				w+=ico.getIconWidth()+oneMargin;
				if(ico.getIconHeight()>fh)
					fh=ico.getIconHeight();
				}
			
			if(comp!=null)
				{
				int insideH=comp.getHeight()+fh;
				int cw=comp.getWidth()+oneMargin-1;
				
				
				if(cw>w)
					w=cw;
				if(insideH>h)
					h=insideH;
				}

			h++;
			return new Dimension(w,h);
			}
		else
			throw new RuntimeException("Bad value");
		}
	
	
	
	/**
	 * Draw basic colored box with text and optional custom component
	 */
	public void paint(Graphics g, FlowView panel, Component comp)
		{
		g.setColor(Color.blue);
		
		Dimension d=getBoundingBox(comp, panel.getFlow());

		RendererFlowUtil.drawBox(g, x, y, x+d.width-1, y+d.height-1, getBackground(), isSelected(panel));
		/*
		g.setColor(getBackground());
		g.fillRect(x,y,d.width,d.height);
		g.setColor(getBorderColor(panel));
		g.drawRect(x,y,d.width,d.height);*/
	
		ImageIcon ico=getIcon();
		int iconW=0;
		if(ico!=null)
			iconW=ico.getIconWidth()+4;

		g.setColor(getTextColor());
		if(textPosition==TEXTLEFT)
			{
			if(ico!=null)
				g.drawImage(ico.getImage(), x+oneMargin-1, y+(d.height-ico.getIconHeight())/2, null);
			g.drawString(getBasicShowName(), x+iconW+oneMargin, y+d.height/2+fonta/2-1);
			}
		else //TEXTABOVE
			{
			int fw=fm.stringWidth(getBasicShowName())+iconW;
			g.drawString(getBasicShowName(), x+(d.width-fw)/2+iconW, y+fonta);

			if(ico!=null)
				g.drawImage(ico.getImage(), x+(d.width-fw)/2, y+1, null);
			}

		helperDrawConnectors(g, panel, comp, d);
		}

	

	
	public boolean mouseHoverMoveRegion(int x, int y, Component comp, Flow flow)
		{
		Dimension dim=getBoundingBox(comp, flow);
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}

	public void editDialog(){}
	
	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}
	
	/**
	 * Trivial implementation, can be overridden
	 */
	public Component getGUIcomponent(FlowView p)
		{
		return null;
		}
	
	
	public int getGUIcomponentOffsetX(Component comp, Flow flow)
		{
		if(textPosition==TEXTABOVE)
			{
			int offset=getBoundingBox(comp, flow).width - comp.getWidth();
			return offset/2;
			}
		else
			{
			ImageIcon ico=getIcon();
			int iconW=0;
			if(ico!=null)
				iconW=ico.getIconWidth()+oneMargin;
			int fw=iconW+oneMargin+fm.stringWidth(getBasicShowName())+oneMargin;
			return fw;
			}
		}
	
	
	public int getGUIcomponentOffsetY(Component comp, Flow flow)
		{
		if(textPosition==TEXTABOVE)
			{
			int fh=fonta;
			ImageIcon ico=getIcon();
			if(ico!=null)
				if(ico.getIconHeight()>fh)
					fh=ico.getIconHeight();
			return fh;
			}
		else
			{
			int offset=getBoundingBox(comp, flow).height - comp.getHeight();
			return offset/2;
			
//			getGUIcomponent(p.getHeight())
//			return 1;
			}
		}
	
	public String getHelpArticle()
		{
		return "Misc flow operations";
		}

	}
