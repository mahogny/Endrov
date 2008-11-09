package endrov.basicWindow;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

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
		System.out.println("here");
		}
	
	public void toFront()
		{
		toFront();
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
