package evplugin.roi;

import java.awt.*;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.imageWindow.*;
import evplugin.metadata.*;
import evplugin.roi.primitive.BoxROI;
import evplugin.ev.*;

public class ImageRendererROI implements ImageWindowRenderer
	{
	public ImageWindow w;
	
	
	public ImageRendererROI(ImageWindow w)
		{
		this.w=w;
		}


	/**
	 * Render nuclei
	 */
	public void draw(Graphics g)
		{
		for(MetaObject ob:w.getImageset().metaObject.values())
			if(ob instanceof ROI)
				drawROI(g, (ROI)ob);
		}
	
	
	public void dataChangedEvent()
		{
		/*
		NucLineage lin=getLineage();
		if(lin!=null)
			interpNuc=lin.getInterpNuc(w.frameControl.getFrame());			//maybe move this one later
			*/
		}


	private void drawROI(Graphics g, ROI roi)
		{
//		Graphics2D g2=(Graphics2D)g;
		
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
					x1=(int)box.regionX.start;
					x2=(int)box.regionX.end;
					}

				if(!box.regionY.all)
					{
					y1=(int)box.regionY.start;
					y2=(int)box.regionY.end;
					}

				
				g.setColor(Color.WHITE);
				g.drawRect(x1, y1, x2-x1, y2-y1);
				
				//w.w2sx(nuc.pos.x);
				}
			
			}
		
		
		}

	
/*
	private void drawNuc(Graphics g, NucPair nucPair, NucLineage.NucInterp nuc)
		{			
		String nucName=nucPair.getRight();
		
		if(nuc==null)
			{
			Log.printError("nuc==null", null);
			return;
			}
		
		//Z projection and visibility check
		double sor=projectSphere(nuc.pos.r, nuc.pos.z);
		if(sor>=0)
			{
			//Coordinate transformation
			double sox=w.w2sx(nuc.pos.x);
			double soy=w.w2sy(nuc.pos.y);
			
			//Pick color of nucleus
			Color nucColor;
			if(NucLineage.selectedNuclei.contains(nucPair))
				nucColor=Color.RED;
			else
				nucColor=Color.BLUE;
			
			//Draw the nucleus
			g.setColor(nucColor);
			if(nuc.frameBefore==null)
				{
				if(!nuc.hasParent)
					{
					//As this nucleus does not really exist here, it is drawn with stippled line
					for(int i=0;i<360/2;i+=2)
						g.drawArc((int)(sox-sor),(int)(soy-sor),(int)(2*sor),(int)(2*sor), i*20, 20);
					}
				}
			else
				{
				//Normal nucleus
				g.drawOval((int)(sox-sor),(int)(soy-sor),(int)(2*sor),(int)(2*sor));
				}
			
			
			//Mark keyframe
			if(nuc.isKeyFrame(w.frameControl.getFrame()))
				{
				g.drawLine((int)(sox-sor-1), (int)(soy), (int)(sox-sor+1), (int)(soy));
				g.drawLine((int)(sox+sor-1), (int)(soy), (int)(sox+sor+1), (int)(soy));					
				}
			
			//Mark endframe
			if(nuc.isEnd)
				{
				g.setColor(Color.BLACK);
				double f=Math.sqrt(1.0/2.0);
				g.drawLine((int)(sox-sor*f), (int)(soy-sor*f), (int)(sox+sor*f), (int)(soy+sor*f));
				g.drawLine((int)(sox-sor*f), (int)(soy+sor*f), (int)(sox+sor*f), (int)(soy-sor*f));
				}
			
			//Update hover
			if(w.mouseInWindow && (w.mouseCurX-sox)*(w.mouseCurX-sox) + (w.mouseCurY-soy)*(w.mouseCurY-soy)<sor*sor)
				NucLineage.currentHover=nucPair;
			
			//Draw name of nucleus. maybe do this last
			if(NucLineage.currentHover.equals(nucPair) || NucLineage.selectedNuclei.contains(nucPair))
				{
				g.setColor(Color.RED);

				g.drawString(nucName, (int)sox-g.getFontMetrics().stringWidth(nucName)/2, (int)soy-2);
				int crossSize=5;
				g.drawLine((int)sox-crossSize, (int)soy, (int)sox+crossSize, (int)soy);
				g.drawLine((int)sox, (int)soy, (int)sox, (int)soy+crossSize);
				}
			}

		}
	
	*/
	
	
	}
