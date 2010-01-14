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
import java.util.Map;

import javax.swing.ImageIcon;

import endrov.flow.ui.FlowPanel;

/**
 * Basic shape flow unit
 * @author Johan Henriksson
 *
 */
public abstract class FlowUnitBasic extends FlowUnit
	{
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
	
	public Dimension getBoundingBox(Component comp, Flow flow)
		{
		ImageIcon ico=getIcon();
		
		int w=fm.stringWidth(getBasicShowName());
		if(ico!=null)
			w+=ico.getIconWidth()+2;
		int cnt=1;
		if(cnt<getTypesInCount(flow)) cnt=getTypesInCount(flow);
		if(cnt<getTypesOutCount(flow)) cnt=getTypesOutCount(flow);
//		cnt++;
		int h=fonth*cnt;
		Dimension d=new Dimension(w+15,h);
		if(comp!=null)
			{
			d.height+=comp.getHeight();
			int cw=comp.getWidth();
			if(cw>d.width)
				d.width=cw;
			}
		return d;
		}
	
	
	
	
	public void paint(Graphics g, FlowPanel panel, Component comp)
		{
		g.setColor(Color.blue);
		
		
		Dimension d=getBoundingBox(comp, panel.getFlow());

//	g.drawRect(x,y,d.width,d.height);
	
		g.setColor(getBackground());
		g.fillRect(x,y,d.width,d.height);
		g.setColor(getBorderColor(panel));
		g.drawRect(x,y,d.width,d.height);
	
		int iconW=0;
		ImageIcon ico=getIcon();
		if(ico!=null)
			{
			iconW=ico.getIconWidth()+2;
			g.drawImage(ico.getImage(), x+3, y+(d.height-ico.getIconHeight())/2, null);
			}
		
		g.setColor(getTextColor());
		g.drawString(getBasicShowName(), x+iconW+5, y+(d.height+fonta)/2);


//		drawConnPointRight(g,x+d.width,y+d.height/2);

		
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

	
	public int getTypesInCount(Flow flow)
		{
		return getTypesIn(flow).size();
		}
	public int getTypesOutCount(Flow flow)
		{
		return getTypesOut(flow).size();
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
	public Component getGUIcomponent(FlowPanel p)
		{
		return null;
		}
	public int getGUIcomponentOffsetX()
		{
		return 0;
		}
	public int getGUIcomponentOffsetY()
		{
		return fonta;
		}
	
	}
