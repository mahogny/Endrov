/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

import javax.swing.*;

import endrov.basicWindow.BasicWindow;

/**
 * This class is extended to add additional items to the Data-menu
 * 
 * @author Johan Henriksson
 */
public abstract class DataMenuExtension
	{
	public void addMetamenu(JMenu menu, JMenuItem mi)
		{
		BasicWindow.addMenuItemSorted(menu, mi);
		}

	public abstract void buildData(JMenu menu);
	public abstract void buildOpen(JMenu menu);
	public abstract void buildSave(JMenu menu, EvData meta);
	}
