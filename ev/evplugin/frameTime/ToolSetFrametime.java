package evplugin.frameTime;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;

import evplugin.basicWindow.BasicWindow;
import evplugin.imageWindow.*;
import evplugin.imageset.*;

/**
 * Image Window Tool: Associate time with current frame
 * @author Johan Henriksson
 */
public class ToolSetFrametime implements ImageWindowTool
	{
	private final ImageWindow w;
	public ToolSetFrametime(ImageWindow w)
		{
		this.w=w;
		}
	
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
