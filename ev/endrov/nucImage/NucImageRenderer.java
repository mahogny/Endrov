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
import endrov.imageWindow.*;
import endrov.nuc.NucCommonUI;
import endrov.nuc.NucLineage;
import endrov.nuc.NucSel;
import endrov.undo.UndoOpBasic;
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

	/**
	 * Nuclei currently being moved 
	 */
	NucSel modifyingNucSel=null;
	
	/**
	 * Original position of modified nucleus.
	 * Saving the entire nucleus is a bit overkill but it avoids some irritating corner cases
	 * e.g. when a frame is changed as a nucleus is moved
	 * 
	 */
	NucLineage.Nuc modifiedNuc=null;
	
	/**
	 * The nuclei might not be modified yet; then it should not be commited because this causes
	 * a useless undo item to be created
	 */
	boolean hasReallyModified;

	/**
	 * If there are icons (buttons) for interacting with a nuclei, which nucleus and where are they? 
	 */
	NucSel iconsForNuc=null;
	Rectangle rectIconCenterZ=null;
	Rectangle rectIconChangeRadius=null;

	
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
		rectIconCenterZ=null;
		rectIconChangeRadius=null;
		iconsForNuc=null;
		
		
		//Update hover
		NucSel lastHover=NucCommonUI.currentHover;			
		if(w.mouseInWindow)
			NucCommonUI.currentHover=NucCommonUI.emptyHover;
	
		EvDecimal currentFrame=w.getFrame();
		
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

		if(!lastHover.equals(NucCommonUI.currentHover))
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
		//Only commit if something has changed
		if(hasReallyModified)
			{
			hasReallyModified=false;
			final NucLineage lin=modifyingNucSel.fst();
			final String name=modifyingNucSel.snd();
			final NucLineage.Nuc currentNuc=modifyingNucSel.getNuc().clone();
			final NucLineage.Nuc lastNuc=modifiedNuc; 
	
			new UndoOpBasic("Modify keyframe for "+modifyingNucSel.snd())
				{
				public void redo()
					{
					lin.nuc.put(name, currentNuc);
					BasicWindow.updateWindows();
					}
	
				public void undo()
					{
					lin.nuc.put(name, lastNuc);
					BasicWindow.updateWindows();
					}
				}.execute();
			}

		modifyingNucSel=null;
		modifiedNuc=null;
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
				Vector3d v=nuc.pos.ovaloidAxisVec[i];
				g.drawLine((int)(so.x), (int)(so.y), (int)(so.x+v.x*len), (int)(so.y+v.y*len));
				}
			}
		}
	
	/**
	 * Draw a single nucleus
	 */
	private void drawNuc(Graphics g, NucSel sel, NucLineage.NucInterp nuc, EvDecimal currentFrame)
		{			
		String nucName=sel.snd();

		//Z projection and visibility check
		double sor=projectSphere(nuc.pos.r, nuc.pos.z);
		boolean isVisible=false;
		if(sor>=0)
			{
			//Coordinate transformation
			Vector2d so=w.transformPointW2S(new Vector2d(nuc.pos.x,nuc.pos.y));
			
			//Draw division lines
			g.setColor(Color.YELLOW);
			EvDecimal lastFrame=sel.getNuc().pos.lastKey();
			if(lastFrame!=null && lastFrame.lessEqual(currentFrame))
				{
				for(String child:sel.getNuc().child)
					{
					NucLineage.Nuc nchild=sel.fst().nuc.get(child);
					if(!nchild.pos.isEmpty())
						{
						EvDecimal firstFrame=nchild.pos.firstKey();
						if(!nchild.pos.isEmpty() && firstFrame.greaterEqual(currentFrame))
							{
							NucLineage.NucPos cpos=nchild.pos.get(firstFrame);
							Vector2d childso=w.transformPointW2S(new Vector2d(cpos.x,cpos.y));
							g.drawLine((int)so.x,(int)so.y, (int)childso.x,(int)childso.y);
							}
						}
					}
				}
			
			//Pick color of nucleus
			Color nucColor;
			if(EvSelection.isSelected(sel))
				nucColor=Color.RED;
			else
				nucColor=Color.BLUE;
			
			//Draw the nucleus and check if it is visible
			g.setColor(nucColor);
			if(nuc.frameBefore==null)
				{
				if(!nuc.hasParent)
					{
					//As this nucleus does not really exist here, it is drawn with stippled line
					for(int i=0;i<360/2;i+=2)
						g.drawArc((int)(so.x-sor),(int)(so.y-sor),(int)(2*sor),(int)(2*sor), i*20, 20);
					isVisible=true;
					drawAxis(g, sel, nuc, so);
					}
				}
			else
				{
				//Normal nucleus
				g.drawOval((int)(so.x-sor),(int)(so.y-sor),(int)(2*sor),(int)(2*sor));
				isVisible=true;
				drawAxis(g, sel, nuc, so);
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
					NucCommonUI.currentHover=sel;
				
				//Draw name of nucleus. maybe do this last
				if(NucCommonUI.currentHover.equals(sel) || EvSelection.isSelected(sel))
					{
					g.setColor(Color.RED);
					g.drawString(nucName, (int)so.x-g.getFontMetrics().stringWidth(nucName)/2, (int)so.y-2);
					int crossSize=5;
					g.drawLine((int)so.x-crossSize, (int)so.y, (int)so.x+crossSize, (int)so.y);
					g.drawLine((int)so.x, (int)so.y, (int)so.x, (int)so.y+crossSize);
					}
				}
			}
		else
			sor=0;

		
		

		
		//Draw tool icons if this nucleus is selected (and only for one of them) and it is visible
		if(EvSelection.isSelected(sel) && iconsForNuc==null)
			{

			//Duplicated code; bad but improves performance. Rewrite in a better way later
			if(nuc.frameBefore==null)
				{
				if(!nuc.hasParent)
					isVisible=true;
				}
			else
				isVisible=true;

			
			if(isVisible)
				{
				Vector2d so=w.transformPointW2S(new Vector2d(nuc.pos.x,nuc.pos.y));
				int iconSize=20;

				g.setColor(Color.WHITE);

				//Change radius icon
				int iconRx=(int)(so.x-sor/Math.sqrt(2)-iconSize);
				int iconRy=(int)(so.y+sor/Math.sqrt(2));
				g.drawRect(iconRx, iconRy, iconSize, iconSize);
				g.drawOval(iconRx+2, iconRy+2, iconSize-4, iconSize-4);
				int mx=iconRx+iconSize/2;
				g.drawLine(mx, iconRy+4, mx, iconRy+iconSize-4);
				g.drawLine(mx, iconRy+4, mx+2, iconRy+4+2);
				g.drawLine(mx, iconRy+4, mx-2, iconRy+4+2);
				g.drawLine(mx, iconRy+iconSize-4, mx+2, iconRy+iconSize-4-2);
				g.drawLine(mx, iconRy+iconSize-4, mx-2, iconRy+iconSize-4-2);
				
				rectIconChangeRadius=new Rectangle(iconRx, iconRy, iconSize, iconSize);
				
				
				//Set Z icon
				int iconCx=(int)(so.x+sor/Math.sqrt(2));
				int iconCy=(int)(so.y+sor/Math.sqrt(2));
				g.drawRect(iconCx, iconCy, iconSize, iconSize);
				g.drawOval(iconCx+4, iconCy+4, iconSize-8, iconSize-8);
				g.drawLine(iconCx+2, iconCy+iconSize/2-2, iconCx+iconSize-2, iconCy+iconSize/2-2);
				rectIconCenterZ=new Rectangle(iconCx, iconCy, iconSize, iconSize);
				
				iconsForNuc=sel;
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
		double wz=w.getZ().doubleValue(); 
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
			EvDecimal framei=w.getFrame();
			if(n.pos.get(framei)==null)
				{
				NucLineage.NucInterp inter=n.interpolatePos(framei);
				n.pos.put(framei, inter.pos.clone());
				}
			return n.pos.get(framei);
			}
		}
	
	
	/**
	 * Get modifying nucleus 
	 */
	public NucLineage.Nuc getModifyingNuc()
		{
		if(modifyingNucSel==null)
			return null;
		else
			return modifyingNucSel.fst().getCreateNuc(modifyingNucSel.snd());
		}
	
	public NucLineage getModifyingLineage()
		{
		if(modifyingNucSel==null)
			return null;
		else
			return modifyingNucSel.fst();
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
