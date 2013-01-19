/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowConsole;

import java.awt.event.*;
import javax.swing.*;

import endrov.gui.window.BasicWindow;
import endrov.gui.window.BasicWindowExtension;
import endrov.gui.window.BasicWindowHook;


/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class ConsoleBasic implements BasicWindowExtension
	{
//	private static ImageIcon iconWindow=new ImageIcon(class.getResource("iconWindow.png"));
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Console",new ImageIcon(getClass().getResource("tangoConsole.png")));
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new ConsoleWindow();
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}