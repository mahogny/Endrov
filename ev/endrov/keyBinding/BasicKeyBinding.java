package endrov.keyBinding;

import endrov.basicWindow.*;
import endrov.basicWindow.icon.BasicIcon;

import java.awt.event.*;

import javax.swing.JMenuItem;

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
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Key Bindings",BasicIcon.iconKeyboard);
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new WindowKeyBinding();
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}