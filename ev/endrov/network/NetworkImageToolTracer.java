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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.*;
import javax.swing.*;

import endrov.basicWindow.*;
import endrov.data.EvContainer;
import endrov.ev.EV;
import endrov.imageWindow.*;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;
import endrov.util.ProgressHandle;
import endrov.util.Vector3i;

/**
 * Make and edit networks
 *
 * @author Johan Henriksson
 */
public class NetworkImageToolTracer implements ImageWindowTool, ActionListener
	{
	private final ImageWindow w;
	private final NetworkImageRenderer r;
		
	public boolean useAuto=false;

	
	


	public static Vector<NetworkTracerFactory> tracers=new Vector<NetworkTracerFactory>();
	static
		{
		tracers.add(NetworkTracerSemiauto.factory);
		}
	private NetworkTracerFactory tracerFactory=NetworkTracerSemiauto.factory;
	
	private Vector3d forcedStartingPoint=null;
	private NetworkTracerInterface lastAuto=null;
	private WeakReference<EvStack> lastStack=new WeakReference<EvStack>(null);

	

	public boolean hasTracer()
		{
		return lastAuto!=null;
		}
	
	public void setForcedStartingPoint(Vector3d v)
		{
		if(!EV.equalsHandlesNull(forcedStartingPoint,v))
			{
			forcedStartingPoint=v;
			lastAuto=null;
			lastStack=new WeakReference<EvStack>(null);
			}
		}

	
	
	/**
	 * Get the automatic tracer and ensure it is up to date. If no tracer is enabled then returns null
	 */
	public NetworkTracerInterface getTracer(EvStack stack, Network.NetworkFrame nf)
		{
		//Reuse or start anew?
		NetworkTracerInterface auto;
		if(lastStack.get()==stack && lastAuto!=null)
			auto=lastAuto;
		else
			{
			if(tracerFactory==null)
				return null;
			else
				{
				lastStack=new WeakReference<EvStack>(stack);
				lastAuto=auto=tracerFactory.create();
				}
			}
		
		//Update costs. If there is a forced starting point then use it, otherwise use the network 
		System.out.println("Calculating");
		if(forcedStartingPoint==null)
			auto.preprocess(new ProgressHandle(), stack, NetworkTracerInterface.startingPointsFromFrame(stack, nf));
		else
			{
			System.out.println("--------------- calc from forced point ----");
			Vector3d v=stack.transformWorldImage(forcedStartingPoint);
			auto.preprocess(new ProgressHandle(), stack, Collections.singleton(new Vector3i((int)v.x,(int)v.y,(int)v.z)));
			}
		System.out.println("Calc done");
		return auto;
		}
	
	/**
	 * Remove tracer and associated memory
	 */
	public void removeTracer()
		{
		lastStack=new WeakReference<EvStack>(null);
		lastAuto=null;
		r.previewPoints=null;
		forcedStartingPoint=null;
		}

	
	
	/**
	 * Calculate preview points. This should only be done when the mouse moves as the window might be redrawn for other reasons
	 * @param e
	 */
	public void recalcTrace(MouseEvent e)
		{
		NetworkTracerInterface auto=lastAuto;
		
		EvStack stack=r.getCurrentStack();
		if(stack!=null)
			{
			Vector3i toPosImage=r.getMousePosImage(stack, e);
			List<Vector3i> points=auto.findPathTo(toPosImage.x, toPosImage.y, toPosImage.z);
			if(points!=null)
				{
				r.previewPoints=new Vector3d[points.size()];
				for(int i=0;i<points.size();i++)
					{
					Vector3i v=points.get(i);
					r.previewPoints[i]=stack.transformImageWorld(new Vector3d(v.x,v.y,v.z));
					}
				}
			}
		else
			r.previewPoints=null;
		}
	
	
	
	
	
	
	
	
	
	private WeakReference<Network> editingObject=new WeakReference<Network>(null);
	private void setEditObject(Network lin)
		{
		editingObject=new WeakReference<Network>(lin);
		}
	
	public NetworkImageToolTracer(final ImageWindow w, NetworkImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	
	
	
	
	
	private void fillMenu(JComponent menu)
		{
		
		final JCheckBoxMenuItem miAuto=new JCheckBoxMenuItem("Semi-automatic",useAuto);
		miAuto.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				useAuto=miAuto.isSelected();
				BasicWindow.updateWindows();
				}
			});
		
		
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
				w.setTool(NetworkImageToolTracer.this);
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
		removeTracer();
		}

	public void mouseClicked(MouseEvent e, Component invoker)
		{
		if(useAuto)
			mouseClickedTrace(e, invoker);
		else
			mouseClickedManual(e, invoker);
		}
	

	public void mouseClickedManual(MouseEvent e, Component invoker)
		{
		}

	public void mouseClickedTrace(MouseEvent e, Component invoker)
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
					getTracer(stack, nf);
					}
				else
					{
					//TODO do not allow this to be done unless thread has calculated the tracer already
					

					NetworkTracerInterface auto=getTracer(stack, nf);
					
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
					setForcedStartingPoint(null);
					getTracer(stack, nf);
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
							setForcedStartingPoint(new Vector3d(xy.x,xy.y,z));
							getTracer(stack, nf);
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
							setForcedStartingPoint(null);
							getTracer(stack, nf);
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
		if(useAuto)
			mouseMovedTrace(e, dx, dy);
		else
			mouseMovedManual(e, dx, dy);
		}

	public void mouseMovedManual(MouseEvent e, int dx, int dy)
		{
		
		}
	
	public void mouseMovedTrace(MouseEvent e, int dx, int dy)
		{
		if(hasTracer())
			{
			recalcTrace(e);
			w.repaint();
			}
		}

	

	
	public void keyPressed(KeyEvent e)
		{
		
		}


	
	public void paintComponent(Graphics g)
		{

		}
	
	public void keyReleased(KeyEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		}

	
	}
