/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer2D.basicExtensions;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.vecmath.Vector2d;

import endrov.core.log.EvLog;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.math.EvDecimal;
import endrov.windowConsole.ConsoleWindow;
import endrov.windowViewer2D.Viewer2DWindow;
import endrov.windowViewer2D.Viewer2DTool;

/**
 * Get information about a pixel
 * @author Johan Henriksson
 */
public class ImageWindowToolPixelInfo implements Viewer2DTool
	{
	private final Viewer2DWindow w;
	
	public ImageWindowToolPixelInfo(Viewer2DWindow w)
		{
		this.w=w;
		}
	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Pixel information");
		mi.setSelected(w.getTool()==this);
		final Viewer2DTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		return mi;
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		}
	
	public void mouseClicked(MouseEvent e, Component invoker)
		{
		EvChannel c=w.getCurrentChannel();
		if(c!=null)
			{
			EvDecimal frame=w.getFrame();
			EvDecimal slice=w.getZ();
			frame=c.closestFrame(frame);
			EvStack stack=c.getStack(frame);
			int closestZ=stack.getClosestPlaneIndex(slice.doubleValue());
			
			Vector2d mpos=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
			Vector2d ppos=stack.transformWorldImage(mpos);
			int px=(int)ppos.x;
			int py=(int)ppos.y;
			
			ConsoleWindow.openConsole();
			
			EvImagePlane image=stack.getPlane(closestZ);
			if(image!=null)
				{
				if(px<stack.getWidth() && py<stack.getHeight() && px>=0 && py>=0)
					{
					EvPixels pix=image.getPixels(new ProgressHandle()).getReadOnly(EvPixelsType.DOUBLE);
					EvLog.printLog("Pixel ("+px+" "+py+") Intensity: "+pix.getArrayDouble()[pix.getPixelIndex(px, py)]);
					}
				else
					EvLog.printLog("Outside image, world coordinates: "+mpos+" image coordinates: "+ppos);
				}
			else
				EvLog.printLog("No image at this Z");
			}
		}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseMoved(MouseEvent e, int dx, int dy) {}
	public void mouseExited(MouseEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void paintComponent(Graphics g) {}
	public void deselected() {}
	}

//TODO: for channel displacement. mark dirty imageset, save
