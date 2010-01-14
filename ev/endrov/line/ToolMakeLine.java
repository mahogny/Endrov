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
import endrov.util.EvDecimal;

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
		if(activeAnnot!=null)
			{
			EvContainer data=w.getRootObject();
			renameObjectDialog(data, activeAnnot.ob);
			activeAnnot=null;
			}
		}
	
	private Hover getHoverAnnot(MouseEvent e)
		{
		EvDecimal curFrame=w.frameControl.getFrame();
		Collection<EvLine> ann=getAnnots();
		EvLine closest=null;
		int closesti=0;
		double cdist=0;
		Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
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
			
			Pos3dt pos=new Pos3dt();
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			pos.v.x=v.x;
			pos.v.y=v.y;
			pos.v.z=w.frameControl.getModelZ().doubleValue();
//			w.s2wz(w.frameControl.getZ()).doubleValue();
			pos.frame=w.frameControl.getFrame();
			
			line.pos.add(pos);
			line.pos.add(new Pos3dt(pos));
			
			a=new Hover();
			a.ob=line;
			a.i=1;
			
			BasicWindow.updateWindows();
			editing=false;
			a.ob.setMetadataModified();
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
			if(mouseHasMoved)
	//		if(activeAnnot.ob.pos.get(activeAnnot.i).equals(activeAnnot.ob.pos.get(activeAnnot.i-1)))
				{
				Pos3dt newpos=new Pos3dt(activeAnnot.ob.pos.get(activeAnnot.i));
				activeAnnot.ob.pos.add(newpos);
				activeAnnot.i++;
//				activeAnnot.ob.pos.remove(activeAnnot.i);
	//			activeAnnot=null;
				activeAnnot.ob.setMetadataModified();
				}
//			else
			}
		w.updateImagePanel(); //more than this. emit. 
//		BasicWindow.updateWindows(); //caused mouse exit all the time?
		}
	private void moveLastPoint(MouseEvent e)
		{
		if(activeAnnot!=null)
			{
			activeAnnot.ob.setMetadataModified();
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			activeAnnot.ob.pos.get(activeAnnot.i).v.x=v.x;
			activeAnnot.ob.pos.get(activeAnnot.i).v.y=v.y;
			activeAnnot.ob.pos.get(activeAnnot.i).v.z=w.frameControl.getModelZ().doubleValue();
			//w.s2wz(w.frameControl.getZ()).doubleValue(); 
	
			EvDecimal curFrame=w.frameControl.getFrame();
			for(Pos3dt a:activeAnnot.ob.pos)
				a.frame=curFrame;
			
			if(!activeAnnot.isAdded)
				{
				w.getRootObject().addMetaObject(activeAnnot.ob);
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
		EvLog.printLog("Length [um]: "+line.getTotalDistance()+ " ( "+distances+")");
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
				else if(mouseHasMoved)
					makeNextPoint();
				else
					lineIsDone();
//					activeAnnot=null;
				}
			}
		mouseHasMoved=false;			
		}
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(toolMode==SINGLESEGMENT)
			moveLastPoint(e);
		else if(toolMode==FREEHAND && !editing && activeAnnot!=null)
			{
			mouseHasMoved=true;
			moveLastPoint(e);
			EvLine line=activeAnnot.ob;
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
			if(SwingUtilities.isLeftMouseButton(e) && activeAnnot!=null)
				{
				System.out.println("released");
				printDistances(activeAnnot.ob);
				lineIsDone();
//				activeAnnot=null;
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
		//hack: confirm that it really is outside
//		if(e.getX()<0 || e.getY()<0 || e.getX()>=w.
		if(activeAnnot!=null)
			{
			EvContainer data=w.getRootObject();
			if(data!=null)
				data.removeMetaObjectByValue(activeAnnot.ob);
			System.out.println("mouse exited "+e.getX()+" "+e.getY());
			activeAnnot=null;
			BasicWindow.updateWindows();
//			w.updateImagePanel();
			}
		}

	
	public void deselected() {}
	}
