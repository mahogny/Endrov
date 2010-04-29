/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageWindow;

import javax.swing.JMenuItem;

/**
 * A selectable tool in the image window
 */
public interface ImageWindowTool extends GeneralTool
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
