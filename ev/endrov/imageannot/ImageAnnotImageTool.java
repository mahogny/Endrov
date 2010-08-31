/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageannot;

import java.awt.*;
import java.awt.event.*;
import java.util.Map;

import javax.vecmath.*;
import javax.swing.*;

import endrov.data.*;
import endrov.imageWindow.*;
import endrov.undo.UndoOpPutObject;
import endrov.util.Tuple;

/**
 * Create and edit image annotation.
 *
 * @author Johan Henriksson
 */
public class ImageAnnotImageTool implements ImageWindowTool
	{
	private final ImageWindow w;
	private final ImageAnnotImageRenderer r;
	
	
	public ImageAnnotImageTool(ImageWindow w, ImageAnnotImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	

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
	
	
	private Tuple<String,ImageAnnot> getHoverAnnot(MouseEvent e)
		{
		Map<String,ImageAnnot> ann=r.getVisible();
		ImageAnnot closest=null;
		String closestName=null;
		double cdist=0;
		Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
		for(Map.Entry<String,ImageAnnot> ae:ann.entrySet())
			{
			ImageAnnot a=ae.getValue();
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
			Tuple<String,ImageAnnot> ae=getHoverAnnot(e);
			if(SwingUtilities.isLeftMouseButton(e))
				{
				if(ae==null)
					{
					//Create new
					String newtext=JOptionPane.showInputDialog(null, "Enter text");
					if(newtext!=null && !newtext.equals(""))
						{
						ImageAnnot a=new ImageAnnot();
						setPos(a,e);
						a.text=newtext;
						new UndoOpPutObject("Create annotation", a, data, data.getFreeChildName()).execute();
						}
					}
				else
					{
					//Rename
					ImageAnnot a=ae.snd();
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
			Tuple<String,ImageAnnot> a=getHoverAnnot(e);
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

	private void setPos(ImageAnnot a, MouseEvent e)
		{
		Vector2d v=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
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


	private void setActiveAnnot(Tuple<String,ImageAnnot> activeAnnot)
		{
		r.activeAnnot = activeAnnot;
		if(activeAnnot==null)
			r.activeAnnotNew=null;
		else
			r.activeAnnotNew=activeAnnot.snd().clone();
		}


	private Tuple<String,ImageAnnot> getActiveAnnot()
		{
		return r.activeAnnot;
		}
	}
