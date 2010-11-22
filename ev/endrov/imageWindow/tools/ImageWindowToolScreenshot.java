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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import endrov.basicWindow.BasicWindow;
import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowTool;
import endrov.util.EvFileUtil;

//either send down variables or add accessors to imagewindow

/**
 * Get a screenshot of the current display
 * @author Johan Henriksson
 */
public class ImageWindowToolScreenshot implements ImageWindowTool, ActionListener
	{
	JMenu m=new JMenu("Screenshot");
	JMenuItem miOverlay=new JMenuItem("As shown with overlay");
	JMenuItem miOrig=new JMenuItem("Get original data");

	private final ImageWindow w;
	
	public ImageWindowToolScreenshot(final ImageWindow w)
		{
		this.w=w;
		}
	
	public JMenuItem getMenuItem()
		{
		m.removeAll();
		m.add(miOverlay);
		m.add(miOrig);
		miOverlay.removeActionListener(this);
		miOverlay.addActionListener(this);
		miOrig.removeActionListener(this);
		miOrig.addActionListener(this);
		return m;
		}
	
	public void actionPerformed(ActionEvent arg0)
		{
		BufferedImage image;
		if(arg0.getSource()==miOverlay)
			{
			image=w.getScreenshotOverlay();
			}
		else
			{
			image=w.getScreenshotOriginal();
			if(image==null)
				BasicWindow.showErrorDialog("No picture to store!");
			}
		
		//Choose location and store
		JFileChooser fc=new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ret=fc.showSaveDialog(w);
		if(ret==JFileChooser.APPROVE_OPTION)
			{
			File f=fc.getSelectedFile();
			try
				{
				ImageIO.write(image, "png", EvFileUtil.makeFileEnding(f, ".png"));
				}
			catch (IOException e2)
				{
				e2.printStackTrace();
				}
			}

		}
	
	public void deselected(){}
	
	public void mouseClicked(MouseEvent e, Component invoker){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	public void mouseDragged(MouseEvent e, int dx, int dy){}
	public void paintComponent(Graphics g){}
	public void mouseMoved(MouseEvent e, int dx, int dy){}
	public void keyPressed(KeyEvent e){}
	public void keyReleased(KeyEvent e){}
	public void mouseExited(MouseEvent e){}
	}
