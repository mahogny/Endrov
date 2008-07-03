package endrov.keyBinding;

import endrov.basicWindow.*;

import java.awt.event.*;

import javax.swing.ImageIcon;
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
			JMenuItem mi=new JMenuItem("Key Bindings",new ImageIcon(getClass().getResource("iconWindow.png")));
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