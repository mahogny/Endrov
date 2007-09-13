package evplugin.nuc;

import java.awt.*;
import java.util.*;

import evplugin.basicWindow.*;
import evplugin.imageWindow.*;
import evplugin.metadata.*;
import evplugin.ev.*;

public class NucImageRenderer implements ImageWindowRenderer
	{
	public ImageWindow w;
	
	/** Interpolated nuclei */
	public Map<String, NucLineage.NucInterp> interpNuc=null;
	
	/** Nuclei currently being moved */
	public String modifyingNucName=null;
	
	
	public NucImageRenderer(ImageWindow w)
		{
		this.w=w;
		}

	/**
	 * Get the lineage object from window
	 */
	public NucLineage getLineage()
		{
		for(MetaObject ob:w.getImageset().metaObject.values())
			if(ob instanceof NucLineage)
				return (NucLineage)ob;
		return null;
		}

	/**
	 * Render nuclei
	 */
	public void draw(Graphics g)
		{
		//Update hover
		String lastHover=NucLineage.currentHover;			
		if(w.mouseInWindow)
			NucLineage.currentHover="";
		
		NucLineage lin=getLineage();
		if(lin!=null)
			{
			interpNuc=lin.getInterpNuc(w.frameControl.getFrame());			//maybe move this one later
			for(String nucName:interpNuc.keySet())
				{
				NucLineage.NucInterp nuc=interpNuc.get(nucName);
				drawNuc(g,nucName,nuc);
				}
			}
		if(!lastHover.equals(NucLineage.currentHover))
			BasicWindow.updateWindows(w);
		}
	
	
	public void dataChangedEvent()
		{
		/*
		NucLineage lin=getLineage();
		if(lin!=null)
			interpNuc=lin.getInterpNuc(w.frameControl.getFrame());			//maybe move this one later
			*/
		}

	
	/**
	 * Currently modified nucleus is finalized. Commit changes.
	 */
	public void commitModifyingNuc()
		{
		modifyingNucName=null;
		BasicWindow.updateWindows();
		}

	
	/**
	 * Draw a single nucleus
	 */
	private void drawNuc(Graphics g, String nucName, NucLineage.NucInterp nuc)
		{			
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
			if(NucLineage.selectedNuclei.contains(nucName))
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
				NucLineage.currentHover=nucName;
			
			//Draw name of nucleus. maybe do this last
			if(NucLineage.currentHover.equals(nucName) || NucLineage.selectedNuclei.contains(nucName))
				{
				g.setColor(Color.RED);
				g.drawString(nucName, 
						(int)sox-g.getFontMetrics().stringWidth(nucName)/2, 
						(int)soy+g.getFontMetrics().getHeight()/2);
				}
			}

		}
	
	
	/**
	 * Project sphere onto plane
	 * @param r Radius
	 * @param z Relative z
	 * @return Projected radius in pixels
	 */
	private double projectSphere(double r, double z)
		{
		//Currently assumes resx=resy. Maybe this should be specified harder?
		double wz=w.s2wz(w.frameControl.getZ());
		double tf=r*r-(z-wz)*(z-wz);
		if(tf>0)
			{
			double wpr=Math.sqrt(tf);
			return w.scaleW2s(wpr);	
			}
		else
			return -1;
		}
	
	
	/** Get pos from modifying nucleus. Also creates a new position!!! (is this a good idea?) */
	public NucLineage.NucPos getModifyingNucPos()
		{
		NucLineage.Nuc n=getModifyingNuc();
		if(n==null)
			return null;
		else
			{
			int framei=(int)w.frameControl.getFrame();
			if(n.pos.get(framei)==null)
				{
				NucLineage.NucInterp inter=n.interpolate(framei);
				n.pos.put(framei, new NucLineage.NucPos(inter.pos));
				//apoptotic info
				}
			return n.pos.get(framei);
			}
		}

	/** Get modifying nucleus */
	public NucLineage.Nuc getModifyingNuc()
		{
		if(modifyingNucName==null)
			return null;
		else
			return getLineage().getNucCreate(modifyingNucName);
		}
	
	}
