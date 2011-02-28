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
		JMenuItem miMakeMovie=new JMenuItem("Make Movie");
		JMenuItem miMakeMovieImw=new JMenuItem("Make Movie from image windows");
		
		public void createMenus(BasicWindow w)
			{
			miMakeMovie.addActionListener(this);
			miMakeMovieImw.addActionListener(this);
			w.addMenuBatch(miMakeMovie);
			w.addMenuBatch(miMakeMovieImw);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			if(e.getSource()==miMakeMovie)
				new MakeMovieWindow();
			else if(e.getSource()==miMakeMovieImw)
				MakeMovieWindowNew.createDialogFromImageWindows();
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}
