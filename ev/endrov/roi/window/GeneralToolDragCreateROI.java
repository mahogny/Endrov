/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi.window;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;
import javax.vecmath.Vector2d;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvContainer;
import endrov.imageWindow.GeneralTool;
import endrov.imageWindow.ImageWindowInterface;
import endrov.roi.ImageRendererROI;
import endrov.roi.ROI;
import endrov.util.EvDecimal;

/**
 * Image window tool: initial placement of a ROI
 * @author Johan Henriksson
 */
public class GeneralToolDragCreateROI implements GeneralTool
	{
	private final ImageWindowInterface w;
	private final ROI roi;
	private boolean active=false;
	private ImageRendererROI renderer;
	
	
	public GeneralToolDragCreateROI(ImageWindowInterface w, ROI roi, ImageRendererROI renderer)
		{
		this.w=w;
		this.roi=roi;
		this.renderer=renderer;
		}
	
	/*
	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Placing ROI");
		mi.setSelected(w.getTool()==this);
		//final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(GeneralToolDragCreateROI.this);}
		});
		return mi;
		}

	*/
	
	public void deselected()
		{
		setRendererROI(null);
		}

	
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(active)
			{
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			roi.getPlacementHandle2().setPos(v.x, v.y);
			w.updateImagePanel();
			}
		}
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			//Start making a nucleus
			active=true;
			setRendererROI(roi);
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			roi.getPlacementHandle1().setPos(v.x, v.y);
			roi.getPlacementHandle2().setPos(v.x, v.y);
			EvDecimal curFrame=w.getFrame();
			EvDecimal curZ=w.getZ();
			roi.initPlacement(w.getCurrentChannelName(),curFrame,curZ);
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
			//Cancel making nucleus
			active=false;
			w.updateImagePanel();
			w.unsetTool();
			}
		}
	public void mouseReleased(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && active)
			{
			EvContainer rec=w.getRootObject();
			rec.addMetaObject(roi);
			roi.openEditWindow();
			active=false;
			w.unsetTool();
			w.updateImagePanel();
			BasicWindow.updateWindows();
			}
		}
	
	void setRendererROI(ROI roi)
		{
		renderer.drawROI=roi;
		}
	
	
	public void mouseMoved(MouseEvent e, int dx, int dy) {}
	public void mouseExited(MouseEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void paintComponent(Graphics g) {}
	
	
	
	}

