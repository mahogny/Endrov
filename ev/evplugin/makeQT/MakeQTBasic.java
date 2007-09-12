package evplugin.makeQT;

import evplugin.basicWindow.*;

import java.awt.event.*;

import javax.swing.JMenuItem;


/**
 * Extension to BasicWindow
 * 
 * @author Johan Henriksson
 */
public class MakeQTBasic implements BasicWindowExtension
	{
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Make QT Movie");
			mi.addActionListener(this);
			w.addMenuBatch(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new MakeQTWindow();
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}
