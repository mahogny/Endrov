package endrov.imagesetImserv;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.BasicWindowExtension;
import endrov.basicWindow.BasicWindowHook;

import java.awt.event.*;

import javax.swing.*;



/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class ImservBasic implements BasicWindowExtension
	{
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("ImServ",new ImageIcon(getClass().getResource("iconImserv.png")));
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new ImservWindow();
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}