/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageWindow.tools;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

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
	/*
	private final ImageWindowInterface w;
	
	public ImageWindowToolChannelDisp(ImageWindowInterface w)
		{
		this.w=w;
		}
		*/
	public JMenuItem getMenuItem(final ImageWindow w)
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Channel/Displacement");
		mi.setSelected(w.getTool()==this);
		final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		return mi;
		}
	
	public void mouseDragged(ImageWindow w, MouseEvent e, int dx, int dy)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			Imageset rec=w.getImageset();
			EvChannel c=w.getSelectedChannel();
			
			double ddx=dx/w.getZoom(); //To world coordinates
			double ddy=dy/w.getZoom(); 
			if(c!=null)
				{
				for(Map.Entry<EvDecimal, EvStack> frames:c.imageLoader.entrySet())
					{
					EvStack stack=frames.getValue();
					stack.dispX+=stack.scaleWorldImageX(ddx); 
					stack.dispY+=stack.scaleWorldImageY(ddy); 
			
					//stack.dispX+=ddx;
					//stack.dispY+=ddy;
					
					
					c.defaultDispX=stack.dispX;
					c.defaultDispY=stack.dispY;
					
					//mark metadata dirty?
					}
				
				BasicWindow.updateWindows();
				rec.setMetadataModified();
				}
			}
		}
	public void mouseClicked(ImageWindow w, MouseEvent e) {}
	public void mousePressed(ImageWindow w, MouseEvent e) {}
	public void mouseReleased(ImageWindow w, MouseEvent e) {}
	public void mouseMoved(ImageWindow w, MouseEvent e, int dx, int dy) {}
	public void mouseExited(ImageWindow w, MouseEvent e) {}
	public void keyPressed(ImageWindow w, KeyEvent e) {}
	public void keyReleased(ImageWindow w, KeyEvent e) {}
	public void paintComponent(ImageWindow w, Graphics g) {}
	public void deselected(ImageWindow w) {}
	}

