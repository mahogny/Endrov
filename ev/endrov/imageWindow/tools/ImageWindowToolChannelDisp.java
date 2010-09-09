/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageWindow.tools;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import endrov.basicWindow.BasicWindow;
import endrov.imageWindow.ImageWindow;
//import endrov.imageWindow.ImageWindowInterface;
import endrov.imageWindow.ImageWindowTool;
import endrov.imageset.EvChannel;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.util.EvDecimal;

/**
 * Change displacement of a channel
 * @author Johan Henriksson
 */
public class ImageWindowToolChannelDisp implements ImageWindowTool
	{
	private final ImageWindow w;
	
	public ImageWindowToolChannelDisp(ImageWindow w)
		{
		this.w=w;
		}
	
	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Channel/Displacement");
		mi.setSelected(w.getTool()==this);
		final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		return mi;
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			Imageset rec=w.getImageset();
			EvChannel c=w.getSelectedChannel();
			
			Vector2d diff=w.transformVectorS2W(new Vector2d(dx,dy));
			
			if(c!=null)
				{
				for(Map.Entry<EvDecimal, EvStack> frames:c.imageLoader.entrySet())
					{
					EvStack stack=frames.getValue();
					//Vector3d d=stack.scaleWorldImage(new Vector3d(diff.x,diff.y,0));
					
					Vector3d disp=stack.getDisplacement();
					disp.sub(new Vector3d(diff.x, diff.y, 0));
					stack.setDisplacement(disp);
//					stack.dispX+=diff.x;
//					stack.dispY+=diff.y;
					
					//TODO handle rotation of stacks?
					}
				
				BasicWindow.updateWindows();
				rec.setMetadataModified();
				}
			}
		}
	public void mouseClicked(MouseEvent e, Component invoker) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseMoved(MouseEvent e, int dx, int dy) {}
	public void mouseExited(MouseEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void paintComponent(Graphics g) {}
	public void deselected() {}
	}

