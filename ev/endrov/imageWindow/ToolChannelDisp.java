package endrov.imageWindow;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import endrov.basicWindow.BasicWindow;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.util.EvDecimal;

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
			EvChannel c=w.getSelectedChannel();
			double ddx=dx/w.getZoom();
			double ddy=dy/w.getZoom();
			if(c!=null)
				{
				c.dispX+=ddx;
				c.dispY+=ddy;
				
				for(Map.Entry<EvDecimal, EvStack> frames:c.imageLoader.entrySet())
					for(Map.Entry<EvDecimal, EvImage> stacks:frames.getValue().entrySet())
						{
						EvImage evim=stacks.getValue();
						evim.dispX+=ddx;
						evim.dispY+=ddy;
						}
				
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
