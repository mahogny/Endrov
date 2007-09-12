package evplugin.consoleWindow;

import java.awt.event.*;
import javax.swing.*;

import evplugin.basicWindow.*;


/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class ConsoleBasic implements BasicWindowExtension
	{
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Console");
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