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
public class NetworkImageToolManual implements ImageWindowTool, ActionListener
	{
	private final ImageWindow w;
	private final NetworkImageRenderer r;
		
	
	
	


	
	private Vector3d forcedStartingPoint=null;
	private WeakReference<EvStack> lastStack=new WeakReference<EvStack>(null);

	

	
	public void setForcedStartingPoint(Vector3d v)
		{
		if(!EV.equalsHandlesNull(forcedStartingPoint,v))
			{
			forcedStartingPoint=v;
			lastStack=new WeakReference<EvStack>(null);
			}
		}

	
	
	
	
	
	
	
	
	private WeakReference<Network> editingObject=new WeakReference<Network>(null);
	private void setEditObject(Network lin)
		{
		editingObject=new WeakReference<Network>(lin);
		}
	
	public NetworkImageToolManual(final ImageWindow w, NetworkImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	
	
	
	

	public JMenuItem getMenuItem()
		{
		return null;
		}
	public void actionPerformed(ActionEvent e)
		{
		}
	
	public void deselected()
		{
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
				
				
				

				BasicWindow.updateWindows();
				}
					
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
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
		/*
		if(hasTracer())
			{
			recalcTrace(e);
			w.repaint();
			}
			*/
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
