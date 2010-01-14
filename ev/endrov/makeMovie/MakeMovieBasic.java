/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.makeMovie;

import endrov.basicWindow.*;

import java.awt.event.*;

import javax.swing.JMenuItem;


/**
 * Extension to BasicWindow
 * 
 * @author Johan Henriksson
 */
public class MakeMovieBasic implements BasicWindowExtension
	{
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Make Movie");
			mi.addActionListener(this);
			w.addMenuBatch(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new MakeMovieWindow();
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}
