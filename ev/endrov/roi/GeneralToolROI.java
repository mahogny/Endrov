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

import endrov.imageWindow.*;

/**
 * Make nuclei by dragging an area. Also move nuclei.
 *
 * @author Johan Henriksson
 */
public class GeneralToolROI //implements ImageWindowTool
	{
	private boolean active=false;
	private ROI currentROI=null;
	private String currentHandle=null;

	private final ImageWindowInterface w;
	
//	public void deselected() {}
	
	public GeneralToolROI(ImageWindow w)
		{
		this.w=w;
		}

	/*
	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("ROI/Edit");
		mi.setSelected(w.getTool()==this);
		final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		return mi;
		}*/
	
	
	public void mouseClicked(MouseEvent e)
		{
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(active)
			{
			for(ROI.Handle h:currentROI.getHandles())
				if(h.getID().equals(currentHandle))
					{
					Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
					h.setPos(v.x,v.y);
					w.updateImagePanel();
					return;
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
