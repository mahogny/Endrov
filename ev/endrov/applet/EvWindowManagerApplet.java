package endrov.applet;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import endrov.basicWindow.*;

/**
 * Ev Window Manager: Everything in an applet
 * @author Johan Henriksson
 */
public class EvWindowManagerApplet implements BasicWindow.EvWindowManagerMaker
	{
	
	private class Window extends JPanel implements EvWindowManager
		{
		static final long serialVersionUID=0; 
		BasicWindow bw;
		
		public Window(BasicWindow bw)
			{
			this.bw=bw;
			setLayout(new BorderLayout());
			add(bw,BorderLayout.CENTER);
//			System.out.println("new");
			totalPane.add(bw);
			}
		
		public void addWindowListener(WindowListener l)
			{
			}
		public void dispose()
			{
			}
		public Rectangle getBounds()
			{
			return super.getBounds();
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
			bw.setVisible(b);
			}
		public void toFront()
			{
			}
		public void setTitle(String title)
			{
			}
		public void setResizable(boolean b)
			{
			}
		
		
		
		}
		
	
	
	public JPanel totalPane=new JPanel();
	
	public EvWindowManagerApplet(int numi, boolean makeFrame)
		{
		totalPane.setLayout(new GridLayout(1,numi));
		if(makeFrame)
			{
			JFrame totalFrame=new JFrame("EV-lite");
			totalFrame.getContentPane().add(totalPane);
			totalFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			totalFrame.setVisible(true);
			totalFrame.setBounds(50, 50, 500, 500);
			}
		}
	
	public EvWindowManager createWindow(BasicWindow bw)
		{
		EvWindowManager w=new Window(bw);
		return w;
		}
	
	}
