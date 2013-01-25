/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeText;

import java.awt.*;
import java.awt.event.*;
import java.util.Map;

import javax.vecmath.*;
import javax.swing.*;

import endrov.data.*;
import endrov.gui.undo.UndoOpPutObject;
import endrov.util.collection.Tuple;
import endrov.windowViewer2D.*;

/**
 * Create and edit image annotation.
 *
 * @author Johan Henriksson
 */
public class TextAnnotImageTool implements Viewer2DTool
	{
	private final Viewer2DWindow w;
	private final TextAnnotImageRenderer r;
	
	
	public TextAnnotImageTool(Viewer2DWindow w, TextAnnotImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	

	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("Text annotation");
		mi.setSelected(w.getTool()==this);
		final Viewer2DTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		return mi;
		}
	
	
	private Tuple<String,TextAnnot> getHoverAnnot(MouseEvent e)
		{
		Map<String,TextAnnot> ann=r.getVisible();
		TextAnnot closest=null;
		String closestName=null;
		double cdist=0;
		Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
		for(Map.Entry<String,TextAnnot> ae:ann.entrySet())
			{
			TextAnnot a=ae.getValue();
			double dist=(a.pos.x-v.x)*(a.pos.x-v.x) + (a.pos.y-v.y)*(a.pos.y-v.y);
			if(cdist>dist || closest==null)
				{
				cdist=dist;
				closest=a;
				closestName=ae.getKey();
				}
			}
		double sdist=w.scaleW2s(cdist);
		if(sdist<5*5 && closest!=null)
			return Tuple.make(closestName,closest);
		else
			return null;
		}
	
	
	public void mouseClicked(MouseEvent e, Component invoker)
		{
		EvContainer data=w.getRootObject();
		if(data!=null)
			{
			Tuple<String,TextAnnot> ae=getHoverAnnot(e);
			if(SwingUtilities.isLeftMouseButton(e))
				{
				if(ae==null)
					{
					//Create new
					String newtext=JOptionPane.showInputDialog(null, "Enter text");
					if(newtext!=null && !newtext.equals(""))
						{
						TextAnnot a=new TextAnnot();
						setPos(a,e);
						a.text=newtext;
						new UndoOpPutObject("Create annotation", a, data, data.getFreeChildName()).execute();
						}
					}
				else
					{
					//Rename
					TextAnnot a=ae.snd();
					String newtext=JOptionPane.showInputDialog(null, "Enter text", a.text);
					if(newtext!=null)
						{
						if(a.text.equals(""))
							{
							//Delete
							new UndoOpPutObject("Delete annotation", null, data, ae.fst()).execute();
							}
						else
							{
							//Rename
							a=a.clone();
							a.text=newtext;
							new UndoOpPutObject("Set annotation text", a, data, ae.fst()).execute();
							setActiveAnnot(null);
							}
						}
					
					}
				}
			
			}
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(getActiveAnnot()!=null)
			{
			Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
			r.activeAnnotNew.pos.x=v.x;
			r.activeAnnotNew.pos.y=v.y;
			w.updateImagePanel(); 
			}
		}
	
	public void mousePressed(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			//Start dragging
			Tuple<String,TextAnnot> a=getHoverAnnot(e);
			setActiveAnnot(a);
			}
		}

	public void mouseReleased(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && getActiveAnnot()!=null)
			{
			if(!r.activeAnnot.snd().equals(r.activeAnnotNew))
				new UndoOpPutObject("Move annotation", r.activeAnnotNew, w.getRootObject(), getActiveAnnot().fst()).execute();
			setActiveAnnot(null);
			}
		}

	private void setPos(TextAnnot a, MouseEvent e)
		{
		Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
		a.pos.x=v.x;
		a.pos.y=v.y;
		a.pos.z=w.getZ().doubleValue();
		}
	
	public void mouseMoved(MouseEvent e, int dx, int dy){}
	public void keyPressed(KeyEvent e){}
	public void paintComponent(Graphics g){}
	public void keyReleased(KeyEvent e){}

	public void mouseExited(MouseEvent e)
		{
		if(getActiveAnnot()!=null)
			{
			EvContainer data=w.getRootObject();
			if(data!=null)
				new UndoOpPutObject("Delete annotation", null, data, getActiveAnnot().fst()).execute();
			setActiveAnnot(null);
			w.updateImagePanel();
			}
		}

	
	public void deselected() {}


	private void setActiveAnnot(Tuple<String,TextAnnot> activeAnnot)
		{
		r.activeAnnot = activeAnnot;
		if(activeAnnot==null)
			r.activeAnnotNew=null;
		else
			r.activeAnnotNew=activeAnnot.snd().clone();
		}


	private Tuple<String,TextAnnot> getActiveAnnot()
		{
		return r.activeAnnot;
		}
	}
