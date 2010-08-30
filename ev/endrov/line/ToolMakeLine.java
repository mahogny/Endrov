/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.line;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;

import javax.vecmath.*;
import javax.swing.*;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.imageWindow.*;
import endrov.line.EvLine.Pos3dt;
import endrov.undo.UndoOpBasic;
import endrov.util.EvDecimal;

/**
 * Create and edit lines.
 *
 * @author Johan Henriksson
 */
public class ToolMakeLine implements ImageWindowTool
	{
	private final ImageWindow w;
	private final EvLineImageRenderer r;
	
	public static final int SINGLESEGMENT=0;
	public static final int MULTISEGMENT=1;
	public static final int FREEHAND=2;
	
	private int toolMode;
	private boolean mouseHasMoved=true;
	private boolean editing;
	
	
	
	
	public ToolMakeLine(ImageWindow w, EvLineImageRenderer r)
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
		}
	
	
	private Collection<EvLine> getAnnots()
		{
		EvLineImageRenderer r=w.getRendererClass(EvLineImageRenderer.class);
		return r.getVisible();
		}
	
	
	
	
	private void renameObjectDialog(EvContainer data, String obId)
		{
		String newId=(String)JOptionPane.showInputDialog(null, "Name:", EV.programName+" Rename object", 
				JOptionPane.QUESTION_MESSAGE, null, null, obId);
		//Maybe use weak reference?
		if(newId!=null)
			{
			EvObject ob=data.metaObject.remove(obId);
			if(ob!=null)
				data.metaObject.put(newId, ob);
			BasicWindow.updateWindows();
			}
		}
	
	private void renameObjectDialog(EvContainer data, EvObject obVal)
		{
		String key=null;
		for(Map.Entry<String, EvObject> e:data.metaObject.entrySet())
			if(e.getValue()==obVal)
				key=e.getKey();
		if(key!=null)
			renameObjectDialog(data, key);
		}
	
	private void lineIsDone()
		{
		if(r.activeAnnot!=null)
			{
			EvContainer data=w.getRootObject();
			renameObjectDialog(data, r.activeAnnot.ob);
			r.activeAnnot=null;
			}
		BasicWindow.updateWindows();
		}
	
	private EvLineImageRenderer.Hover getHoverAnnot(MouseEvent e)
		{
		EvDecimal curFrame=w.getFrame();
		Collection<EvLine> ann=getAnnots();
		EvLine closest=null;
		int closesti=0;
		double cdist=0;
		Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
		for(EvLine a:ann)
			for(int i=0;i<a.pos.size();i++)
				{
				double dx=a.pos.get(i).v.x-v.x;
				double dy=a.pos.get(i).v.y-v.y;
				double dist=dx*dx + dy*dy;
				if((cdist>dist || closest==null) && curFrame.equals(a.pos.get(i).frame))
					{
					cdist=dist;
					closest=a;
					closesti=i;
					}
				}
		double sdist=w.scaleW2s(Math.sqrt(cdist));
		if(closest!=null && sdist<ImageWindow.snapDistance)
			{
			EvLineImageRenderer.Hover h=new EvLineImageRenderer.Hover();
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
		EvLineImageRenderer.Hover a=getHoverAnnot(e);
		if(a==null)
			{
			//Create a new line
			EvLine line=new EvLine();
			line.setMetadataModified();
			
			Pos3dt pos=new Pos3dt();
			Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
			pos.v.x=v.x;
			pos.v.y=v.y;
			pos.v.z=w.getZ().doubleValue();
			pos.frame=w.getFrame();
			
			line.pos.add(pos);
			line.pos.add(new Pos3dt(pos));
			
			a=new EvLineImageRenderer.Hover();
			a.ob=line;
			a.i=1;
			
			editing=false;
			BasicWindow.updateWindows();
			}
		else
			{
			//Modify an existing line
			printDistances(a.ob);
			editing=true;
			}
		r.activeAnnot=a;
		}
	
	/**
	 * Add a new point to the segment
	 */
	private void makeNextPoint()
		{
		if(editing)
			r.activeAnnot=null;
		else
			{
			if(mouseHasMoved)
				{
				Pos3dt newpos=new Pos3dt(r.activeAnnot.ob.pos.get(r.activeAnnot.i));
				r.activeAnnot.ob.pos.add(newpos);
				r.activeAnnot.i++;
				r.activeAnnot.ob.setMetadataModified();
				}
			}
		w.updateImagePanel(); //more than this. emit. 
		}
	
	private static class UndoOpAddObject extends UndoOpBasic
		{
		private EvContainer container;
		/**
		 * How safe is it to point to the original object? Quite safe, if other undo operations
		 * modify it back to the original state
		 */
		private EvObject newOb;
		private String id;
		public UndoOpAddObject(String opName, EvObject newOb, EvContainer container)
			{
			super(opName);
			this.newOb=newOb;
			this.container=container;
			}

		public void redo()
			{
			id=container.addMetaObject(newOb);
			}

		public void undo()
			{
			container.metaObject.remove(id);
			}
		
		}
	
	
	/**
	 * Update position of last point
	 */
	private void moveLastPoint(MouseEvent e)
		{
		if(r.activeAnnot!=null)
			{
			r.activeAnnot.ob.setMetadataModified();
			Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
			r.activeAnnot.ob.pos.get(r.activeAnnot.i).v.x=v.x;
			r.activeAnnot.ob.pos.get(r.activeAnnot.i).v.y=v.y;
			r.activeAnnot.ob.pos.get(r.activeAnnot.i).v.z=w.getZ().doubleValue();
	
			EvDecimal curFrame=w.getFrame();
			for(Pos3dt a:r.activeAnnot.ob.pos)
				a.frame=curFrame;
			
			if(!r.activeAnnot.isAdded)
				{
				//new UndoOpAddObject("Add line", activeAnnot.ob, w.getRootObject());  TODO
				//Move and commit at end instead
				w.getRootObject().addMetaObject(r.activeAnnot.ob);
				r.activeAnnot.isAdded=true;
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
		EvLog.printLog("Length [um]: "+line.getTotalDistance()+ " ( "+distances+")");
		}
	

	
	
	public void mouseClicked(MouseEvent e, Component invoker)
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
				if(r.activeAnnot==null)
					{
					makeFirstPoint(e);
					moveLastPoint(e);
					}
				else if(mouseHasMoved)
					makeNextPoint();
				else
					lineIsDone();
				}
			}
		mouseHasMoved=false;			
		}
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(toolMode==SINGLESEGMENT)
			moveLastPoint(e);
		else if(toolMode==FREEHAND && !editing && r.activeAnnot!=null)
			{
			mouseHasMoved=true;
			moveLastPoint(e);
			EvLine line=r.activeAnnot.ob;
			if(line.pos.size()>=2)
				{
				Pos3dt a=new Pos3dt(line.pos.get(line.pos.size()-1));
				a.v.sub(line.pos.get(line.pos.size()-2).v);
				a.frame=EvDecimal.ZERO; //The right thing?
				if(w.scaleW2s(a.v.length())>3)
					makeNextPoint();
				}
			}
		}
	public void mouseReleased(MouseEvent e)
		{
		if(toolMode==SINGLESEGMENT || toolMode==FREEHAND)
			if(SwingUtilities.isLeftMouseButton(e) && r.activeAnnot!=null)
				{
				printDistances(r.activeAnnot.ob);
				lineIsDone();
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
		if(r.activeAnnot!=null)
			{
			EvContainer data=w.getRootObject();
			if(data!=null)
				data.removeMetaObjectByValue(r.activeAnnot.ob);
			System.out.println("mouse exited "+e.getX()+" "+e.getY());
			r.activeAnnot=null;
			BasicWindow.updateWindows();
			}
		}

	
	public void deselected() {}
	}
