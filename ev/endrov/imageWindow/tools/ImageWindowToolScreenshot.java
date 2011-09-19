/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageWindow.tools;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.FrameControl;
import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowTool;
import endrov.util.EvFileUtil;
import endrov.util.ProgressHandle;

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
	
	private static String lastFile=null;
	
	public void actionPerformed(ActionEvent arg0)
		{
		Map<String,BufferedImage> image=new HashMap<String, BufferedImage>();
		if(arg0.getSource()==miOverlay)
			{
			image.put("overlay",w.getScreenshotOverlay());
			}
		else
			{
			//TODO  allow cancel if it is too slow
			image=w.getScreenshotOriginal(new ProgressHandle());
			if(image==null)
				BasicWindow.showErrorDialog("No picture to store!");
			}
		
		//Choose location
		FileDialog fd=new FileDialog((Frame)null, "Save screenshot", FileDialog.SAVE);
		if(lastFile!=null)
			fd.setDirectory(lastFile);
		
		//Suggest location
		fd.setFile(FrameControl.formatTime(w.getFrame())+"-"+w.getZ());
	
		fd.setVisible(true);
		String thisFile=fd.getFile();
		if(thisFile!=null)
			{
			lastFile=fd.getDirectory();
				try
					{
					for(String chname:image.keySet())
						{
						File f=new File(fd.getDirectory(),fd.getFile()+"-"+chname);
						ImageIO.write(image.get(chname), "png", EvFileUtil.makeFileEnding(f, ".png"));
						}
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
