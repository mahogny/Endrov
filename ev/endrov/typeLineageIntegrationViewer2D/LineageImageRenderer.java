/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeLineageIntegrationViewer2D;

import java.awt.*;
import java.util.*;
import javax.vecmath.*;


import endrov.data.EvSelection;
import endrov.data.EvSelection.EvSelectable;
import endrov.gui.undo.UndoOpBasic;
import endrov.gui.window.EvBasicWindow;
import endrov.typeLineage.Lineage;
import endrov.typeLineage.LineageSelParticle;
import endrov.util.math.EvDecimal;
import endrov.windowViewer2D.*;

/**
 * Image window renderer of lineage
 * @author Johan Henriksson
 *
 */
public class LineageImageRenderer implements Viewer2DRenderer
	{
	public Viewer2DWindow w;
	
	/** Interpolated particles */
	public Map<LineageSelParticle, Lineage.InterpolatedParticle> interpParticle=new HashMap<LineageSelParticle, Lineage.InterpolatedParticle>();

	/**
	 * Particle currently being moved 
	 */
	LineageSelParticle modifyingParticleSelected=null;
	
	/**
	 * Original position of modified particle.
	 * Saving the entire particle is a bit overkill but it avoids some irritating corner cases
	 * e.g. when a frame is changed as a particle is moved
	 * 
	 */
	Lineage.Particle modifiedParticle=null;
	
	/**
	 * The particle might not be modified yet; then it should not be commited because this causes
	 * a useless undo item to be created
	 */
	boolean hasReallyModified;

	/**
	 * If there are icons (buttons) for interacting with a particle, which particle and where are they? 
	 */
	LineageSelParticle iconsForParticle=null;
	Rectangle rectIconCenterZ=null;
	Rectangle rectIconChangeRadius=null;

	
	public LineageImageRenderer(Viewer2DWindow w)
		{
		this.w=w;
		}


	
	public Collection<Lineage> getVisibleParticles()
		{
		//TODO: pick out
		return Lineage.getParticles(w.getRootObject());
		}
	

	
	/**
	 * Render lineage
	 */
	public void draw(Graphics g)
		{
		rectIconCenterZ=null;
		rectIconChangeRadius=null;
		iconsForParticle=null;
		
		
		//Update hover
		EvSelectable lastHover=EvSelection.currentHover;
		if(w.mouseInWindow)
			EvSelection.currentHover=EvSelection.noSelection;
	
		EvDecimal currentFrame=w.getFrame();
		
		interpParticle.clear();
		for(Lineage lin:getVisibleParticles())
			{
			Map<LineageSelParticle, Lineage.InterpolatedParticle> interpNucPart=lin.interpolateParticles(currentFrame);
			interpParticle.putAll(interpNucPart);
			}
		for(LineageSelParticle nucPair:interpParticle.keySet())
			{
			Lineage.InterpolatedParticle nuc=interpParticle.get(nucPair);
			drawParticle(g,nucPair,nuc,currentFrame);
			}

		if(!lastHover.equals(EvSelection.currentHover))
			EvBasicWindow.updateWindows(w);
		}
	
	
	public void dataChangedEvent()
		{
		}

	
	/**
	 * Currently modified lineage is finalized. Commit changes.
	 */
	public void commitModifyingNuc()
		{
		//Only commit if something has changed
		if(hasReallyModified)
			{
			hasReallyModified=false;
			final Lineage lin=modifyingParticleSelected.fst();
			final String name=modifyingParticleSelected.snd();
			final Lineage.Particle currentNuc=modifyingParticleSelected.getParticle().clone();
			final Lineage.Particle lastNuc=modifiedParticle; 
	
			new UndoOpBasic("Modify keyframe for "+modifyingParticleSelected.snd())
				{
				public void redo()
					{
					lin.particle.put(name, currentNuc);
					EvBasicWindow.updateWindows();
					}
	
				public void undo()
					{
					lin.particle.put(name, lastNuc);
					EvBasicWindow.updateWindows();
					}
				}.execute();
			}

		modifyingParticleSelected=null;
		modifiedParticle=null;
		}

	
	/**
	 * Draw ovaloid axis'
	 */
	private void drawAxis(Graphics g, LineageSelParticle nucPair, Lineage.InterpolatedParticle nuc, Vector2d so)
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
	 * Draw a particle
	 */
	private void drawParticle(Graphics g, LineageSelParticle sel, Lineage.InterpolatedParticle nuc, EvDecimal currentFrame)
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
			EvDecimal lastFrame=sel.getParticle().pos.lastKey();
			if(lastFrame!=null && lastFrame.lessEqual(currentFrame))
				{
				for(String child:sel.getParticle().child)
					{
					Lineage.Particle nchild=sel.fst().particle.get(child);
					if(!nchild.pos.isEmpty())
						{
						EvDecimal firstFrame=nchild.pos.firstKey();
						if(!nchild.pos.isEmpty() && firstFrame.greaterEqual(currentFrame))
							{
							Lineage.ParticlePos cpos=nchild.pos.get(firstFrame);
							Vector2d childso=w.transformPointW2S(new Vector2d(cpos.x,cpos.y));
							g.drawLine((int)so.x,(int)so.y, (int)childso.x,(int)childso.y);
							}
						}
					}
				}
			
			//Pick color
			Color particleColor;
			if(EvSelection.isSelected(sel))
				particleColor=Color.RED;
			else
				particleColor=Color.BLUE;
			
			//Draw the particle and check if it is visible
			g.setColor(particleColor);
			if(nuc.frameBefore==null)
				{
				if(!nuc.hasParent)
					{
					//As this particle does not really exist here, it is drawn with stippled line
					for(int i=0;i<360/2;i+=2)
						g.drawArc((int)(so.x-sor),(int)(so.y-sor),(int)(2*sor),(int)(2*sor), i*20, 20);
					isVisible=true;
					drawAxis(g, sel, nuc, so);
					}
				}
			else
				{
				//Normal state of rendering
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
					EvSelection.currentHover=sel;
				
				//Draw name. maybe this should be done last?
				if(EvSelection.currentHover.equals(sel) || EvSelection.isSelected(sel))
					{
					String showString=nucName;
					String eventName=sel.getParticle().events.get(currentFrame);
					if(eventName!=null)
						showString+=" ("+eventName+")";
					
					g.setColor(Color.RED);
					g.drawString(showString, (int)so.x-g.getFontMetrics().stringWidth(nucName)/2, (int)so.y-2);
					int crossSize=5;
					g.drawLine((int)so.x-crossSize, (int)so.y, (int)so.x+crossSize, (int)so.y);
					g.drawLine((int)so.x, (int)so.y, (int)so.x, (int)so.y+crossSize);
					}
				}
			}
		else
			sor=0;

		
		

		
		//Draw tool icons if this particle is selected (and only for one of them) and it is visible
		if(EvSelection.isSelected(sel) && iconsForParticle==null)
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
				
				iconsForParticle=sel;
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
	
	
	/**
	 * Get pos from modifying particle. Also creates a new position!!! (is this a good idea?) 
	 */
	public Lineage.ParticlePos getModifyingNucPos()
		{
		Lineage.Particle n=getModifyingParticle();
		if(n==null)
			return null;
		else
			{
			EvDecimal framei=w.getFrame();
			if(n.pos.get(framei)==null)
				{
				Lineage.InterpolatedParticle inter=n.interpolatePos(framei);
				n.pos.put(framei, inter.pos.clone());
				}
			return n.pos.get(framei);
			}
		}
	
	
	/**
	 * Get particle modified currently 
	 */
	public Lineage.Particle getModifyingParticle()
		{
		if(modifyingParticleSelected==null)
			return null;
		else
			return modifyingParticleSelected.fst().getCreateParticle(modifyingParticleSelected.snd());
		}
	
	public Lineage getModifyingLineage()
		{
		if(modifyingParticleSelected==null)
			return null;
		else
			return modifyingParticleSelected.fst();
		}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Viewer2DWindow.addImageWindowExtension(new Viewer2DWindowExtension()
			{
			public void newImageWindow(Viewer2DWindow w)
				{
				LineageImageRenderer r=new LineageImageRenderer(w);
				w.addImageWindowTool(new LineageImageTool(w,r));
				w.addImageWindowRenderer(r);
				}
			});
		}
	
	

	}
