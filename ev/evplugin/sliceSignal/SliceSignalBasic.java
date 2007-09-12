package evplugin.sliceSignal;

import evplugin.basicWindow.*;

import java.awt.event.*;

import javax.swing.JMenuItem;


/**
 * Extension to BasicWindow
 * 
 * @author Johan Henriksson
 */
public class SliceSignalBasic implements BasicWindowExtension
	{
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Slice/Signal");
			mi.addActionListener(this);
			w.addMenuBatch(mi);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			new SliceSignalWindow();
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}
