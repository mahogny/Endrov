/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.annotationLineageIntegrationViewer2D;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.vecmath.*;
import javax.swing.*;

import endrov.annotationLineage.Lineage;
import endrov.annotationLineage.LineageCommonUI;
import endrov.annotationLineage.LineageSelParticle;
import endrov.annotationLineage.UndoOpLineageEditParticleKeyframe;
import endrov.annotationLineage.UndoOpLineageReplaceSomeParticle;
import endrov.data.EvContainer;
import endrov.data.EvSelection;
import endrov.gui.window.BasicWindow;
import endrov.keybinding.KeyBinding;
import endrov.util.EvDecimal;
import endrov.windowViewer2D.*;

/**
 * Lineage creation and editing tool for image window
 *
 * @author Johan Henriksson
 */
public class LineageImageTool implements Viewer2DTool, ActionListener
	{
	private boolean isMakingParticle=false;
	private double x1,x2,y1,y2;

	private boolean holdTranslateMouse=false;
	private boolean holdRadiusMouse=false;
	private boolean ignoreLeftClick=false;

	private final Viewer2DWindow w;
	private final LineageImageRenderer r;
	
	private WeakReference<Lineage> editingLin=new WeakReference<Lineage>(null);
	private void setEditLin(Lineage lin)
		{
		editingLin=new WeakReference<Lineage>(lin);
		}
	
	public LineageImageTool(final Viewer2DWindow w, LineageImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	
	
	
	
	private void fillMenu(JComponent menu)
		{
		new LineageCommonUI(w, w).addToMenu(menu, false);

		menu.add(new JSeparator());
		
		JMenuItem miNewEvent=new JMenuItem("Set event");
		menu.add(miNewEvent);
		miNewEvent.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				final Collection<LineageSelParticle> sel=LineageCommonUI.getSelectedParticleClone();
				if(sel.size()!=1)
					BasicWindow.showErrorDialog("Select 1 particle");
				else
					{
					final LineageSelParticle theSel=sel.iterator().next();
					final EvDecimal frame=w.getFrame();
					String currentEventName=theSel.getParticle().events.get(frame);
					if(currentEventName==null)
						currentEventName="";
					final String answer=JOptionPane.showInputDialog(null, "Event name", currentEventName);
					if(answer!=null)
						{
						new UndoOpLineageReplaceSomeParticle("Set event")
							{
							public void redo()
								{
								keep(theSel.fst(), theSel.snd());
								if(answer.equals(""))
									theSel.getParticle().events.remove(frame);
								else
									theSel.getParticle().events.put(frame, answer);
								BasicWindow.updateWindows();
								}
							}.execute();
						}
					}
				}
			});
		
		menu.add(new JSeparator());

		EvContainer ims=w.getRootObject();
		final WeakReference<EvContainer> wims=new WeakReference<EvContainer>(ims);
		if(ims!=null)
			for(Map.Entry<String, Lineage> e:ims.getIdObjects(Lineage.class).entrySet())
				{
				JCheckBoxMenuItem miEdit=new JCheckBoxMenuItem("Edit "+e.getKey());
				miEdit.setActionCommand(e.getKey());
				miEdit.setSelected(editingLin.get()==e.getValue());
				miEdit.addActionListener(this);
				menu.add(miEdit);
				}		
		
		JMenuItem miNew=new JMenuItem("New lineage");
		final LineageImageTool This=this;
		miNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				Lineage lin=new Lineage();
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
		JMenu menu=new JMenu("Lineage");
		fillMenu(menu);
		return menu;
		}
	public void actionPerformed(ActionEvent e)
		{
		String id=e.getActionCommand();
		setEditLin((Lineage)w.getRootObject().getMetaObject(id));
		w.setTool(this);
		}
	
	public void deselected()
		{
		}


	public void mouseClicked(MouseEvent e, Component invoker)
		{
		if(SwingUtilities.isLeftMouseButton(e) && !r.getVisibleParticles().isEmpty())
			{
			if(!ignoreLeftClick)
				LineageCommonUI.mouseSelectObject(EvSelection.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
				//LineageCommonUI.mouseSelectParticle(LineageCommonUI.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
			ignoreLeftClick=false;
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
			JPopupMenu popup=new JPopupMenu();
			fillMenu(popup);
			popup.show(e.getComponent(),e.getX(),e.getY());
			}
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(r.modifyingParticleSelected!=null && (holdTranslateMouse || holdRadiusMouse))
			{
			//Get or create position
			Lineage.ParticlePos pos=r.getModifyingNucPos();
			if(pos!=null)
				{
				if(holdTranslateMouse)
					{
					//Translate
					r.hasReallyModified=true;
					Vector2d v2=w.transformVectorS2W(new Vector2d(dx,dy));
					pos.x+=v2.x;
					pos.y+=v2.y;
					w.updateImagePanel(); //TODO should signal update of lineage
					}
				else if(holdRadiusMouse)
					{
					//Change radius
					r.hasReallyModified=true;
					pos.r+=w.scaleS2w(dy);
					if(pos.r<w.scaleS2w(8.0))
						pos.r=w.scaleS2w(8.0);
					w.updateImagePanel();
					//new UndoOpNucleiEditKeyframe(lin, name, frame, pos).execute();
					//This is some code that could be resurrected, updating movement in all windows. but it might be slow.
					//it is safer than the current approach from an undo point of view
					}
				
				}
			}
		
		if(isMakingParticle)
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
			if(r.rectIconChangeRadius!=null && r.rectIconChangeRadius.contains(e.getX(), e.getY()))
				{
				//Change radius of particle
				if(r.interpParticle.containsKey(r.iconsForParticle))
					{
					//Also select?
					holdRadiusMouse=true;
					LineageCommonUI.mouseSelectObject(r.iconsForParticle, false);
					//mouseSelectParticle(r.iconsForParticle, false);
					startModifying(r.iconsForParticle);
					}
				ignoreLeftClick=true;
				}
			else if(r.rectIconCenterZ!=null && r.rectIconCenterZ.contains(e.getX(), e.getY()))
				{
				//Move particle z to this plane
				if(r.interpParticle.containsKey(r.iconsForParticle))
					actionBringToZ(r.iconsForParticle);
				ignoreLeftClick=true;
				}
			else if(LineageCommonUI.getHoveredParticle()!=null)
				{
				//Move a particle
				if(r.interpParticle.containsKey(EvSelection.currentHover))
					{
					//Also select?
					holdTranslateMouse=true;
					LineageCommonUI.mouseSelectObject(EvSelection.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
					startModifying((LineageSelParticle)EvSelection.currentHover);
					}
				ignoreLeftClick=true;
				}			
			else
				{
				//Start making a particle
				isMakingParticle=true;
				Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
				x1=x2=v.x;
				y1=y2=v.y;
				}
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
			//Cancel making a particle
			isMakingParticle=false;
			w.updateImagePanel();
			}
		}

	
	public void mouseReleased(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			
			if(holdTranslateMouse)
				{
				holdTranslateMouse=false;
				r.commitModifyingNuc();
				}

			if(holdRadiusMouse)
				{
				holdRadiusMouse=false;
				r.commitModifyingNuc();
				}

			if(isMakingParticle)
				{
				//Make a particle if mouse has been dragged. If the particle is too small it is likely a mistake; ignore it in that case.
				final Lineage lin=editingLin.get();
				double radius=Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))/2;
				if(w.scaleW2s(radius)>5 && lin!=null /*&& r.modifyingNucSel==null*/)
					{
					final EvDecimal frame=w.getFrame();
					
					//New name for this particle => null
					final String particleName=lin.getUniqueParticleName();
					final Lineage.ParticlePos pos=new Lineage.ParticlePos();
					pos.x=(x1+x2)/2;
					pos.y=(y1+y2)/2;
					pos.z=w.getZ().doubleValue();
					pos.r=radius;
					
					new UndoOpLineageReplaceSomeParticle("Create "+particleName)
						{
						public void redo()
							{
							keep(lin,particleName);
							Lineage.Particle n=lin.getCreateParticle(particleName);
							n.pos.put(frame, pos);
							EvSelection.selectOnly(new LineageSelParticle(lin,particleName));
							BasicWindow.updateWindows();
							}
						}.execute();
					}
					
				isMakingParticle=false;
				w.updateImagePanel();
				}
			
			}
		}

	public void mouseMoved(MouseEvent e, int dx, int dy)
		{	
		}

	
	private void startModifying(LineageSelParticle sel)
		{
		r.modifyingParticleSelected=sel;
		r.modifiedParticle=sel.getParticle().clone();
		r.hasReallyModified=false;
		}
	
	public void keyPressed(KeyEvent e)
		{
		EvDecimal curFrame=w.getFrame();
		Lineage lin=LineageCommonUI.getHoveredParticleLineage();//LineageCommonUI.currentHover.fst();

		if(lin!=null && (KeyBinding.get(Lineage.KEY_TRANSLATE).typed(e) || KeyBinding.get(Lineage.KEY_CHANGE_RADIUS).typed(e)))
			{
			//Translate or change radius
			if(r.modifyingParticleSelected==null && LineageCommonUI.getHoveredParticle()!=null && r.interpParticle.containsKey(EvSelection.currentHover))
				startModifying(LineageCommonUI.getHoveredParticleSelectedOrNull());
			}
		else if(r.modifyingParticleSelected!=null)
			{
		
			if(lin!=null && KeyBinding.get(Lineage.KEY_DIVIDENUC).typed(e))
				{
				//Divide particle
				//Lineage.Particle n=getHoveredParticle();
				LineageSelParticle sel=LineageCommonUI.getHoveredParticleSelectedOrNull();
				if(sel!=null && r.interpParticle.containsKey(sel))
					LineageCommonUI.actionDivideParticle(lin,LineageCommonUI.getHoveredParticleSelectedOrNull().snd(), curFrame);
				}
			else if(KeyBinding.get(Lineage.KEY_SETZ).typed(e))
				{
				//Bring particle to this Z
				Set<LineageSelParticle> sels=LineageCommonUI.getSelectedOrHoveredParticle();
				if(sels.size()==1)
					{
					LineageSelParticle useNuc=sels.iterator().next();
					actionBringToZ(useNuc);
					}
				}
			else if(lin!=null && KeyBinding.get(Lineage.KEY_SETEND).typed(e))
				{
				//Set end frame
				//Lineage.Particle n=getHoveredParticle();//lin.particle.get(LineageCommonUI.currentHover.snd());
				LineageSelParticle sel=LineageCommonUI.getHoveredParticleSelectedOrNull();
				if(sel!=null)
					LineageCommonUI.actionSetEndFrame(Collections.singleton(sel), curFrame);
				}
			else if(lin!=null && KeyBinding.get(Lineage.KEY_SETSTART).typed(e))
				{
				//Set start frame
				//Lineage.Particle n=lin.particle.get(LineageCommonUI.currentHover.snd());
				LineageSelParticle sel=LineageCommonUI.getHoveredParticleSelectedOrNull();
				if(sel!=null)
					LineageCommonUI.actionSetStartFrame(Collections.singleton(sel), curFrame);
				}
			else if(lin!=null && KeyBinding.get(Lineage.KEY_SETPARENT).typed(e))
				{
				//Create parent-children relation
				LineageCommonUI.actionAssocParent();
				}
			
			}
		
		}

	
	private void actionBringToZ(LineageSelParticle sel)
		{
		EvDecimal curFrame=w.getFrame();
		Lineage.ParticlePos pos=LineageCommonUI.getOrInterpolatePosCopy(sel.fst(), sel.snd(), curFrame);
		if(pos!=null)
			{
			pos.z=w.getZ().doubleValue();
			new UndoOpLineageEditParticleKeyframe("Bring "+sel.snd()+" to z",sel.fst(), sel.snd(), curFrame, pos).execute();
			}
		}

	public void paintComponent(Graphics g)
		{
		if(isMakingParticle)
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
		}

	public void mouseExited(MouseEvent e)
		{
		//Stop editing here!
		if(r.modifyingParticleSelected!=null)
			r.commitModifyingNuc();
		BasicWindow.updateWindows();
		}

	
	}
