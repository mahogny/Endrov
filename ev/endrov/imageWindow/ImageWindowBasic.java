/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageWindow;
import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;

import java.awt.event.*;
import javax.swing.*;

/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class ImageWindowBasic implements BasicWindowExtension
	{
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Image",BasicIcon.iconImage);
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new ImageWindow();
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}
