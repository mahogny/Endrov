package endrov.basicWindow;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

import endrov.basicWindow.icon.BasicIcon;
import endrov.ev.EV;

/**
 * Ev Window Manager: Free-floating windows
 * @author Johan Henriksson
 */
public class EvWindowManagerFree extends JFrame implements WindowListener, EvWindowManager
	{
	static final long serialVersionUID=0; 
	private BasicWindow bw;
	
	
	
	public EvWindowManagerFree(BasicWindow bw)
		{
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.bw=bw;
		addWindowListener(this);
		add(bw);
		
    //int titleBarHeight = getInsets().top; //can be used to set the right icon
    //20x20 seems good on windows? or more?
		//16x16 on gnome, but in alt+tab larger. can supply larger image
		if(!EV.isMac())
			setIconImage(BasicIcon.programIcon.getImage());
		}
	
	public void setTitle(String title)
		{
		super.setTitle(EV.programName+" "+title+" ["+bw.windowInstance+"]");
		}
	
	
	public void windowClosing(WindowEvent e) {}
	public void windowActivated(WindowEvent arg0)	{}
	public void windowDeactivated(WindowEvent arg0)	{}
	public void windowDeiconified(WindowEvent arg0)	{}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0)
		{
		BasicWindow.windowList.remove(bw);
		bw.freeResources();
		System.out.println("here, window closed");
		}
	public void setResizable(boolean b)
		{
		super.setResizable(b);
		}
	
	
	
	
	public static class Manager implements BasicWindow.EvWindowManagerMaker
		{
		public EvWindowManager createWindow(BasicWindow bw)
			{
			EvWindowManager w=new EvWindowManagerFree(bw);
			return w;
			}
		}

	}
