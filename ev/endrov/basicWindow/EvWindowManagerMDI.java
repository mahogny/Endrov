package endrov.basicWindow;

import java.awt.BorderLayout;
//import java.awt.FlowLayout;
import java.awt.Rectangle;
//import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import endrov.ev.EV;

/**
 * Ev Window Manager: MDI windows
 * @author Johan Henriksson
 */
public class EvWindowManagerMDI extends JPanel implements /*WindowListener,*/ EvWindowManager
	{
	static final long serialVersionUID=0; 
//	private BasicWindow bw;
	
	
	
	
	public void addWindowListener(WindowListener l)
		{
		}

	public void dispose()
		{
		}

	public Rectangle getBounds()
		{
		return null;
		}

	public void pack()
		{
		}

	public void setBounds(Rectangle r)
		{
		}

	public void setJMenuBar(JMenuBar mb)
		{
		}

	public void setVisible(boolean b)
		{
		}

	public void toFront()
		{
		}
	public void setLocation(int x, int y)
		{
		}
	public void setResizable(boolean b)
		{
		}

	public static JFrame totalFrame=new JFrame(EV.programName+" foo");
	public static JPanel totalPane=new JPanel();
	{
	totalFrame.getContentPane().add(totalPane);
	totalFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	totalFrame.setVisible(true);
	totalFrame.setBounds(50, 50, 500, 500);
	}
	
	
	
	public EvWindowManagerMDI(BasicWindow bw)
		{
	//	this.bw=bw;
		//addWindowListener(this);
		setLayout(new BorderLayout());
		add(bw,BorderLayout.CENTER);
		System.out.println("new");
		totalPane.add(bw);
		}
	
	public void setTitle(String title)
		{
//		super.setTitle(EV.programName+" "+title);
		}
	
	/*
	public void windowClosing(WindowEvent e) {}
	public void windowActivated(WindowEvent arg0)	{}
	public void windowDeactivated(WindowEvent arg0)	{}
	public void windowDeiconified(WindowEvent arg0)	{}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0)
		{
		BasicWindow.windowList.remove(bw);
		}*/
	
	
	
	
	
	public static class Manager implements BasicWindow.EvWindowManagerMaker
		{
		public EvWindowManager createWindow(BasicWindow bw)
			{
			EvWindowManagerMDI w=new EvWindowManagerMDI(bw);
			return w;
			}
		}

	}
