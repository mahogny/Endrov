package evplugin.line;

import java.awt.*;
import java.util.*;
import javax.vecmath.*;

import evplugin.imageWindow.*;

public class EvLineRenderer implements ImageWindowRenderer
	{
	public ImageWindow w;
	
	
	public EvLineRenderer(ImageWindow w)
		{
		this.w=w;
		}

	
	public Collection<EvLine> getVisible()
		{
		//TODO: pick out
		return EvLine.getObjects(w.getImageset());
		}
	
	/**
	 * Render nuclei
	 */
	public void draw(Graphics g)
		{
		int curFrame=(int)w.frameControl.getFrame();
		for(EvLine ann:getVisible())
			{
			//Draw the nucleus
			g.setColor(Color.GREEN);
			if(ann.pos.size()>0 && ann.pos.get(0).w==curFrame)
				{
				Vector2d last=w.transformW2S(new Vector2d(ann.pos.get(0).x,ann.pos.get(0).y));
				for(int i=1;i<ann.pos.size();i++)
					{
					Vector2d next=w.transformW2S(new Vector2d(ann.pos.get(i).x,ann.pos.get(i).y));
					g.drawLine((int)last.x, (int)last.y, (int)next.x, (int)next.y);
					last=next;
					}
				}
			double curZ=w.s2wz(w.frameControl.getZ());
			for(int i=0;i<ann.pos.size();i++)
				if(ann.pos.get(i).w==curFrame)
					{
					Vector2d pos=w.transformW2S(new Vector2d(ann.pos.get(i).x,ann.pos.get(i).y));
					int midx=(int)pos.x;
					int midy=(int)pos.y;
					
					//Factor out this code if needed in other places
					if(ann.pos.get(i).z<curZ)
						g.drawOval(midx-4, midy-4, 8, 8);
					else if(ann.pos.get(i).z>curZ)
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
