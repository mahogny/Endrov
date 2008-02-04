package evplugin.modelWindow;

import evplugin.basicWindow.*;

import java.awt.event.*;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class ModelWindowBasic implements BasicWindowExtension
	{
	private static ImageIcon iconWindow=new ImageIcon(ModelWindowBasic.class.getResource("iconWindow.png"));
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Model",iconWindow);
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new ModelWindow();
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}