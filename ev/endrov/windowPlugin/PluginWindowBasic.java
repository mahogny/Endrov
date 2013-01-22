/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowPlugin;

import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;

import java.awt.event.*;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class PluginWindowBasic implements EvBasicWindowExtension
	{
	public void newBasicWindow(EvBasicWindow w)
		{
		w.addHook(this.getClass(),new Hook());
		}
	private class Hook implements EvBasicWindowHook, ActionListener
		{
		public void createMenus(EvBasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Plugins",new ImageIcon(getClass().getResource("silkPluginWindow.png")));
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new PluginWindow();
			}
		
		public void buildMenu(EvBasicWindow w){}
		}
	}
