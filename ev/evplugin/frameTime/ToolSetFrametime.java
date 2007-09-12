package evplugin.frameTime;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import evplugin.basicWindow.BasicWindow;
import evplugin.imageWindow.ImageWindow;
import evplugin.imageWindow.ImageWindowTool;
//import evplugin.sql.DB;
import evplugin.imageset.*;
import evplugin.metadata.*;

/**
 * 
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
		String frametimes=JOptionPane.showInputDialog("What is the current frametime?");
		//DB db=DB.getCurrentDB();
		if(frametimes!=null /*&& db!=null*/)
			{
			Imageset rec=w.comboChannel.getImageset();
			for(MetaObject o:rec.metaObject.values())
				if(o instanceof FrameTime)
					{
					FrameTime f=(FrameTime)o;
					double frametime=Double.parseDouble(frametimes);
//					FrameTime f=new FrameTime(db, DB.currentSample);
					f.add((int)w.frameControl.getFrame(), frametime);
					BasicWindow.updateWindows();
	//				f.appendSql();
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
		

	}
