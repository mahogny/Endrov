package endrov.basicWindow;

import java.awt.event.*;

/**
 * Extension to BasicWindow
 * @author Johan Henriksson
 */
public class BasicWindowExitLast implements BasicWindowExtension
	{
	public static void integrate()
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExitLast());
		}
	
	public void newBasicWindow(BasicWindow w)
		{
		w.basicWindowExtensionHook.put(this.getClass(),new Hook());
		}
	private class Hook implements BasicWindowHook, ActionListener
		{
		BasicWindow w;
		
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
				if(BasicWindow.getWindowList().size()==1)
					BasicWindow.dialogQuit();
				else
					w.evw.dispose();
//					w.dispose();
				}
			};

		public void createMenus(BasicWindow w)
			{
			this.w=w;
			w.evw.addWindowListener(windowListener);
			}
		
		public void actionPerformed(ActionEvent e) 
			{
			}
		
		public void buildMenu(BasicWindow w){}
		}
	}
