package endrov.imageannot;

import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

import javax.vecmath.*;
import javax.swing.*;

import endrov.data.*;
import endrov.imageWindow.*;

/**
 * Create and edit image annotation.
 *
 * @author Johan Henriksson
 */
public class ImageAnnotImageTool implements ImageWindowTool
	{
	private final ImageWindow w;
	private final ImageAnnotImageRenderer r;
	
	private ImageAnnot activeAnnot=null;
	
	public ImageAnnotImageTool(ImageWindow w, ImageAnnotImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	
	/*
	public boolean isToggleable(){return true;}
	public String toolCaption(){return "Annotate Image";}
	public boolean enabled(){return true;}*/

	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Annotate Image");
		mi.setSelected(w.getTool()==this);
		final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		return mi;
		}
	
	
	private Collection<ImageAnnot> getAnnots()
		{
		return r.getVisible();
		}
	
	
	private ImageAnnot getHoverAnnot(MouseEvent e)
		{
		Collection<ImageAnnot> ann=getAnnots();
		ImageAnnot closest=null;
		double cdist=0;
		Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
		for(ImageAnnot a:ann)
			{
			double dist=(a.pos.x-v.x)*(a.pos.x-v.x) + (a.pos.y-v.y)*(a.pos.y-v.y);
			if(cdist>dist || closest==null)
				{
				cdist=dist;
				closest=a;
				}
			}
		double sdist=w.scaleW2s(cdist);
		if(sdist<5*5)
			return closest;
		else
			return null;
		}
	
	
	public void mouseClicked(MouseEvent e)
		{
		EvContainer data=w.getImageset();
		if(data!=null)
			{
			ImageAnnot a=getHoverAnnot(e);
			if(SwingUtilities.isLeftMouseButton(e))
				{
				if(a==null)
					{
					//Create
					a=new ImageAnnot();
					setPos(a,e);
					data.addMetaObject(a);
					}
				//Edit text
				String newtext=JOptionPane.showInputDialog(null, "Enter text", a.text);
				if(newtext!=null)
					a.text=newtext;
				if(a.text.equals(""))
					data.removeMetaObjectByValue(a);
				w.updateImagePanel();
				}
			}
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(activeAnnot!=null)
			{
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			activeAnnot.pos.x=v.x;
			activeAnnot.pos.y=v.y;
			w.updateImagePanel(); //more than this. emit
			}
		}
	
	public void mousePressed(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			//Start dragging
			ImageAnnot a=getHoverAnnot(e);
			activeAnnot=a;
			}
		}

	public void mouseReleased(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && activeAnnot!=null)
			{
			setPos(activeAnnot,e);
			activeAnnot=null;
			w.updateImagePanel();
			}
		}

	private void setPos(ImageAnnot a, MouseEvent e)
		{
		Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
		a.pos.x=v.x;
		a.pos.y=v.y;
		a.pos.z=w.frameControl.getZ().doubleValue();
		}
	
	public void mouseMoved(MouseEvent e, int dx, int dy){}
	public void keyPressed(KeyEvent e){}
	public void paintComponent(Graphics g){}
	public void keyReleased(KeyEvent e){}

	public void mouseExited(MouseEvent e)
		{
		if(activeAnnot!=null)
			{
			EvContainer data=w.getImageset();
			if(data!=null)
				data.removeMetaObjectByValue(activeAnnot);
			activeAnnot=null;
			w.updateImagePanel();
			}
		}

	
	public void unselected() {}
	}
