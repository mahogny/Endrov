package evplugin.line;

import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

import javax.vecmath.*;
import javax.swing.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.data.EvData;
import evplugin.ev.Log;
import evplugin.imageWindow.*;

/**
 * Create and edit lines.
 *
 * @author Johan Henriksson
 */
public class ToolMakeLine implements ImageWindowTool
	{
	private final ImageWindow w;
	private final EvLineRenderer r;
	
	private class Hover
		{
		EvLine ob;
		int i;
		boolean isAdded=false;
		}
	
	private Hover activeAnnot=null;
	
	public ToolMakeLine(ImageWindow w, EvLineRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	
	

	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Lines");
		mi.setSelected(w.getTool()==this);
		final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		return mi;
		}
	
	
	private Collection<EvLine> getAnnots()
		{
		return r.getVisible();
		}
	
	
	
	private Hover getHoverAnnot(MouseEvent e)
		{
		Collection<EvLine> ann=getAnnots();
		EvLine closest=null;
		int closesti=0;
		double cdist=0;
		Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
		for(EvLine a:ann)
			for(int i=0;i<a.pos.size();i++)
				{
				double dx=a.pos.get(i).x-v.x;
				double dy=a.pos.get(i).y-v.y;
				double dist=dx*dx + dy*dy;
				if(cdist>dist || closest==null)
					{
					cdist=dist;
					closest=a;
					closesti=i;
					}
				}
		double sdist=w.scaleW2s(cdist);
		if(closest!=null && sdist<ImageWindow.snapDistance*ImageWindow.snapDistance)
			{
			Hover h=new Hover();
			h.ob=closest;
			h.i=closesti;
			h.isAdded=true;
			return h;
			}
		else
			return null;
		}
	
	
	public void mouseClicked(MouseEvent e)
		{
		}
	
	public void mousePressed(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			//Start dragging
			Hover a=getHoverAnnot(e);
			if(a==null)
				{
				EvLine line=new EvLine();
				//w.getImageset().addMetaObject(line);
				
				Vector3d pos=new Vector3d();
				Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
				pos.x=v.x;
				pos.y=v.y;
				pos.z=w.s2wz(w.frameControl.getZ());

				line.pos.add(pos);
				line.pos.add(new Vector3d(pos));
				
				a=new Hover();
				a.ob=line;
				a.i=1;
				
				BasicWindow.updateWindows();
				}
			else
				{
				printDistances(a.ob);
				}
			activeAnnot=a;
			}
		}
	
	public void printDistances(EvLine line)
		{
		StringBuffer distances=new StringBuffer();
		for(double d:line.getSegmentDistances())
			{
			distances.append(Double.toString(d));
			distances.append(" ");
			}
		Log.printLog("Length [um]: "+line.getTotalDistance()+ " ( "+distances+")");
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(activeAnnot!=null)
			{
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			activeAnnot.ob.pos.get(activeAnnot.i).x=v.x;
			activeAnnot.ob.pos.get(activeAnnot.i).y=v.y;
			activeAnnot.ob.pos.get(activeAnnot.i).z=w.s2wz(w.frameControl.getZ()); 
			
			if(!activeAnnot.isAdded)
				{
				w.getImageset().addMetaObject(activeAnnot.ob);
				activeAnnot.isAdded=true;
				}
			
			w.updateImagePanel(); //more than this. emit
			}
		}
	
	

	public void mouseReleased(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && activeAnnot!=null)
			{
			printDistances(activeAnnot.ob);
			activeAnnot=null;
			BasicWindow.updateWindows();
			}
		}

	
	public void mouseMoved(MouseEvent e, int dx, int dy){}
	public void keyPressed(KeyEvent e){}
	public void paintComponent(Graphics g){}
	public void keyReleased(KeyEvent e){}

	public void mouseExited(MouseEvent e)
		{
		if(activeAnnot!=null)
			{
			EvData data=w.getImageset();
			if(data!=null)
				data.removeMetaObjectByValue(activeAnnot.ob);
			activeAnnot=null;
			w.updateImagePanel();
			}
		}

	
	public void unselected() {}
	}
