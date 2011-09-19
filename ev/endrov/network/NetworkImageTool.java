/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.network;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import javax.vecmath.*;
import javax.swing.*;

import endrov.basicWindow.*;
import endrov.data.EvContainer;
import endrov.imageWindow.*;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;
import endrov.util.Vector3i;

/**
 * Make nuclei by dragging an area. Also move nuclei.
 *
 * @author Johan Henriksson
 */
public class NetworkImageTool implements ImageWindowTool, ActionListener
	{
	private final ImageWindow w;
	private final NetworkImageRenderer r;
	
	private WeakReference<Network> editingObject=new WeakReference<Network>(null);
	private void setEditObject(Network lin)
		{
		editingObject=new WeakReference<Network>(lin);
		}
	
	public NetworkImageTool(final ImageWindow w, NetworkImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	
	
	
	
	private void fillMenu(JComponent menu)
		{
		
		
		JMenuItem miExportToSWC=new JMenuItem("Export to SWC");
		miExportToSWC.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				EvDecimal frame=w.getFrame();
				Network editNetwork=editingObject.get();
				if(editNetwork==null)
					return;
				Network.NetworkFrame nf=editNetwork.frame.get(frame);
				if(nf==null)
					return;
				
				JFileChooser fc=new JFileChooser();
				int ret=fc.showSaveDialog(w);
				if(ret==JFileChooser.APPROVE_OPTION)
					{
					File f=fc.getSelectedFile();
					try
						{
						SWCFile.write(f, nf);
						}
					catch (IOException e1)
						{
						BasicWindow.showErrorDialog("Error writing file: "+e1.getMessage());
						e1.printStackTrace();
						}
					}
				}
		});
		menu.add(miExportToSWC);
		
		menu.add(new JSeparator());
		
		EvContainer ims=w.getRootObject();
		final WeakReference<EvContainer> wims=new WeakReference<EvContainer>(ims);
		if(ims!=null)
			for(Map.Entry<String, Network> e:ims.getIdObjects(Network.class).entrySet())
				{
				JCheckBoxMenuItem miEdit=new JCheckBoxMenuItem("Edit "+e.getKey());
				miEdit.setActionCommand(e.getKey());
				miEdit.setSelected(editingObject.get()==e.getValue());
				miEdit.addActionListener(this);
				menu.add(miEdit);
				}		
		
		JMenuItem miNew=new JMenuItem("New object");
		miNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				Network lin=new Network();
				wims.get().addMetaObject(lin);
				setEditObject(lin);
				w.setTool(NetworkImageTool.this);
				}
		});
		menu.add(miNew);
		
		}

	public JMenuItem getMenuItem()
		{
		JMenu menu=new JMenu("Network");
		fillMenu(menu);
		return menu;
		}
	public void actionPerformed(ActionEvent e)
		{
		String id=e.getActionCommand();
		setEditObject((Network)w.getRootObject().getMetaObject(id));
		w.setTool(this);
		}
	
	public void deselected()
		{
		r.removeTracer();
		}


	public void mouseClicked(MouseEvent e, Component invoker)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			Network editNetwork=editingObject.get();
			if(editNetwork!=null)
				{
				EvDecimal frame=w.getFrame();
				Network.NetworkFrame nf=editNetwork.frame.get(frame);
				if(nf==null)
					editNetwork.frame.put(frame,nf=new Network.NetworkFrame());

				EvStack stack=r.getCurrentStack();
				
				if(nf.points.isEmpty())
					{
					//First time just create a free point. Make it fit the grid for later simplicity
					
					Vector3i v=r.getMousePosImage(stack, e);
					Vector3d posW=stack.transformImageWorld(new Vector3d(v.x,v.y,v.z));
					
					nf.putNewPoint(new Network.Point(posW.x,posW.y, posW.z, null));
					
					//Update tracer
					r.getAutoTracer(stack, nf);
					}
				else
					{
					//TODO do not allow this to be done unless thread has calculated the tracer already
					

					SemiautoNetworkTracer auto=r.getAutoTracer(stack, nf);
					
					Vector3i toPosImage=r.getMousePosImage(stack, e);
					
					//Extract path and add to network
					List<Vector3i> points=auto.findPathTo(toPosImage.x, toPosImage.y, toPosImage.z);
					Network.Segment segment=new Network.Segment();
					segment.points=new int[points.size()];
					for(int i=0;i<points.size();i++)
						{
						Vector3i v=points.get(i);
						Vector3d posW=stack.transformImageWorld(new Vector3d(v.x,v.y,v.z));
						
						int pi;
						pi=nf.reusePoint(new Network.Point(posW,null));
						
						segment.points[i]=pi;
						}
					nf.segments.add(segment);
					
					//Update tracer
					r.setForcedStartingPoint(null);
					r.getAutoTracer(stack, nf);
					}
				
				

				BasicWindow.updateWindows();
				}
					
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
			JPopupMenu menu=new JPopupMenu();

			
			EvDecimal frame=w.getFrame();
			Network editNetwork=editingObject.get();
			final Network.NetworkFrame nf=editNetwork.frame.get(frame);
			
			final Vector2d xy=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
			final double z=w.getZ().doubleValue();

			
			if(nf!=null)
				{

				//Could also use click and drag for this?
				JMenuItem miForcePoint=new JMenuItem("Start from here");
				miForcePoint.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent a)
						{
						EvStack stack=r.getCurrentStack();
						if(stack!=null)
							{
							r.setForcedStartingPoint(new Vector3d(xy.x,xy.y,z));
							r.getAutoTracer(stack, nf);
							w.repaint();
							}
						}
					});
				
				

				JMenuItem miForceNone=new JMenuItem("Start from network");
				miForceNone.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent a)
						{
						EvStack stack=r.getCurrentStack();
						if(stack!=null)
							{
							r.setForcedStartingPoint(null);
							r.getAutoTracer(stack, nf);
							w.repaint();
							}
						}
					});
				
				
				menu.add(miForcePoint);
				menu.add(miForceNone);
				}

			
			menu.show(e.getComponent(),e.getX(),e.getY());
			}
		}
	
	
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		
		}
	
	
	public void mousePressed(MouseEvent e)
		{
	
		}

	
	public void mouseReleased(MouseEvent e)
		{
		
		}

	public void mouseMoved(MouseEvent e, int dx, int dy)
		{
		if(r.hasTracer())
			{
			r.recalcTrace(e);
			w.repaint();
			}
		}

	
	/*
	private void startModifying(NucSel sel)
		{
	
		r.modifyingNucSel=sel;
		r.modifiedNuc=sel.getNuc().clone();
		r.hasReallyModified=false;
		
		}*/
	
	public void keyPressed(KeyEvent e)
		{
		/*
		EvDecimal curFrame=w.getFrame();
		NucLineage lin=NucCommonUI.currentHover.fst();

		if(lin!=null && (KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e) || KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e)))
			{
			//Translate or change radius
			if(r.modifyingNucSel==null && NucCommonUI.currentHover!=NucCommonUI.emptyHover && r.interpNuc.containsKey(NucCommonUI.currentHover))
				startModifying(NucCommonUI.currentHover);
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
					actionBringToZ(useNuc);
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
			
			else if(lin!=null && KeyBinding.get(NucLineage.KEY_SETPARENT).typed(e))
				{
				//Create parent-children relation
				NucCommonUI.actionAssocParent();
				}
			
			}
		*/
		}

	/*
	private void actionBringToZ(NucSel useNuc)
		{
		EvDecimal curFrame=w.getFrame();
		NucLineage.NucPos pos=NucCommonUI.getOrInterpolatePosCopy(useNuc.fst(), useNuc.snd(), curFrame);
		if(pos!=null)
			{
			pos.z=w.getZ().doubleValue();
			new UndoOpNucleiEditKeyframe("Bring "+useNuc.snd()+" to z",useNuc.fst(), useNuc.snd(), curFrame, pos).execute();
			}
		}
*/
	
	public void paintComponent(Graphics g)
		{
		/*
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
			*/
		}
	
	public void keyReleased(KeyEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		/*
		//Stop editing here!
		if(r.modifyingNucSel!=null)
			r.commitModifyingNuc();
		BasicWindow.updateWindows();
		*/
		}

	
	}
