package evplugin.roi;

import java.awt.*;
import java.util.*;
import javax.vecmath.*;

//import evplugin.basicWindow.*;
import evplugin.data.*;
import evplugin.imageWindow.*;
import evplugin.roi.primitive.BoxROI;

/**
 * Render ROI in Image Window
 * 
 * @author Johan Henriksson
 */
public class ImageRendererROI implements ImageWindowRenderer
	{
	public static final int HANDLESIZE=3;

	public ImageWindow w;
	public TreeMap<ROI, TreeMap<String,ROI.Handle>> handleList=new TreeMap<ROI, TreeMap<String,ROI.Handle>>();
	
	
	public ImageRendererROI(ImageWindow w)
		{
		this.w=w;
		}


	/**
	 * Render nuclei
	 */
	public void draw(Graphics g)
		{
		for(EvObject ob:w.getImageset().metaObject.values())
			if(ob instanceof ROI)
				drawROI(g, (ROI)ob);
		}
	
	
	public void dataChangedEvent()
		{
		}


	private void drawROI(Graphics g, ROI roi)
		{
		handleList.clear();

		double frame=w.frameControl.getFrame();
		int z=w.frameControl.getZ();
		String channel=w.getCurrentChannelName();
		
		if(roi.imageInRange(channel, frame, z))
			{
			if(roi instanceof BoxROI)
				{
				BoxROI box=(BoxROI)roi;
			
				double x1=-1000,y1=-1000,x2=1000,y2=1000;
				if(!box.regionX.all)
					{
					x1=box.regionX.start;
					x2=box.regionX.end;
					}
				if(!box.regionY.all)
					{
					y1=box.regionY.start;
					y2=box.regionY.end;
					}
				Vector2d ul=w.transformW2S(new Vector2d(x1,y1));
				Vector2d ll=w.transformW2S(new Vector2d(x1,y2));
				Vector2d ur=w.transformW2S(new Vector2d(x2,y1));
				Vector2d lr=w.transformW2S(new Vector2d(x2,y2));
				
				g.setColor(Color.WHITE);
				g.drawLine((int)ul.x, (int)ul.y, (int)ll.x, (int)ll.y);
				g.drawLine((int)ur.x, (int)ur.y, (int)lr.x, (int)lr.y);
				g.drawLine((int)ul.x, (int)ul.y, (int)ur.x, (int)ur.y);
				g.drawLine((int)ll.x, (int)ll.y, (int)lr.x, (int)lr.y);
				}

			//Draw handles
			TreeMap<String,ROI.Handle> roimap=new TreeMap<String,ROI.Handle>();
			handleList.put(roi,roimap);
			for(ROI.Handle h:roi.getHandles())
				{
				roimap.put(h.getID(),h);
				Vector2d xy=w.transformW2S(new Vector2d(h.getX(), h.getY()));
				g.setColor(Color.CYAN);
				g.drawRect((int)xy.x-HANDLESIZE, (int)xy.y-HANDLESIZE, HANDLESIZE*2, HANDLESIZE*2);
				//pixels huge. rather, there is a need for a general transform function. Scaling functions has to disappear
				/*
				w.transformOverlay((Graphics2D)g);		
				g.drawRect((int)h.getX()-HANDLESIZE, (int)h.getY()-HANDLESIZE, HANDLESIZE*2, HANDLESIZE*2);
				w.untransformOverlay((Graphics2D)g);		
				*/
				}
			
			}
		
		
		}

	
	
	
	}
