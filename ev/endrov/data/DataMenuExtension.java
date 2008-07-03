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
	
	public abstract void buildOpen(JMenu menu);
	public abstract void buildSave(JMenu menu, EvData meta);
	}
