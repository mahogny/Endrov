/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.nucImage;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
//import java.util.Collection;

import javax.vecmath.*;
import javax.swing.*;

import endrov.basicWindow.*;
import endrov.data.EvContainer;
import endrov.data.EvSelection;
import endrov.imageWindow.*;
import endrov.keyBinding.KeyBinding;
import endrov.nuc.NucLineage;
import endrov.nuc.NucCommonUI;
import endrov.nuc.NucSel;
import endrov.nuc.UndoOpReplaceSomeNuclei;
import endrov.nuc.NucLineage.NucPos;
import endrov.undo.UndoOpBasic;
import endrov.util.EvDecimal;

/**
 * Make nuclei by dragging an area. Also move nuclei.
 *
 * @author Johan Henriksson
 */
public class NucImageTool implements ImageWindowTool, ActionListener
	{
	private boolean isMakingNucleus=false;
	private double x1,x2,y1,y2;

	private boolean holdTranslate=false;
	private boolean holdRadius=false;

	private final ImageWindow w;
	private final NucImageRenderer r;
	
	/**
	 * Undo operation for only changing one keyframe
	 * @author Johan Henriksson
	 */
	
	public static class UndoOpNucleiEditKeyframe extends UndoOpBasic
		{
		private NucLineage lin;
		private String name;
		private EvDecimal frame;
		private NucPos pos;
		private NucPos newPos;
		public UndoOpNucleiEditKeyframe(String opname, NucLineage lin, String name, EvDecimal frame, NucPos newPos)
			{
			super(opname);
			this.frame=frame;
			this.lin=lin;
			this.name=name;
			this.newPos=newPos;
			NucLineage.Nuc nuc=lin.nuc.get(name);
			if(nuc.pos.containsKey(frame))
				pos=nuc.pos.get(frame).clone();
			}
	
		public void redo()
			{
			NucLineage.Nuc nuc=lin.nuc.get(name);
			nuc.pos.put(frame, newPos);
			BasicWindow.updateWindows();  //this is a problem! w.updateImagePanel works well
			}
		
		public void undo()
			{
			if(pos==null)
				lin.nuc.get(name).pos.remove(frame);
			else
				lin.nuc.get(name).pos.put(frame,pos.clone());
			BasicWindow.updateWindows();
			}
		
		}
	
	
	
	
	private WeakReference<NucLineage> editingLin=new WeakReference<NucLineage>(null);
	private void setEditLin(NucLineage lin)
		{
		editingLin=new WeakReference<NucLineage>(lin);
		}
	
	public NucImageTool(final ImageWindow w, NucImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	
	
	
	
	private void fillMenu(JComponent menu)
		{
		new NucCommonUI(w).addToMenu(menu, false);

		menu.add(new JSeparator());
		
		EvContainer ims=w.getRootObject();
		final WeakReference<EvContainer> wims=new WeakReference<EvContainer>(ims);
		if(ims!=null)
			for(Map.Entry<String, NucLineage> e:ims.getIdObjects(NucLineage.class).entrySet())
				{
				JCheckBoxMenuItem miEdit=new JCheckBoxMenuItem("Edit "+e.getKey());
				miEdit.setActionCommand(e.getKey());
				miEdit.setSelected(editingLin.get()==e.getValue());
				miEdit.addActionListener(this);
				menu.add(miEdit);
				}
		JMenuItem miNew=new JMenuItem("New lineage");
		final NucImageTool This=this;
		miNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				NucLineage lin=new NucLineage();
				wims.get().addMetaObject(lin);
				setEditLin(lin);
				w.setTool(This);
				}
		});
		menu.add(miNew);
		
		JMenuItem miAuto=new JMenuItem("Autolineage...");
		miAuto.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				new WindowAutoLineaging();
				}
		});		
		menu.add(miAuto);
		}

	public JMenuItem getMenuItem()
		{
		JMenu menu=new JMenu("Nucleus");
		fillMenu(menu);
		return menu;
		}
	public void actionPerformed(ActionEvent e)
		{
		String id=e.getActionCommand();
		setEditLin((NucLineage)w.getRootObject().getMetaObject(id));
		w.setTool(this);
		}
	
	public void deselected()
		{
		}

	
	
	public void mouseClicked(MouseEvent e, Component invoker)
		{
		if(SwingUtilities.isLeftMouseButton(e) && !r.getVisibleLineages().isEmpty())
			NucCommonUI.mouseSelectNuc(NucCommonUI.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
		else if(SwingUtilities.isRightMouseButton(e))
			{
			JPopupMenu popup=new JPopupMenu();
			fillMenu(popup);
			popup.show(e.getComponent(),e.getX(),e.getY());
			}
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(isMakingNucleus)
			{
			Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
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
			isMakingNucleus=true;
			Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
			x1=x2=v.x;
			y1=y2=v.y;
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
			//Cancel making nucleus
			isMakingNucleus=false;
			w.updateImagePanel();
			}
		}

	
	public void mouseReleased(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && isMakingNucleus)
			{
			//Make a nucleus if mouse has been dragged. If the nucleus is too small it is likely a mistake; ignore it then.
			final NucLineage lin=editingLin.get();
			double radius=Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))/2;
			if(w.scaleW2s(radius)>5 && lin!=null /*&& r.modifyingNucSel==null*/)
				{
				final EvDecimal frame=w.getFrame();
				
				//New name for this nucleus => null
				final String nucName=lin.getUniqueNucName();
				final NucLineage.NucPos pos=new NucLineage.NucPos();
				pos.x=(x1+x2)/2;
				pos.y=(y1+y2)/2;
				pos.z=w.frameControl.getZ().doubleValue();
				pos.r=radius;
				
				new UndoOpReplaceSomeNuclei("Create "+nucName)
					{
					public void redo()
						{
						keep(lin,nucName);
						NucLineage.Nuc n=lin.getCreateNuc(nucName);
						n.pos.put(frame, pos);
						EvSelection.selectOnly(new NucSel(lin,nucName));
						BasicWindow.updateWindows();
						}
					}.execute();
				}
				
			isMakingNucleus=false;
			w.updateImagePanel();
			}
		}

	public void mouseMoved(MouseEvent e, int dx, int dy)
		{			
		if(r.modifyingNucSel!=null && (holdTranslate || holdRadius))
			{
			/*NucLineage lin=r.modifyingNucSel.fst();
			String name=r.modifyingNucSel.snd();
			EvDecimal frame=w.getFrame();*/
			//NucLineage.NucPos pos=NucCommonUI.getOrInterpolatePosCopy(lin, name, frame);
			
			
			
			//Get or create position
			NucLineage.NucPos pos=r.getModifyingNucPos();
			if(pos!=null)
				{
				if(holdTranslate)
					{
					//Translate
					Vector2d v2=w.transformVectorS2W(new Vector2d(dx,dy));
					pos.x+=v2.x;
					pos.y+=v2.y;
					w.updateImagePanel(); //TODO should signal update of lineage
					//new UndoOpNucleiEditKeyframe(lin, name, frame, pos).execute();
					}
				//else if(KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e)) //KEYBIND
				else if(holdRadius)
					{
					//Change radius
					pos.r+=w.scaleS2w(dy);
					if(pos.r<w.scaleS2w(8.0))
						pos.r=w.scaleS2w(8.0);
					w.updateImagePanel();
					//new UndoOpNucleiEditKeyframe(lin, name, frame, pos).execute();
					}
				
				}
			}
		}

	
	public void keyPressed(KeyEvent e)
		{
		if(KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e))
			holdTranslate=true;
		if(KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			holdRadius=true;
		
		EvDecimal curFrame=w.getFrame();
		NucLineage lin=NucCommonUI.currentHover.fst();

		System.out.println("press!");

		if(lin!=null && (KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e) || KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e)))
			{
			//Translate or change radius
			if(r.modifyingNucSel==null && NucCommonUI.currentHover!=NucCommonUI.emptyHover && r.interpNuc.containsKey(NucCommonUI.currentHover))
				{
				System.out.println("modifying   "+r.modifyingNucSel+"   "+NucCommonUI.currentHover);
				r.modifyingNucSel=NucCommonUI.currentHover;
				r.modifiedNuc=r.modifyingNucSel.getNuc().clone();
				}
			}
		else if(r.modifyingNucSel!=null)
			{
		
			if(lin!=null && KeyBinding.get(NucLineage.KEY_DIVIDENUC).typed(e))
				{
				//Divide nucleus
				NucLineage.Nuc n=NucCommonUI.currentHover.getNuc();
				if(n!=null && r.interpNuc.containsKey(NucCommonUI.currentHover))
					NucCommonUI.actionDivideNuc(lin,NucCommonUI.currentHover.snd(), curFrame);
				}
			else if(KeyBinding.get(NucLineage.KEY_SETZ).typed(e))
				{
				//Bring nucleus to this Z
				Set<NucSel> sels=NucCommonUI.getSelectedOrHoveredNuclei();
				if(sels.size()==1)
					{
					NucSel useNuc=sels.iterator().next();
					NucLineage.NucPos pos=NucCommonUI.getOrInterpolatePosCopy(useNuc.fst(), useNuc.snd(), curFrame);
					if(pos!=null)
						{
						pos.z=w.frameControl.getZ().doubleValue();
						new NucImageTool.UndoOpNucleiEditKeyframe("Bring to z, "+useNuc.snd(),useNuc.fst(), useNuc.snd(), curFrame, pos).execute();
						}
					}
				}
			else if(lin!=null && KeyBinding.get(NucLineage.KEY_SETEND).typed(e))
				{
				//Set end frame of nucleus
				NucLineage.Nuc n=lin.nuc.get(NucCommonUI.currentHover.snd());
				if(n!=null)
					NucCommonUI.actionSetEndFrame(Collections.singleton(NucCommonUI.currentHover), curFrame);
				}
			else if(lin!=null && KeyBinding.get(NucLineage.KEY_SETSTART).typed(e))
				{
				//Set end frame of nucleus
				NucLineage.Nuc n=lin.nuc.get(NucCommonUI.currentHover.snd());
				if(n!=null)
					NucCommonUI.actionSetStartFrame(Collections.singleton(NucCommonUI.currentHover), curFrame);
				}
			/*else if(lin!=null && KeyBinding.get(NucLineage.KEY_MAKEPARENT).typed(e))
				{
				//This way of working... should it be kept?
				
				//Create parent for selected nucleus/nuclei
				String parentName=lin.getUniqueNucName();
				NucLineage.Nuc parent=lin.getCreateNuc(parentName);
				
				double x=0,y=0,z=0,r=0;
				int num=0;
				EvDecimal firstFrame=null;
				for(NucSel childPair:NucLineage.getSelectedNuclei())
					{
					String childName=childPair.snd();
					NucLineage.Nuc n=lin.nuc.get(childName);
					NucLineage.NucPos pos=n.pos.get(n.getFirstFrame());
					x+=pos.x;				
					y+=pos.y;				
					z+=pos.z;				
					r+=pos.r;
					if(firstFrame==null || n.getFirstFrame().less(firstFrame))
						firstFrame=n.getFirstFrame();
					num++;
					n.parent=parentName;
					parent.child.add(childName);
					}
				x/=num;			
				y/=num;			
				z/=num;			
				r/=num;
				NucLineage.NucPos pos=new NucLineage.NucPos();
				pos.x=x; 
				pos.y=y; 
				pos.z=z; 
				pos.r=r;
				parent.pos.put(firstFrame.subtract(EvDecimal.ONE),pos);
				
//				this.r.w.frameControl.setFrame(firstFrame.subtract(EvDecimal.ONE));
				BasicWindow.updateWindows();
				}*/
			else if(lin!=null && KeyBinding.get(NucLineage.KEY_SETPARENT).typed(e))
				{
				//Create parent-children relation
				NucCommonUI.actionAssocParent();
				}
			
			}
		
		}

	
	public void paintComponent(Graphics g)
		{
		if(isMakingNucleus)
			{
			g.setColor(Color.RED);
			double midx=(x2+x1)/2;
			double midy=(y2+y1)/2;
			double r=Math.sqrt((x1-midx)*(x1-midx)+(y1-midy)*(y1-midy));
			Vector2d omid=w.transformPointW2S(new Vector2d(midx,midy));
			double or=w.scaleW2s(r);
			g.drawOval((int)(omid.x-or),(int)(omid.y-or),(int)(or*2),(int)(or*2));
			}
		}

	
	public void keyReleased(KeyEvent e)
		{
		System.out.println("release!");

		if(KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e))
			holdTranslate=false;
		if(KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			holdRadius=false;
		
		if(!holdTranslate && !holdRadius && r.modifyingNucSel!=null)
//		if(KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e) || KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			r.commitModifyingNuc();
		}

	public void mouseExited(MouseEvent e)
		{
		System.out.println("mouse exit");
		
		//Stop editing here!
		if(r.modifyingNucSel!=null)
			r.commitModifyingNuc();
		
		/*
		
		//Delete nucleus if one is held and translating
		NucLineage lin=r.getModifyingLineage();
		NucLineage.Nuc n=r.getModifyingNuc();
		
		if(lin!=null && n!=null && holdTranslate)
			{
			EvDecimal framei=w.frameControl.getFrame();
			n.pos.remove(framei);
			if(n.pos.size()==0)
				lin.removeNuc(r.modifyingNucSel.snd());
			}
		r.modifyingNucSel=null;
		r.modifiedNuc=null;
		*/
		BasicWindow.updateWindows();
		}

	
	}
