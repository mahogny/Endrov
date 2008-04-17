package evplugin.roi.window;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.vecmath.Vector2d;

import evplugin.basicWindow.BasicWindow;
import evplugin.imageWindow.ImageWindow;
import evplugin.imageWindow.ImageWindowRenderer;
import evplugin.imageWindow.ImageWindowTool;
import evplugin.imageset.Imageset;
import evplugin.roi.ImageRendererROI;
import evplugin.roi.ROI;

/**
 * Image window tool: initial placement of a ROI
 * @author Johan Henriksson
 */
public class ToolDragCreateROI implements ImageWindowTool
	{
	private final ImageWindow w;
	private final ROI roi;
	private boolean active=false;
	
	
	
	public ToolDragCreateROI(ImageWindow w, ROI roi)
		{
		this.w=w;
		this.roi=roi;
		}
	
	/*
	public boolean isToggleable()
		{
		return true;
		}
	public String toolCaption()
		{
		return "Placing ROI";
		}
	public boolean enabled()
		{
		return true;
		}*/
	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Placing ROI");
		mi.setSelected(w.getTool()==this);
		final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		return mi;
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
			double curFrame=w.frameControl.getFrame();
			double curZ=w.frameControl.getZ();
			roi.initPlacement(w.getCurrentChannelName(),curFrame,curZ);
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
			//Cancel making nucleus
			active=false;
			w.updateImagePanel();
			w.setTool(null);
			}
		}
	public void mouseReleased(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && active)
			{
			Imageset rec=w.getImageset();
			rec.addMetaObject(roi);
			roi.openEditWindow();
			active=false;
			w.setTool(null);
			w.updateImagePanel();
			BasicWindow.updateWindows();
			}
		}
	
	private void setRendererROI(ROI roi)
		{
		for(ImageWindowRenderer rend:w.imageWindowRenderers)
			if(rend instanceof ImageRendererROI)
				((ImageRendererROI)rend).drawROI=roi;
		}
	
	
	public void mouseMoved(MouseEvent e, int dx, int dy) {}
	public void mouseExited(MouseEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void paintComponent(Graphics g) {}
	public void unselected()
		{
		setRendererROI(null);
		}
	
	
	
	}

//TODO: for channel displacement. mark dirty imageset, save
