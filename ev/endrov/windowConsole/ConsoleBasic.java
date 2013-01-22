/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowConsole;

import java.awt.event.*;
import javax.swing.*;

import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;


/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class ConsoleBasic implements EvBasicWindowExtension
	{
//	private static ImageIcon iconWindow=new ImageIcon(class.getResource("iconWindow.png"));
	public void newBasicWindow(EvBasicWindow w)
		{
		w.addHook(this.getClass(),new Hook());
		}
	private class Hook implements EvBasicWindowHook, ActionListener
		{
		public void createMenus(EvBasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Console",new ImageIcon(getClass().getResource("tangoConsole.png")));
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new ConsoleWindow();
			}
		
		public void buildMenu(EvBasicWindow w){}
		}
	}