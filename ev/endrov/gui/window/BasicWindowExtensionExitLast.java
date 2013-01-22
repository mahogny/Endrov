/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.window;

import java.awt.event.*;

/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class BasicWindowExtensionExitLast implements EvBasicWindowExtension
	{
	public static void integrate()
		{
		EvBasicWindow.addBasicWindowExtension(new BasicWindowExtensionExitLast());
		}
	
	public void newBasicWindow(EvBasicWindow w)
		{
		w.addHook(this.getClass(),new Hook());
		}
	private class Hook implements EvBasicWindowHook, ActionListener
		{
		EvBasicWindow w;
		
		/**
		 * Called when window close-button is clicked. Ask if to quit when only
		 * one window left.
		 */
		WindowListener windowListener=new WindowListener()
			{
			public void windowActivated(WindowEvent arg0)	{}
			public void windowClosed(WindowEvent arg0) {}
			public void windowDeactivated(WindowEvent arg0)	{}
			public void windowDeiconified(WindowEvent arg0)	{}
			public void windowIconified(WindowEvent arg0) {}
			public void windowOpened(WindowEvent arg0) {}
			public void windowClosing(WindowEvent e)
				{
				if(EvBasicWindow.getWindowList().size()==1)
					EvBasicWindow.dialogQuit();
				else
					w.getEvw().dispose();
//					w.dispose();
				}
			};

		public void createMenus(EvBasicWindow w)
			{
			this.w=w;
			w.getEvw().addWindowListener(windowListener);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			}
		
		public void buildMenu(EvBasicWindow w){}
		}
	}
