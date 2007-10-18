package evplugin.roi;

import java.awt.*;
import java.util.*;

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
			
				int x1=-1,y1=-1,x2=100000,y2=100000;
				
				if(!box.regionX.all)
					{
					x1=(int)w.w2sx(box.regionX.start);
					x2=(int)w.w2sx(box.regionX.end);
					}
				if(!box.regionY.all)
					{
					y1=(int)w.w2sy(box.regionY.start);
					y2=(int)w.w2sy(box.regionY.end);
					}
				
				g.setColor(Color.WHITE);
				g.drawRect(x1, y1, x2-x1, y2-y1);
				}

			//Draw handles
			TreeMap<String,ROI.Handle> roimap=new TreeMap<String,ROI.Handle>();
			handleList.put(roi,roimap);
			for(ROI.Handle h:roi.getHandles())
				{
				roimap.put(h.getID(),h);
				int x=(int)w.w2sx(h.getX());
				int y=(int)w.w2sy(h.getY());
				g.setColor(Color.CYAN);
				g.drawRect(x-HANDLESIZE, y-HANDLESIZE, HANDLESIZE*2, HANDLESIZE*2);
				}
			
			}
		
		
		}

	
	
	
	}
