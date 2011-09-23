/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.scriptWindow;

import java.awt.event.*;
import javax.swing.*;

import endrov.basicWindow.*;


/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class ConsoleBasic implements BasicWindowExtension
	{
	private static ImageIcon iconWindow=new ImageIcon(ScriptWindow.class.getResource("tangoScript.png"));
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Script",iconWindow);
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new ScriptWindow();
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}