/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer2D;

import javax.swing.JMenuItem;

import endrov.gui.GeneralTool;

/**
 * A selectable tool in the image window
 */
public interface Viewer2DTool extends GeneralTool
	{
	/**
	 * Get the menu item to be placed in the tools menu
	 */
	public JMenuItem getMenuItem();

	/**
	 * Called when the tool has been deselected
	 */
	public void deselected();
	}
