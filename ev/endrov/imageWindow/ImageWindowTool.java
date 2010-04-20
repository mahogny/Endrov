/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageWindow;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;

/**
 * A selectable tool in the image window
 */
public interface ImageWindowTool
	{
	/**
	 * Get the menu item to be placed in the tools menu
	 */
	public JMenuItem getMenuItem(ImageWindow w);

	/**
	 * Called when the tool has been deselected
	 */
	public void deselected(ImageWindow w);
	
	public void mouseClicked(ImageWindow w, MouseEvent e);
	public void mousePressed(ImageWindow w, MouseEvent e);
	public void mouseReleased(ImageWindow w, MouseEvent e);
	public void mouseDragged(ImageWindow w, MouseEvent e, int dx, int dy);
	public void paintComponent(ImageWindow w, Graphics g);
	public void mouseMoved(ImageWindow w, MouseEvent e, int dx, int dy);
	public void keyPressed(ImageWindow w, KeyEvent e);
	public void keyReleased(ImageWindow w, KeyEvent e);
	public void mouseExited(ImageWindow w, MouseEvent e);
	}
