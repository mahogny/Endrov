package endrov.flow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.*;

public class FlowUnit
	{
	int x,y;
	
	
	public Dimension getBoundingBox()
		{
		Dimension d=new Dimension(30,30);
		return d;
		}
	
	
	public void paint(Graphics g, FlowPanel panel)
		{
		g.setColor(Color.blue);
		g.drawRect(x-panel.cameraX,y-panel.cameraY,30,30);
		
		
		
		}
	
	
	public SortedMap<String, FlowType> getInputTypes()
		{
		return null;
		}
	public SortedMap<String, FlowType> getOutputTypes()
		{
		return null;
		}

	
	
	}
