package evplugin.nuc;

import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

import javax.vecmath.*;
import javax.swing.*;

import evplugin.basicWindow.*;
import evplugin.ev.Log;
import evplugin.imageWindow.*;
import evplugin.imageset.*;
import evplugin.keyBinding.KeyBinding;

/**
 * Make nuclei by dragging an area. Also move nuclei.
 *
 * @author Johan Henriksson
 */
public class ToolMakeNuc implements ImageWindowTool
	{
	private boolean active=false;
	private double x1,x2,y1,y2;

	private boolean holdTranslate=false;
	private boolean holdRadius=false;

	private final ImageWindow w;
	private final NucImageRenderer r;
	
	public ToolMakeNuc(ImageWindow w, NucImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	
	public boolean isToggleable()
		{
		return true;
		}
	public String toolCaption()
		{
		return "Nucleus/Define";
		}
	public boolean enabled()
		{
		return true;
		}

	
	
	
	/**
	 * Get a lineage. Create lineage if there is none and this is possible
	 */
/*	private NucLineage getLineage()
		{
		NucLineage lin=r.getLineage();
		if(lin==null)
			{
			Imageset rec=w.getImageset();
			rec.addMetaObject(new NucLineage());
			return r.getLineage();
			}
		else
			return lin;
		}*/
	
	
	private Collection<NucLineage> getLineages()
		{
		Collection<NucLineage> lins=r.getVisibleLineages();
		if(lins.isEmpty())
			{
			Imageset rec=w.getImageset();
			rec.addMetaObject(new NucLineage());
			return r.getVisibleLineages();
			}
		else
			return lins;
		}
	
	
	public void mouseClicked(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && !r.getVisibleLineages().isEmpty())
			NucLineage.mouseSelectNuc(NucLineage.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(active)
			{
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			x2=v.x;
			y2=v.y;
			w.updateImagePanel();
			}
		}
	
	public void mousePressed(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			//Start making a nucleus
			active=true;
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			x1=x2=v.x;
			y1=y2=v.y;
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
			//Cancel making nucleus
			active=false;
			w.updateImagePanel();
			}
		}

	public void mouseReleased(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && active)//changed
			{
			//Make a nucleus if mouse has been dragged
//			NucLineage lin=getLineage();
			Collection<NucLineage> lins=getLineages();
			//TODO proper multiselection
			NucLineage lin=null;
			if(!lins.isEmpty())
				lin=lins.iterator().next();
			
			if(x1!=x2 && y1!=y2 && lin!=null && r.modifyingNucName==null)
				{
				//New name for this nucleus => null
				String nucName=lin.getUniqueNucName();
				NucLineage.Nuc n=lin.getNucCreate(nucName);
				NucLineage.NucPos pos=n.getPosCreate((int)w.frameControl.getFrame());
				pos.x=(x1+x2)/2;
				pos.y=(y1+y2)/2;
				pos.z=w.s2wz(w.frameControl.getZ());
				pos.r=Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))/2;
				
				Vector2d so1=w.transformW2S(new Vector2d(pos.r,0));
				Vector2d so2=w.transformW2S(new Vector2d(0,0));

				if(Math.abs(so1.x-so2.x)>8)
//				if(Math.abs(w.w2sx(pos.r)-w.w2sx(0))>8)
					{
					NucLineage.selectedNuclei.clear();
					NucLineage.selectedNuclei.add(new NucPair(lin,nucName));
					BasicWindow.updateWindows();
					}
				}
				
			active=false;
			w.updateImagePanel();
			}
		}

	public void mouseMoved(MouseEvent e, int dx, int dy)
		{			
		//Handle modify-nucleus
		NucLineage.NucPos pos=r.getModifyingNucPos();
		if(pos!=null)
			{
			if(holdTranslate)
				{
				//Translate
				pos.x+=w.scaleS2w(dx);
				pos.y+=w.scaleS2w(dy);
				}
			//else if(KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e)) //KEYBIND
			else if(holdRadius)
				{
				//Change radius
				pos.r+=w.scaleS2w(dy);
				if(pos.r<w.scaleS2w(8.0))
					pos.r=w.scaleS2w(8.0);
				}			
			}
		}

	
	/*
	 * (non-Javadoc)
	 * @see client.ImageWindow.Tool#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
		{
		if(KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e))
			holdTranslate=true;
		if(KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			holdRadius=true;
		
		
		int curFramei=(int)w.frameControl.getFrame();
		NucLineage lin=NucLineage.currentHover.getLeft();//getLineage();
		if(lin==null)
			return;
		
		if(KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e) || KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			{
			//Translate or change radius
			if(r.modifyingNucName==null && r.interpNuc.containsKey(NucLineage.currentHover)) //TODO: guarantee that every nucleus has 1 pos except during load
				r.modifyingNucName=NucLineage.currentHover;
			}
		else if(KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			{
			//Divide nucleus
			NucLineage.Nuc n=NucLineage.currentHover.getLeft().nuc.get(NucLineage.currentHover.getRight());
			if(n!=null && r.interpNuc.containsKey(NucLineage.currentHover))
				{
				lin.divide(NucLineage.currentHover.getRight(), curFramei);
				BasicWindow.updateWindows();
				}
			}
		else if(KeyBinding.get(NucLineage.KEY_SETZ).typed(e))
			{
			//Bring nucleus to this Z
			NucLineage.Nuc n=lin.nuc.get(NucLineage.currentHover.getRight());
			
			NucPair useNuc=NucLineage.currentHover;
			if(useNuc.equals("") && NucLineage.selectedNuclei.size()==1)
				{
				//Take a selected nucleus instead
				useNuc=NucLineage.selectedNuclei.iterator().next();
				}
			
			if(n!=null && r.interpNuc.containsKey(useNuc))
				{
				NucLineage.NucInterp inter=r.interpNuc.get(NucLineage.currentHover);
				NucLineage.NucPos pos=n.getPosCreate(curFramei); 
				//Maybe this function should interpolate whenever possible. would give better separation
//				n.pos.put(curFramei,pos);
				pos.x=inter.pos.x;
				pos.y=inter.pos.y;
				pos.z=w.s2wz(w.frameControl.getZ());
				pos.r=inter.pos.r;
				r.commitModifyingNuc();
				}
			}
		else if(KeyBinding.get(NucLineage.KEY_SETEND).typed(e))
			{
			//Set end frame of nucleus
			NucLineage.Nuc n=lin.nuc.get(NucLineage.currentHover.getRight());
			if(n!=null)
				{
				if(n.end!=null && n.end==curFramei)
					n.end=null;
				else
					{
					n.end=curFramei;
					//r.getModifyingNucPos(); //Make a key frame for the sake of keeping interpolation?
					lin.removePosAfterEqual(NucLineage.currentHover.getRight(), curFramei+1);
					}
				BasicWindow.updateWindows();
				}
			}
		else if(KeyBinding.get(NucLineage.KEY_MAKEPARENT).typed(e))
			{
			//Create parent for selected nucleus/nuclei
			String parentName=lin.getUniqueNucName();
			NucLineage.Nuc parent=lin.getNucCreate(parentName);
			
			double x=0,y=0,z=0,r=0;
			int num=0;
			int firstFrame=1000000;
			for(NucPair childPair:NucLineage.selectedNuclei)
				{
				String childName=childPair.getRight();
				NucLineage.Nuc n=lin.nuc.get(childName);
				NucLineage.NucPos pos=n.pos.get(n.pos.firstKey());
				x+=pos.x;				y+=pos.y;				z+=pos.z;				r+=pos.r;
				if(n.pos.firstKey()<firstFrame)
					firstFrame=n.pos.firstKey();
				num++;
				n.parent=parentName;
				parent.child.add(childName);
				}
			x/=num;			y/=num;			z/=num;			r/=num;
			NucLineage.NucPos pos=new NucLineage.NucPos();
			pos.x=x; pos.y=y; pos.z=z; pos.r=r;
			parent.pos.put(firstFrame-1,pos);
			Log.printLog("Made parent "+parentName);
			
			this.r.w.frameControl.setFrame(firstFrame-1);
			BasicWindow.updateWindows();
			}
		else if(KeyBinding.get(NucLineage.KEY_SETPARENT).typed(e))
			{
			//Create parent-children relation
			NucLineage.createParentChildSelected();
			BasicWindow.updateWindows();
			}
		}

	
	public void paintComponent(Graphics g)
		{
		if(active)
			{
			g.setColor(Color.RED);
			double midx=(x2+x1)/2;
			double midy=(y2+y1)/2;
			double r=Math.sqrt((x1-midx)*(x1-midx)+(y1-midy)*(y1-midy));
			Vector2d omid=w.transformW2S(new Vector2d(midx,midy));
//			double omidx=w.w2sx(midx);
//			double omidy=w.w2sy(midy);
			double or=w.scaleW2s(r);
			g.drawOval((int)(omid.x-or),(int)(omid.y-or),(int)(or*2),(int)(or*2));
			}
		}

	
	public void keyReleased(KeyEvent e)
		{
		if(KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e))
			holdTranslate=false;
		if(KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			holdRadius=false;
		
		if(KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e) || KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			r.commitModifyingNuc();
		}

	public void mouseExited(MouseEvent e)
		{
		//	Delete nucleus if one is held and translating
		NucLineage lin=r.getModifyingLineage();
		NucLineage.Nuc n=r.getModifyingNuc();
		
		if(lin!=null && n!=null && holdTranslate)
			{
			//nuc temporarily flashes back here. don't know why (used to?)
			int framei=(int)w.frameControl.getFrame();
			n.pos.remove(framei);
			if(n.pos.size()==0)
				lin.removeNuc(r.modifyingNucName.getRight());
			}
		r.modifyingNucName=null;
		BasicWindow.updateWindows();
		}

	
	public void unselected() {}
	}
