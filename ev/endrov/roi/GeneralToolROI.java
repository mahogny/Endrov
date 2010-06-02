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

import endrov.basicWindow.WSTransformer;
import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.imageWindow.*;
import endrov.util.EvDecimal;

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
	
//	public void deselected() {}
	
	public GeneralToolROI(ImageWindowInterface w)
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
		if(SwingUtilities.isRightMouseButton(e))
			{
			//If a ROI is beneath then open a pop-up menu
			
			
			}

		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(active)
			{
			if(currentHandle!=null)
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
			else
				{
				//TODO be able to move an entire ROI

				
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
						
						//TODO select ROI as well
						
						return;
						}
					}
			
			//If no handle then maybe the user hit a ROI head on?
			
			
			}
		}

	/*
	public ROI hitROI()
		{
		ROI roi=hitROIrecursive(frame, z, channel, con);
		}
	
	
	private ROI hitROIrecursive(EvDecimal frame, EvDecimal z, String channel, EvContainer con)
		{
		for(Map.Entry<String, EvObject> e:con.metaObject.entrySet())
			{
			if(e.getValue() instanceof ROI)
				if(testHitROI(w, (ROI)e.getValue(), e.getKey(), frame, z, channel))
					return (ROI)e.getValue();
			ROI roi=hitROIrecursive(frame, z, channel, e.getValue());
			if(roi!=null)
				return roi;
			}
		return null;
		}
	
	private boolean testHitROI(WSTransformer w, ROI roiUncast, String roiName, EvDecimal frame, EvDecimal z, String channel)
		{
		
		return false;
		}*/
	
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
