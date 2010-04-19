/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.line;

import java.awt.*;
import java.util.*;
import javax.vecmath.*;

import endrov.imageWindow.*;
import endrov.util.EvDecimal;

public class EvLineRenderer implements ImageWindowRenderer
	{
	public ImageWindowInterface w;
	
	
	public EvLineRenderer(ImageWindowInterface w)
		{
		this.w=w;
		}

	
	public Collection<EvLine> getVisible()
		{
		//TODO: pick out
		return w.getRootObject().getObjects(EvLine.class);
		}
	
	/**
	 * Render nuclei
	 */
	public void draw(Graphics g)
		{
		EvDecimal curFrame=w.frameControl.getFrame();
		for(EvLine ann:getVisible())
			{
			//Draw the nucleus
			g.setColor(Color.GREEN);
			if(ann.pos.size()>0 && ann.pos.get(0).frame.equals(curFrame))
				{
				Vector2d last=w.transformW2S(new Vector2d(ann.pos.get(0).v.x,ann.pos.get(0).v.y));
				for(int i=1;i<ann.pos.size();i++)
					{
					Vector2d next=w.transformW2S(new Vector2d(ann.pos.get(i).v.x,ann.pos.get(i).v.y));
					g.drawLine((int)last.x, (int)last.y, (int)next.x, (int)next.y);
					last=next;
					}
				}
			EvDecimal curZ=w.frameControl.getModelZ();
//			w.s2wz(w.frameControl.getZ().doubleValue()); 
			for(int i=0;i<ann.pos.size();i++)
				if(ann.pos.get(i).frame.equals(curFrame))
					{
					Vector2d pos=w.transformW2S(new Vector2d(ann.pos.get(i).v.x,ann.pos.get(i).v.y));
					int midx=(int)pos.x;
					int midy=(int)pos.y;
					
					//Factor out this code if needed in other places
					if(ann.pos.get(i).v.z<curZ.doubleValue()) //TODO bd bad compare
						g.drawOval(midx-4, midy-4, 8, 8);
					else if(ann.pos.get(i).v.z>curZ.doubleValue()) //TODO bd bad compare
						g.drawOval(midx-3, midy-3, 6, 6);
					else
						{
						int size=3;
						g.drawLine(midx, midy-size, midx, midy+size);
						g.drawLine(midx-size, midy, midx+size, midy);
						}
					}
			}
		}
	
	
	public void dataChangedEvent()
		{
		}

	
	}
