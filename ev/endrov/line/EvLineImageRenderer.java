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

public class EvLineImageRenderer implements ImageWindowRenderer
	{
	public ImageWindowInterface w;
	

	static class Hover
		{
		EvLine ob;
		EvLine replaces;
		int i;
		boolean isAdded=false;
		//String id;
		}

	Hover activeAnnot=null;
	
	public EvLineImageRenderer(ImageWindowInterface w)
		{
		this.w=w;
		}

	
	
	
	public Collection<EvLine> getVisible()
		{
		//TODO: pick out
		Set<EvLine> lines=new HashSet<EvLine>();
		lines.addAll(w.getRootObject().getObjects(EvLine.class));
		if(activeAnnot!=null)
			{
			lines.add(activeAnnot.ob);
			lines.remove(activeAnnot.replaces);
			}
		return lines;
		}
	
	/**
	 * Render line
	 */
	public void draw(Graphics g)
		{
		EvDecimal curFrame=w.getFrame();
		for(EvLine ann:getVisible())
			{
			g.setColor(Color.GREEN);
			if(ann.pos.size()>0 && ann.pos.get(0).frame.equals(curFrame))
				{
				Vector2d last=w.transformPointW2S(new Vector2d(ann.pos.get(0).v.x,ann.pos.get(0).v.y));
				for(int i=1;i<ann.pos.size();i++)
					{
					Vector2d next=w.transformPointW2S(new Vector2d(ann.pos.get(i).v.x,ann.pos.get(i).v.y));
					g.drawLine((int)last.x, (int)last.y, (int)next.x, (int)next.y);
					last=next;
					}
				}
			EvDecimal curZ=w.getZ();
			for(int i=0;i<ann.pos.size();i++)
				if(ann.pos.get(i).frame.equals(curFrame))
					{
					Vector2d pos=w.transformPointW2S(new Vector2d(ann.pos.get(i).v.x,ann.pos.get(i).v.y));
					int midx=(int)pos.x;
					int midy=(int)pos.y;
					
					//Factor out this code if needed in other places
					if(ann.pos.get(i).v.z<curZ.doubleValue())
						g.drawOval(midx-4, midy-4, 8, 8);
					else if(ann.pos.get(i).v.z>curZ.doubleValue()) 
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
