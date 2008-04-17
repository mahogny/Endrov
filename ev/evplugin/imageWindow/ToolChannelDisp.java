package evplugin.imageWindow;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import evplugin.basicWindow.BasicWindow;
import evplugin.imageset.Imageset;

/**
 * 
 * @author Johan Henriksson
 */
public class ToolChannelDisp implements ImageWindowTool
	{
	private final ImageWindow w;
	
	public ToolChannelDisp(ImageWindow w)
		{
		this.w=w;
		}
	/*
	public boolean isToggleable()
		{
		return true;
		}
	public String toolCaption()
		{
		return "Channel/Displacement";
		}
	public boolean enabled()
		{
		return true;
		}*/
	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Channel/Displacement");
		mi.setSelected(w.getTool()==this);
		final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		return mi;
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			Imageset rec=w.getImageset();
			Imageset.ChannelImages c=w.getSelectedChannel();
			if(rec!=null && c!=null)
				{
				c.getMeta().dispX+=dx/w.getZoom();
				c.getMeta().dispY+=dy/w.getZoom();
				//w.updateImagePanel();
				BasicWindow.updateWindows();
				rec.setMetadataModified(true);
				}
			}
		}
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseMoved(MouseEvent e, int dx, int dy) {}
	public void mouseExited(MouseEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void paintComponent(Graphics g) {}
	public void unselected() {}
	}

//TODO: for channel displacement. mark dirty imageset, save
