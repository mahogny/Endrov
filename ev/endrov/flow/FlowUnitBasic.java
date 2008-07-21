package endrov.flow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

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
		for(int i=0;i<getTypesInCount();i++)
			{
			double py=y+(i+1)*d.height/(cntIn+1);
			drawConnPointLeft(g, x, (int)py);
			}

		
		int cntOut=1;
		if(cntOut<getTypesOutCount()) cntOut=getTypesOutCount();
		for(int i=0;i<getTypesOutCount();i++)
			{
			double py=y+(i+1)*d.height/(cntOut+1);
			drawConnPointRight(g, x+d.width, (int)py);
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

	
	
	
	
	
	
	
	
	
	
	}
