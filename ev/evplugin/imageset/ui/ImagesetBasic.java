package evplugin.imageset.ui;

import evplugin.basicWindow.*;

import java.awt.event.*;
import javax.swing.*;

/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class ImagesetBasic implements BasicWindowExtension
	{
	public static Hook hook(BasicWindow w)
		{
		Hook h=(Hook)w.basicWindowExtensionHook.get((new ImagesetBasic()).getClass());
		return h;
		}
	
	
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook(w));
		}
	public class Hook implements BasicWindowHook, ActionListener
		{
		private JMenuItem miImagesetMeta=new JMenuItem("Imageset Meta");
		
		public Hook(BasicWindow w)
			{
			}
		
		public void createMenus(BasicWindow w)
			{
			w.addMenuWindow(miImagesetMeta);
			miImagesetMeta.addActionListener(this);
			buildMenu(w);
			}
		
		public void buildMenu(BasicWindow w)
			{
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			if(e.getSource()==miImagesetMeta)
				new MetaWindow();
			}
		}
	}