/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindow;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

/**
 * Listener for model window mouse events
 * @author Johan Henriksson
 */
public interface ModelWindowMouseListener
	{
	public boolean mouseDragged(MouseEvent e, int dx, int dy);
	public void mouseMoved(MouseEvent e);
	
	/**
	 * Handle mouse click. Return true if the event is swallowed (thus the caller will not try to handle the event).
	 * A menu is provided that can be filled in with additional right-click entries
	 */
	public boolean mouseClicked(MouseEvent e, JPopupMenu menu);
	
	public void mouseEntered(MouseEvent e);
	public void mouseExited(MouseEvent e);
	public void mousePressed(MouseEvent e);
	public void mouseReleased(MouseEvent e);
	}
