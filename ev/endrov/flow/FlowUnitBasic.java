package endrov.flow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jdom.Element;

import endrov.flow.ui.FlowPanel;

/**
 * Basic shape flow unit
 * @author Johan Henriksson
 *
 */
public abstract class FlowUnitBasic extends FlowUnit
	{
	public abstract String getBasicName();
//	public FlowUnit varUnit;

	
	public abstract Color getBackground();
	
	public Dimension getBoundingBox()
		{
		int w=fm.stringWidth(getBasicName());
		int cnt=1;
		if(cnt<getTypesInCount()) cnt=getTypesInCount();
		if(cnt<getTypesOutCount()) cnt=getTypesOutCount();
//		cnt++;
		int h=fonth*cnt;
		Dimension d=new Dimension(w+15,h);
		return d;
		}
	
	
	
	
	public void paint(Graphics g, FlowPanel panel)
		{
		g.setColor(Color.blue);
		
		
		Dimension d=getBoundingBox();

//	g.drawRect(x,y,d.width,d.height);
	

		g.setColor(getBackground());
		g.fillRect(x,y,d.width,d.height);
		g.setColor(getBorderColor());
		g.drawRect(x,y,d.width,d.height);

		g.drawString(getBasicName(), x+5, y+(d.height+fonta)/2);


//		drawConnPointRight(g,x+d.width,y+d.height/2);

		
		int cntIn=1;
		if(cntIn<getTypesInCount()) cntIn=getTypesInCount();
		int i=0;
		for(Map.Entry<String, FlowType> entry:getTypesIn().entrySet())
			{
			double py=y+(i+1)*d.height/(cntIn+1);
			panel.drawConnPointLeft(g, this, entry.getKey(), x, (int)py);
			i++;
			}

		
		int cntOut=1;
		if(cntOut<getTypesOutCount()) cntOut=getTypesOutCount();
		i=0;
		for(Map.Entry<String, FlowType> entry:getTypesOut().entrySet())
			{
			double py=y+(i+1)*d.height/(cntOut+1);
			panel.drawConnPointRight(g, this, entry.getKey(), x+d.width, (int)py);
			i++;
			}

		
//		int cnt=1;
//		if(cnt<getTypesInCount()) cnt=getTypesInCount();
		/*
		for(int i=0;i<getTypesInCount();i++)
			{
			double py=y+(i+0.5)*fonth;
			drawConnPointLeft(g, x, (int)py);
			}*/

		
		
		}

	
	public int getTypesInCount()
		{
		return getTypesIn().size();
		}
	public int getTypesOutCount()
		{
		return getTypesOut().size();
		}

	
	public boolean mouseHoverMoveRegion(int x, int y)
		{
		Dimension dim=getBoundingBox();
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}

	public void editDialog(){}
	
	public void storeXML(Element e){}
	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}
	
	
	
	
	}
