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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.vecmath.*;
import javax.swing.*;


import endrov.basicWindow.*;
import endrov.data.EvContainer;
import endrov.ev.EV;
import endrov.imageWindow.*;
import endrov.imageset.EvStack;
import endrov.network.Network.NetworkFrame;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;
import endrov.util.Maybe;
import endrov.util.ProgressHandle;
import endrov.util.Vector3i;

/**
 * Make and edit networks
 *
 * @author Johan Henriksson
 */
public class NetworkImageTool implements ImageWindowTool, ActionListener
	{
	private final ImageWindow w;
	private final NetworkImageRenderer r;
		
	public boolean useAuto=false;

	
	private boolean ignoreMouseRelease=false;
	




	public static Vector<NetworkTracerFactory> tracers=new Vector<NetworkTracerFactory>();
	static
		{
		tracers.add(NetworkTracerSemiauto.factory);
		}
	private NetworkTracerFactory tracerFactory=NetworkTracerSemiauto.factory;
	
	private Maybe<Vector3d> forcedStartingPoint=null;
	private NetworkTracerInterface lastAuto=null;
	private WeakReference<EvStack> lastStack=new WeakReference<EvStack>(null);

	private Integer editingPoint=null;


	public boolean hasTracer()
		{
		return lastAuto!=null;
		}
	
	public void setForcedStartingPointXYZ(Maybe<Vector3d> v)
		{
		if(!EV.equalsHandlesNull(forcedStartingPoint,v))
			{
			forcedStartingPoint=v;
			lastAuto=null;
			lastStack=new WeakReference<EvStack>(null);
			}
		}
	
	/*
	private Maybe<Integer> forceStartID
	private void setForcedStartingPointID(Maybe<Integer> just)
		{
		// TODO Auto-generated method stub
		
		}
		*/


	
	
	/**
	 * Get the automatic tracer and ensure it is up to date. If no tracer is enabled then returns null
	 */
	public NetworkTracerInterface getTracer(EvStack stack, Network.NetworkFrame nf)
		{
		if(useAuto)
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
				LinkedList<Vector3i> list=new LinkedList<Vector3i>();
				if(forcedStartingPoint!=null && forcedStartingPoint.get()!=null)
					{
					Vector3d v=stack.transformWorldImage(forcedStartingPoint.get());
					list.add(new Vector3i((int)v.x,(int)v.y,(int)v.z));
					}
				auto.preprocess(new ProgressHandle(), stack, list);
				}
			System.out.println("Calc done");
			return auto;
			}
		else
			return null;
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
	
	public NetworkImageTool(final ImageWindow w, NetworkImageRenderer r)
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
		menu.add(miAuto);
		
		
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
		
		
		
		
		
		
		
		
		JMenuItem miMakeProfile=new JMenuItem("Make intensity-radius profile");
		miMakeProfile.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				EvDecimal frame=w.getFrame();
				Network editNetwork=editingObject.get();
				if(editNetwork==null)
					return;
				Network.NetworkFrame nf=editNetwork.frame.get(frame);
				EvStack stack=r.getCurrentStack();
				if(nf==null || stack==null)
					return;
				
				String sRadius=JOptionPane.showInputDialog("Radius (null for max)");
				if(sRadius==null)
					return;
				
				Double maxRadius;
				if(sRadius.equals(""))
					maxRadius=NetworkIntensityRadiusHistogram.maxRadius(nf);
				else
					maxRadius=Double.parseDouble(sRadius);
				
				String sBins=JOptionPane.showInputDialog("Number of bins (null for 100)");
				if(sBins==null)
					return;
				int histBins=100;
				if(!sBins.equals(""))
					histBins=Integer.parseInt(sBins);
				
				NetworkIntensityRadiusHistogram hist=new NetworkIntensityRadiusHistogram(nf, stack, maxRadius, histBins);
				
				StringBuffer sbIntensityProf=new StringBuffer();
				for(int i=0;i<hist.intensity.length;i++)
					sbIntensityProf.append(""+(i*hist.intensityDr)+"\t"+hist.intensity[i]+"\n");
				
				
				StringBuffer sbHist=new StringBuffer();
				Map<Double,Integer> histIntensity=new TreeMap<Double, Integer>(hist.histIntensity.hist);
				for(Map.Entry<Double, Integer> e2:histIntensity.entrySet())
					sbHist.append(""+e2.getKey()+"\t"+e2.getValue()+"\n");
				
				
				JFileChooser fc=new JFileChooser();
				int ret=fc.showSaveDialog(w);
				if(ret==JFileChooser.APPROVE_OPTION)
					{
					File f=fc.getSelectedFile();
					try
						{
						EvFileUtil.writeFile(new File(f.getParentFile(),f.getName()+".prof.txt"), sbIntensityProf.toString());
						EvFileUtil.writeFile(new File(f.getParentFile(),f.getName()+".hist.txt"), sbHist.toString());
						}
					catch (IOException e1)
						{
						BasicWindow.showErrorDialog("Error writing file: "+e1.getMessage());
						e1.printStackTrace();
						}
					}
				}
		});
		menu.add(miMakeProfile);
		
		
		
		
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
		removeTracer();
		}

	public void mouseClicked(MouseEvent e, Component invoker)
		{
		if(useAuto)
			mouseClickedTrace(e, invoker);
		else
			mouseClickedManual(e, invoker);
		}
	

	public void mouseClickedManual(final MouseEvent e, Component invoker)
		{
		if(SwingUtilities.isRightMouseButton(e))
			{
			
			
			JPopupMenu menu=new JPopupMenu();

			
			EvDecimal frame=w.getFrame();
			Network editNetwork=editingObject.get();
			final Network.NetworkFrame nf=editNetwork.frame.get(frame);
			
			//final Vector2d xy=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
			//final double z=w.getZ().doubleValue();

			
			if(nf!=null)
				{

				final Vector3d pos=r.getMousePosWorld(e);
				//if(r.previewPoints!=null)

				final Integer closestID=getClosestPointID(nf, pos);
				if(closestID!=null)
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
								setForcedStartingPointXYZ(Maybe.just(nf.points.get(closestID).toVector3d()));
								//setForcedStartingPointID(Maybe.just(closestID));
								w.repaint();
								}
							}

						});
					
					JMenuItem miResize=new JMenuItem("Resize");
					miResize.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent a)
							{
							resize(e);
							}
						});
					
					
					JMenuItem miForceAnew=new JMenuItem("Start anew");
					miForceAnew.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent a)
							{
							EvStack stack=r.getCurrentStack();
							if(stack!=null)
								{
								setForcedStartingPointXYZ(Maybe.just((Vector3d)null));
								getTracer(stack, nf);
								w.repaint();
								}
							}
						});
					
					menu.add(miForcePoint);
					menu.add(miResize);
					menu.add(miForceAnew);
					
					}
				
				}
			
			
			
			menu.show(e.getComponent(),e.getX(),e.getY());
			
			}
		

		}

	public void mouseClickedTrace(final MouseEvent e, Component invoker)
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
					setForcedStartingPointXYZ(null);
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

				JMenuItem miForcePoint=new JMenuItem("Start from here");
				miForcePoint.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent a)
						{
						EvStack stack=r.getCurrentStack();
						if(stack!=null)
							{
							setForcedStartingPointXYZ(Maybe.just(new Vector3d(xy.x,xy.y,z)));
							getTracer(stack, nf);
							w.repaint();
							}
						}
					});
				
				
				JMenuItem miResize=new JMenuItem("Resize");
				miResize.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent a)
						{
						resize(e);
						}
					});
				

				JMenuItem miForceNone=new JMenuItem("Start from closest point");
				miForceNone.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent a)
						{
						EvStack stack=r.getCurrentStack();
						if(stack!=null)
							{
							setForcedStartingPointXYZ(null);
							getTracer(stack, nf);
							w.repaint();
							}
						}
					});

				
				JMenuItem miForceAnew=new JMenuItem("Start anew");
				miForceAnew.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent a)
						{
						EvStack stack=r.getCurrentStack();
						if(stack!=null)
							{
							setForcedStartingPointXYZ(Maybe.just((Vector3d)null));
							getTracer(stack, nf);
							w.repaint();
							}
						}
					});

				
				menu.add(miForcePoint);
				menu.add(miResize);
				menu.add(miForceAnew);
				menu.add(miForceNone);
				}

			
			menu.show(e.getComponent(),e.getX(),e.getY());
			}
		}
	
	
	private void resize(MouseEvent e)
		{
		EvDecimal frame=w.getFrame();
		Network network=editingObject.get();
		if(network!=null)
			{

			Vector3d pos=r.getMousePosWorld(e);

			

				NetworkFrame nf=network.frame.get(frame);
				
				Integer closestID=getClosestPointID(nf, pos);
				if(closestID!=null)
					{
					
					
					
					editingPoint=closestID;
					
					
					//TODO what about undo??
					}
				
			}
		
		
		}
	
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		tryMoveEditingPoint(dx,dy);
		}
	
	
	
	private boolean tryMoveEditingPoint(int dx, int dy)
		{
		if(!useAuto)
			{
			////hmmm... final action should be save the last state? how does this fit together with undo?

			final EvDecimal frame=w.getFrame();
			final Network network=editingObject.get();
			if(network!=null)
				{
				if(editingPoint!=null)
					{

					NetworkFrame nf=network.frame.get(frame);
					Network.Point p=nf.points.get(editingPoint);
					
					if(p.r==null)
						p.r=0.0;
					double dist=w.s2wz(dy);//w.scaleS2w(Math.sqrt(dx*dx+dy*dy));
					p.r+=dist;
					if(p.r<=0)
						p.r=null;
					w.repaint();
					
					return true;
					}

				}

			}
		return false;
		}
	
	
	public void mousePressed(MouseEvent e)
		{
		if(!useAuto)
			mousePressedManual(e);
		}

	
	
	public void mousePressedManual(MouseEvent e)
		{
		if(storeEditingPoint())
			{
			ignoreMouseRelease=true;
			return;
			}
		
		
		if(SwingUtilities.isLeftMouseButton(e))
			{
			final EvDecimal frame=w.getFrame();
			final Network network=editingObject.get();
			if(network!=null)
				{

				final Vector3d pos=r.getMousePosWorld(e);
				//if(r.previewPoints!=null)

				System.out.println("pos world "+pos);
				
				NetworkFrame nf=network.frame.get(frame);
				//final Integer closestID=getClosestPointID(nf, pos);
				
				
				
				Vector3d startFrom;
				if(forcedStartingPoint!=null)
					startFrom=forcedStartingPoint.get();
				else
					{
					Integer closestID=getClosestPointID(nf, pos);
					if(closestID!=null)
						startFrom=nf.points.get(closestID).toVector3d();
					else
						startFrom=null;
					}
				final Vector3d startFromFinal=startFrom;
				
				//TODO could add an override start pos here
					
				new UndoOpNetworkReplaceFrame(frame, network, "Create point")
					{
					public void redo()
						{
						Network.Point p=new Network.Point(pos,null);
						
						//Add the point
						NetworkFrame nf=network.frame.get(frame);
						int newPointID=nf.putNewPoint(p);
						
						if(startFromFinal!=null)
							{

							//Link to previous point
							int lastID=nf.getPointIDByPos(startFromFinal.x, startFromFinal.y, startFromFinal.z);
							
							Network.Segment s=new Network.Segment();
							s.points=new int[]{lastID, newPointID};
							nf.segments.add(s);

							}
						
						
						//Start editing point
						editingPoint=newPointID;
						
						//No longer force this point
						forcedStartingPoint=null;
						
						BasicWindow.updateWindows();
						}
					}.execute();
					
				}
			}
		}
	
	public void mouseReleased(MouseEvent e)
		{
		if(ignoreMouseRelease)
			ignoreMouseRelease=false;
		else
			storeEditingPoint();
		}
	
	public boolean storeEditingPoint()
		{
		if(editingPoint!=null)
			{
			
			//TODO store state here?
			editingPoint=null;
			return true;
			}
		else
			return false;
		}

	
	public void mouseMoved(MouseEvent e, int dx, int dy)
		{
		if(useAuto)
			mouseMovedTrace(e, dx, dy);
		else
			mouseMovedManual(e, dx, dy);
		}

	
	public Integer getClosestPointID(Network.NetworkFrame nf, Vector3d curPos)
		{
		Integer id=null;
		double closestDist2=Double.MAX_VALUE;
		for(Map.Entry<Integer, Network.Point> e:nf.points.entrySet())
			{
			Network.Point p=e.getValue();
			double ddx=p.x-curPos.x;
			double ddy=p.y-curPos.y;
			double ddz=p.z-curPos.z;
			double dist2=ddx*ddx+ddy*ddy+ddz*ddz;
			if(dist2<closestDist2)
				{
				id=e.getKey();
				closestDist2=dist2;
				}
			}
		return id;
		}
	
	public void mouseMovedManual(MouseEvent e, int dx, int dy)
		{
		tryMoveEditingPoint(dx,dy);
		
		
		r.previewPoints=null;
		
		if(editingPoint==null)
			{

			EvDecimal frame=w.getFrame();
			Network editNetwork=editingObject.get();
			Network.NetworkFrame nf=editNetwork.frame.get(frame);
			if(nf==null)
				editNetwork.frame.put(frame,nf=new Network.NetworkFrame());
			
			Vector3d curPos=r.getMousePosWorld(e);

			
			if(forcedStartingPoint!=null)
				{
				if(forcedStartingPoint.get()!=null)
					{
				//From-To
					r.previewPoints=new Vector3d[]{forcedStartingPoint.get(), curPos};
					}
				}
			else if(!nf.points.isEmpty())
				{
				//Find closest point
				Network.Point closestPoint=null;
				double closestDist2=Double.MAX_VALUE;
				for(Network.Point p:nf.points.values())
					{
					double ddx=p.x-curPos.x;
					double ddy=p.y-curPos.y;
					double ddz=p.z-curPos.z;
					double dist2=ddx*ddx+ddy*ddy+ddz*ddz;
					if(dist2<closestDist2)
						{
						closestPoint=p;
						closestDist2=dist2;
						}
					}
				if(closestPoint!=null)
					{
					//From-To
					r.previewPoints=new Vector3d[]{closestPoint.toVector3d(), curPos};
					}
				}
			w.repaint();
			}
		
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
