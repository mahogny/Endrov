package evplugin.line;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
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
	
	public static final int SINGLESEGMENT=0;
	public static final int MULTISEGMENT=1;
	public static final int FREEHAND=2;
	
	private int toolMode;
	private boolean mouseHasMoved=true;
	private boolean editing;
	
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
	
	private void setMode(int toolMode)
		{
		this.toolMode=toolMode;
		}

	public JMenuItem getMenuItem()
		{
		JMenu menu=new JMenu("Lines");

		boolean anySelected=w.getTool()==this;
		
		final JCheckBoxMenuItem miSingle=new JCheckBoxMenuItem("Simple", anySelected && toolMode==SINGLESEGMENT);
		final JCheckBoxMenuItem miMulti=new JCheckBoxMenuItem("Multisegment", anySelected && toolMode==MULTISEGMENT);
		final JCheckBoxMenuItem miFree=new JCheckBoxMenuItem("Freehand", anySelected && toolMode==FREEHAND);
		menu.add(miSingle);
		menu.add(miMulti);
		menu.add(miFree);
		
		final WeakReference<ImageWindowTool> This=new WeakReference<ImageWindowTool>(this);
		ActionListener list=new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				if(e.getSource()==miSingle)
					setMode(SINGLESEGMENT);
				else if(e.getSource()==miMulti)
					setMode(MULTISEGMENT);
				else
					setMode(FREEHAND);
				w.setTool(This.get());
				}
		};

		miSingle.addActionListener(list);
		miMulti.addActionListener(list);
		miFree.addActionListener(list);
		
		return menu;
		
		
		
		/*
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Lines");
		mi.setSelected(w.getTool()==this);
		final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		
		return mi;
		*/
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
	
	
	private void makeFirstPoint(MouseEvent e)
		{
		//Start dragging
		Hover a=getHoverAnnot(e);
		if(a==null)
			{
			EvLine line=new EvLine();
			//w.getImageset().addMetaObject(line);
			
			Vector4d pos=new Vector4d();
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			pos.x=v.x;
			pos.y=v.y;
			pos.z=w.s2wz(w.frameControl.getZ());
			pos.w=(int)w.frameControl.getFrame();
			
			line.pos.add(pos);
			line.pos.add(new Vector4d(pos));
			
			a=new Hover();
			a.ob=line;
			a.i=1;
			
			BasicWindow.updateWindows();
			editing=false;
			a.ob.metaObjectModified=true;
			}
		else
			{
			printDistances(a.ob);
			editing=true;
			}
		activeAnnot=a;
		}
	private void makeNextPoint()
		{
		if(editing)
			activeAnnot=null;
		else
			{
			Vector4d newpos=new Vector4d(activeAnnot.ob.pos.get(activeAnnot.i));
			activeAnnot.ob.pos.add(newpos);
			activeAnnot.i++;
			if(!mouseHasMoved)
	//		if(activeAnnot.ob.pos.get(activeAnnot.i).equals(activeAnnot.ob.pos.get(activeAnnot.i-1)))
				{
				activeAnnot.ob.pos.remove(activeAnnot.i);
				activeAnnot=null;
				}
			activeAnnot.ob.metaObjectModified=true;
			}
		BasicWindow.updateWindows();
		}
	private void moveLastPoint(MouseEvent e)
		{
		if(activeAnnot!=null)
			{
			activeAnnot.ob.metaObjectModified=true;
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			activeAnnot.ob.pos.get(activeAnnot.i).x=v.x;
			activeAnnot.ob.pos.get(activeAnnot.i).y=v.y;
			activeAnnot.ob.pos.get(activeAnnot.i).z=w.s2wz(w.frameControl.getZ()); 
	
			int curFrame=(int)w.frameControl.getFrame();
			for(Vector4d a:activeAnnot.ob.pos)
				a.w=curFrame;
			
			if(!activeAnnot.isAdded)
				{
				w.getImageset().addMetaObject(activeAnnot.ob);
				activeAnnot.isAdded=true;
				}
			
			w.updateImagePanel(); //more than this. emit
			}
		}
	private void printDistances(EvLine line)
		{
		StringBuffer distances=new StringBuffer();
		for(double d:line.getSegmentDistances())
			{
			distances.append(Double.toString(d));
			distances.append(" ");
			}
		Log.printLog("Length [um]: "+line.getTotalDistance()+ " ( "+distances+")");
		}
	

	
	
	public void mouseClicked(MouseEvent e)
		{
		}	
	public void mousePressed(MouseEvent e)
		{
		if(toolMode==SINGLESEGMENT || toolMode==FREEHAND)
			{
			if(SwingUtilities.isLeftMouseButton(e))
				makeFirstPoint(e);
			}
		else if(toolMode==MULTISEGMENT)
			{
			if(SwingUtilities.isLeftMouseButton(e))
				{
				if(activeAnnot==null)
					{
					makeFirstPoint(e);
					moveLastPoint(e);
					}
				else
					makeNextPoint();
				}
			}
		mouseHasMoved=false;			
		}
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(toolMode==SINGLESEGMENT)
			moveLastPoint(e);
		else if(toolMode==FREEHAND && !editing)
			{
			moveLastPoint(e);
			EvLine line=activeAnnot.ob;
			if(line.pos.size()>=2)
				{
				Vector4d a=new Vector4d(line.pos.get(line.pos.size()-1));
				a.sub(line.pos.get(line.pos.size()-2));
				a.w=0;
				if(w.scaleW2s(a.length())>3)
					makeNextPoint();
				}
			}
		}
	public void mouseReleased(MouseEvent e)
		{
		if(toolMode==SINGLESEGMENT || toolMode==FREEHAND)
			if(SwingUtilities.isLeftMouseButton(e) && activeAnnot!=null)
				{
				printDistances(activeAnnot.ob);
				activeAnnot=null;
				BasicWindow.updateWindows();
				}
		}
	public void mouseMoved(MouseEvent e, int dx, int dy)
		{
		mouseHasMoved=true;
		if(toolMode==MULTISEGMENT)
			moveLastPoint(e);
		}
	
	
	
	
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
