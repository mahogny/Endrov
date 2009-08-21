package endrov.imageWindow.tools;

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
import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowTool;
import endrov.imageset.EvChannel;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.util.EvDecimal;

/**
 * Change displacement of a channel
 * @author Johan Henriksson
 */
public class ImageWindowToolChannelDisp implements ImageWindowTool
	{
	private final ImageWindow w;
	
	public ImageWindowToolChannelDisp(ImageWindow w)
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
			
			double ddx=dx/w.getZoom(); //To world coordinates
			double ddy=dy/w.getZoom(); 
			if(c!=null)
				{
				for(Map.Entry<EvDecimal, EvStack> frames:c.imageLoader.entrySet())
					{
					EvStack stack=frames.getValue();
					stack.dispX+=stack.scaleWorldImageX(ddx); 
					stack.dispY+=stack.scaleWorldImageY(ddy); 
			
					//stack.dispX+=ddx;
					//stack.dispY+=ddy;
					
					
					c.defaultDispX=stack.dispX;
					c.defaultDispY=stack.dispY;
					
					//mark metadata dirty?
					}
				
				BasicWindow.updateWindows();
				rec.setMetadataModified();
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
	public void deselected() {}
	}

