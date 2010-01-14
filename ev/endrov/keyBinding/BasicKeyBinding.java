/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.keyBinding;

import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;
import endrov.util.EvSwingUtil;

import java.awt.event.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class BasicKeyBinding implements BasicWindowExtension
	{
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		JMenu mInput=new JMenu("Input");
		
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Key bindings",BasicIcon.iconKeyboard);
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			
			//BasicWindow.addMenuItemSorted(menu, ni, itemName)
			BasicWindow.addMenuItemSorted(w.menuFile, mInput, "input");
			//w.menuFile
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new WindowKeyBinding();
			}
		
		public void buildMenu(BasicWindow w)
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
						BasicWindow.updateWindows();
						}
					});
				

				mInput.add(miMode);
				}
			
			
			}
		}
	}
