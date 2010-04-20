/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.nucImage;

import java.awt.*;
import java.util.*;
import javax.vecmath.*;


import endrov.basicWindow.*;
import endrov.data.EvSelection;
import endrov.ev.*;
import endrov.imageWindow.*;
import endrov.nuc.NucLineage;
import endrov.nuc.NucSel;
import endrov.util.EvDecimal;

/**
 * Image window renderer of nuclei
 * @author Johan Henriksson
 *
 */
public class NucImageRenderer implements ImageWindowRenderer
	{
	public ImageWindow w;
	
	/** Interpolated nuclei */
	public Map<NucSel, NucLineage.NucInterp> interpNuc=new HashMap<NucSel, NucLineage.NucInterp>();

	/** Nuclei currently being moved */
	public NucSel modifyingNucName=null;
	
	
	public NucImageRenderer(ImageWindow w)
		{
		this.w=w;
		}


	
	public Collection<NucLineage> getVisibleLineages()
		{
		//TODO: pick out
		return NucLineage.getLineages(w.getRootObject());
		}
	

	/**
	 * Render nuclei
	 */
	public void draw(Graphics g)
		{
		//Update hover
		NucSel lastHover=NucLineage.currentHover;			
		if(w.mouseInWindow)
			NucLineage.currentHover=new NucSel();
	
		EvDecimal currentFrame=w.getFrame();//frameControl.getFrame();
		
		interpNuc.clear();
		for(NucLineage lin:getVisibleLineages())
			{
			Map<NucSel, NucLineage.NucInterp> interpNucPart=lin.getInterpNuc(currentFrame);
			interpNuc.putAll(interpNucPart);
			}
		for(NucSel nucPair:interpNuc.keySet())
			{
			NucLineage.NucInterp nuc=interpNuc.get(nucPair);
			drawNuc(g,nucPair,nuc,currentFrame);
			}

		if(!lastHover.equals(NucLineage.currentHover))
			BasicWindow.updateWindows(w);
		}
	
	
	public void dataChangedEvent()
		{
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
	 * Draw ovaloid axis'
	 */
	private void drawAxis(Graphics g, NucSel nucPair, NucLineage.NucInterp nuc, Vector2d so)
		{
		if(nuc.pos.ovaloidAxisLength!=null)
			{
			for(int i=0;i<nuc.pos.ovaloidAxisLength.length;i++)
				{
				double len=w.scaleW2s(nuc.pos.ovaloidAxisLength[i]);
				//double len=w.scaleW2s(1);
				Vector3d v=nuc.pos.ovaloidAxisVec[i];
				g.drawLine((int)(so.x), (int)(so.y), (int)(so.x+v.x*len), (int)(so.y+v.y*len));
				}
			}
		}
	
	/**
	 * Draw a single nucleus
	 */
	private void drawNuc(Graphics g, NucSel nucPair, NucLineage.NucInterp nuc, EvDecimal currentFrame)
		{			
		String nucName=nucPair.snd();
		
		if(nuc==null)
			{
			EvLog.printError("nuc==null", null);
			return;
			}
		
		//Z projection and visibility check
		double sor=projectSphere(nuc.pos.r, nuc.pos.z);
		if(sor>=0)
			{
			//Coordinate transformation
			Vector2d so=w.transformW2S(new Vector2d(nuc.pos.x,nuc.pos.y));
			
			

			//Draw division lines
			g.setColor(Color.YELLOW);
			EvDecimal lastFrame=nucPair.getNuc().pos.lastKey();//getLastFrame();
			if(lastFrame!=null && lastFrame.lessEqual(currentFrame))
				{
				for(String child:nucPair.getNuc().child)
					{
					NucLineage.Nuc nchild=nucPair.fst().nuc.get(child);
					if(!nchild.pos.isEmpty())
						{
						EvDecimal firstFrame=nchild.pos.firstKey();
						if(!nchild.pos.isEmpty() && firstFrame.greaterEqual(currentFrame))
							{
							NucLineage.NucPos cpos=nchild.pos.get(firstFrame);
							Vector2d childso=w.transformW2S(new Vector2d(cpos.x,cpos.y));
							g.drawLine((int)so.x,(int)so.y, (int)childso.x,(int)childso.y);
							}
						}
					}
				}
			
			
			//Pick color of nucleus
			Color nucColor;
			if(EvSelection.isSelected(nucPair))
				nucColor=Color.RED;
			else
				nucColor=Color.BLUE;
			
			//Draw the nucleus and check if it is visible
			g.setColor(nucColor);
			boolean isVisible=false;
			if(nuc.frameBefore==null)
				{
				if(!nuc.hasParent)
					{
					//As this nucleus does not really exist here, it is drawn with stippled line
					for(int i=0;i<360/2;i+=2)
						g.drawArc((int)(so.x-sor),(int)(so.y-sor),(int)(2*sor),(int)(2*sor), i*20, 20);
					isVisible=true;
					drawAxis(g, nucPair, nuc, so);
					}
				}
			else
				{
				//Normal nucleus
				g.drawOval((int)(so.x-sor),(int)(so.y-sor),(int)(2*sor),(int)(2*sor));
				isVisible=true;
				drawAxis(g, nucPair, nuc, so);
				}
			


			
			//If it is visible then draw more things
			if(isVisible)
				{
				//Mark keyframe
				if(nuc.isKeyFrame(w.getFrame()))
					{
					g.drawLine((int)(so.x-sor-1), (int)(so.y), (int)(so.x-sor+1), (int)(so.y));
					g.drawLine((int)(so.x+sor-1), (int)(so.y), (int)(so.x+sor+1), (int)(so.y));					
					}
				
				//Mark endframe
				if(nuc.isEnd)
					{
					g.setColor(Color.BLACK);
					double f=Math.sqrt(1.0/2.0);
					g.drawLine((int)(so.x-sor*f), (int)(so.y-sor*f), (int)(so.x+sor*f), (int)(so.y+sor*f));
					g.drawLine((int)(so.x-sor*f), (int)(so.y+sor*f), (int)(so.x+sor*f), (int)(so.y-sor*f));
					}
				
				//Update hover
				if(w.mouseInWindow && (w.mouseCurX-so.x)*(w.mouseCurX-so.x) + (w.mouseCurY-so.y)*(w.mouseCurY-so.y)<sor*sor)
					NucLineage.currentHover=nucPair;
				
				//Draw name of nucleus. maybe do this last
				if(NucLineage.currentHover.equals(nucPair) || EvSelection.isSelected(nucPair))
					{
					g.setColor(Color.RED);
					g.drawString(nucName, (int)so.x-g.getFontMetrics().stringWidth(nucName)/2, (int)so.y-2);
					int crossSize=5;
					g.drawLine((int)so.x-crossSize, (int)so.y, (int)so.x+crossSize, (int)so.y);
					g.drawLine((int)so.x, (int)so.y, (int)so.x, (int)so.y+crossSize);
					}
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
		//double wz=w.frameControl.getZ().doubleValue();//w.s2wz(w.frameControl.getZ().doubleValue());
		double wz=w.getModelZ().doubleValue(); 
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
			EvDecimal framei=w.getFrame(); //frameControl.getFrame();
			if(n.pos.get(framei)==null)
				{
				NucLineage.NucInterp inter=n.interpolatePos(framei);
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
			return modifyingNucName.fst().getCreateNuc(modifyingNucName.snd());
		}
	
	public NucLineage getModifyingLineage()
		{
		if(modifyingNucName==null)
			return null;
		else
			return modifyingNucName.fst();
		}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				NucImageRenderer r=new NucImageRenderer(w);
				w.addImageWindowTool(new NucImageTool(w,r));
				w.addImageWindowRenderer(r);
				}
			});
		}

	}
