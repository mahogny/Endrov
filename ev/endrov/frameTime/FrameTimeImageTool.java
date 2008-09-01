package endrov.frameTime;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import endrov.basicWindow.BasicWindow;
import endrov.imageWindow.*;
import endrov.imageset.*;

/**
 * Image Window Tool: Associate time with current frame
 * @author Johan Henriksson
 */
public class FrameTimeImageTool implements ImageWindowTool
	{
	private final ImageWindow w;
	public FrameTimeImageTool(ImageWindow w)
		{
		this.w=w;
		}
	
	/*
	public boolean isToggleable()
		{
		return false;
		}
	public String toolCaption()
		{
		return "Frametime/Set current";
		}
	public boolean enabled()
		{
		return true;
		}
		*/
	public JMenuItem getMenuItem()
		{
		JMenuItem mi=new JMenuItem("Frametime/Set current");
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){mouseClicked(null);}
		});
		return mi;
		}
	
	public void mouseClicked(MouseEvent e)
		{
		String frametimes=JOptionPane.showInputDialog("What is the current time?");
		if(frametimes!=null)
			{
			Imageset rec=w.getImageset();
			for(FrameTime f:rec.getObjects(FrameTime.class))
				{
				double frametime=Double.parseDouble(frametimes);
				f.add((int)w.frameControl.getFrame(), frametime);
				BasicWindow.updateWindows();
				}
			}
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e)	{}
	public void mouseMoved(MouseEvent e, int dx, int dy) {}
	public void mouseExited(MouseEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void paintComponent(Graphics g) {}
	public void unselected() {}

	}
