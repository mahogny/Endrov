/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.keybinding;

import endrov.gui.EvSwingUtil;
import endrov.gui.icon.BasicIcon;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;

import java.awt.event.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class BasicKeyBinding implements EvBasicWindowExtension
	{
	public void newBasicWindow(EvBasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements EvBasicWindowHook, ActionListener
		{
		JMenu mInput=new JMenu("Input");
		
		public void createMenus(EvBasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Key bindings",BasicIcon.iconKeyboard);
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			
			//BasicWindow.addMenuItemSorted(menu, ni, itemName)
			EvBasicWindow.addMenuItemSorted(w.menuFile, mInput, "input");
			//w.menuFile
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new WindowKeyBinding();
			}
		
		public void buildMenu(EvBasicWindow w)
			{
			EvSwingUtil.tearDownMenu(mInput);
			String selected=JInputManager.selectedGamepadMode;
			
			
			for(final String name:JInputManager.gamepadModes.keySet())
				{
				JRadioButtonMenuItem miMode=new JRadioButtonMenuItem(name);
				if(name.equals(selected))
					miMode.setSelected(true);
				miMode.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						JInputManager.selectedGamepadMode=name;
						EvBasicWindow.updateWindows();
						}
					});
				

				mInput.add(miMode);
				}
			
			
			}
		}
	}
