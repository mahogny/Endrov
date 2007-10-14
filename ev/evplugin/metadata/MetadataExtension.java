package evplugin.metadata;

import javax.swing.*;

import evplugin.basicWindow.BasicWindow;


public abstract class MetadataExtension
	{
	public void addMetamenu(JMenu menu, JMenuItem mi)
		{
		BasicWindow.addMenuItemSorted(menu, mi);
		}
	
	public abstract void buildOpen(JMenu menu);
	public abstract void buildSave(JMenu menu, Metadata meta);
	}
