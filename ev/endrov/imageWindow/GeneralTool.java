/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageWindow;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * A selectable tool in the image window
 */
public interface GeneralTool
	{
	public void mouseClicked(MouseEvent e);
	public void mousePressed(MouseEvent e);
	public void mouseReleased(MouseEvent e);
	public void mouseDragged(MouseEvent e, int dx, int dy);
	public void paintComponent(Graphics g);
	public void mouseMoved(MouseEvent e, int dx, int dy);
	public void keyPressed(KeyEvent e);
	public void keyReleased(KeyEvent e);
	public void mouseExited(MouseEvent e);
	}
