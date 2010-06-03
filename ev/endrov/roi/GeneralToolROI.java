/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.vecmath.*;

import endrov.basicWindow.BasicWindow;
import endrov.imageWindow.*;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * Make nuclei by dragging an area. Also move nuclei.
 *
 * @author Johan Henriksson
 */
public class GeneralToolROI implements GeneralTool//implements ImageWindowTool
	{
	private boolean active=false;
	private ROI currentROI=null;
	private String currentHandle=null;

	private final ImageWindowInterface w;

	
	public GeneralToolROI(ImageWindowInterface w)
		{
		this.w=w;
		}

	
	public Tuple<String,ROI> getRoiBeneath(MouseEvent e)
		{
		Vector2d v=new Vector2d(e.getX(), e.getY());
		v=w.transformS2W(v);
		
		EvDecimal z=w.getZ();
		EvDecimal frame=w.getFrame();
		String channel=w.getCurrentChannelName();
		
		for(Map.Entry<String,ROI> sr:w.getRootObject().getIdObjects(ROI.class).entrySet())
			{
//			System.out.println("testing "+sr.getKey());
			if(sr.getValue().pointInRange(channel, frame, v.x, v.y, z))
				return new Tuple<String, ROI>(sr.getKey(),sr.getValue());
			}
		return null;
		}
	
	public void mouseClicked(MouseEvent e, Component invoker)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			Tuple<String,ROI> t=getRoiBeneath(e);
			if(t!=null)
				{
				ROI.setSelected(Arrays.asList(t.snd()));
				//BasicWindow.updateWindows();
				}
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
			//If a ROI is beneath then open a pop-up menu
			Tuple<String,ROI> t=getRoiBeneath(e);
			if(t!=null)
				{
				JPopupMenu menu=new JPopupMenu();
				final String roiName=t.fst();
				
				JMenuItem miDelete=new JMenuItem("Delete");
				menu.add(miDelete);
				
				miDelete.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent arg0)
						{
						w.getRootObject().metaObject.remove(roiName);
						ROI.setSelected(new LinkedList<ROI>());
						currentROI=null;
						BasicWindow.updateWindows();
						}
					});
		
				menu.show(invoker, e.getX(), e.getY());
				}
			
			
			}

		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(active)
			{
			if(currentHandle!=null)
				{
				//Move a handle
				for(ROI.Handle h:currentROI.getHandles())
					if(h.getID().equals(currentHandle))
						{
						Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
						h.setPos(v.x,v.y);
						w.updateImagePanel();
						return;
						}
				}
			else
				{
				//Move an entire ROI
				double wdx=w.scaleS2w(dx);
				double wdy=w.scaleS2w(dy);
				ROI.Handle h1=currentROI.getPlacementHandle1();
				if(h1!=null)
					{
					double x=h1.getX();
					double y=h1.getY();
					h1.setPos(x+wdx, y+wdy);
					}
				ROI.Handle h2=currentROI.getPlacementHandle2();
				if(h2!=null)
					{
					double x=h2.getX();
					double y=h2.getY();
					h2.setPos(x+wdx, y+wdy);
					}
				w.updateImagePanel();
				}
			
			}
		}
	
	private boolean mouseOverHandle(MouseEvent e, ROI.Handle h)
		{
		Vector2d so=w.transformW2S(new Vector2d(h.getX(),h.getY()));
		return Math.abs(so.x-e.getX())<ImageRendererROI.HANDLESIZE && Math.abs(so.y-e.getY())<ImageRendererROI.HANDLESIZE;
		}
	
	public void mousePressed(MouseEvent e)
		{
		ImageRendererROI r=w.getRendererClass(ImageRendererROI.class);
		
		if(SwingUtilities.isLeftMouseButton(e))
			{
			//Which handle?
			for(Map.Entry<ROI, Map<String,ROI.Handle>> re:r.handleList.entrySet())
				for(Map.Entry<String, ROI.Handle> rh:re.getValue().entrySet())
					{
					if(mouseOverHandle(e, rh.getValue()))
						{
						active=true;
						currentHandle=rh.getKey();
						currentROI=re.getKey();
						return;
						}
					}
			
			//If no handle then maybe the user hit a ROI head on?
			Tuple<String,ROI> t=getRoiBeneath(e);
			if(t!=null)
				{
				active=true;
				currentROI=t.snd();
				currentHandle=null;
				}
			
			}
		}

	
	public void mouseReleased(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && active)
			{
			active=false;
			w.updateImagePanel();
			}
		}

	public void mouseMoved(MouseEvent e, int dx, int dy)
		{			
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
		active=false;
		}

	
	}
