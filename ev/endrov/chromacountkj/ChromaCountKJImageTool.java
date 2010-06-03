/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.chromacountkj;

import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

import javax.vecmath.*;
import javax.swing.*;

import endrov.data.EvContainer;
import endrov.imageWindow.*;

/**
 * Create and edit image annotation.
 *
 * @author Johan Henriksson
 */
public class ChromaCountKJImageTool implements ImageWindowTool
	{
	private final ImageWindow w;
	//private final ChromaCountKJImageRenderer r;

	private int vicinityR=3;

	private ChromaCountKJ activeAnnot=null;
	
	private int curGroup=0;

	private int lastMouseX=0;
	private int lastMouseY;
	
	public ChromaCountKJImageTool(ImageWindow w/*, ChromaCountKJImageRenderer r*/)
		{
		this.w=w;
		//this.r=r;
		}
	

	public JMenuItem getMenuItem()
		{
		JCheckBoxMenuItem mi=new JCheckBoxMenuItem("ChromaCount KJ");
		mi.setSelected(w.getTool()==this);
		final ImageWindowTool This=this;
		mi.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){w.setTool(This);}
		});
		return mi;
		}
	
	
	private Collection<ChromaCountKJ> getAnnots()
		{
		ChromaCountKJImageRenderer r=w.getRendererClass(ChromaCountKJImageRenderer.class);
		return r.getVisible();
		}
	
	
	private ChromaCountKJ getHoverAnnot(MouseEvent e)
		{
		return getHoverAnnot(e.getX(),e.getY());
		}
	private ChromaCountKJ getHoverAnnot(int x, int y)
		{
		Collection<ChromaCountKJ> ann=getAnnots();
		ChromaCountKJ closest=null;
		double cdist=0;
		Vector2d v=w.transformS2W(new Vector2d(x,y));
		for(ChromaCountKJ a:ann)
			{
			double dist=(a.pos.x-v.x)*(a.pos.x-v.x) + (a.pos.y-v.y)*(a.pos.y-v.y);
			if(cdist>dist || closest==null)
				{
				cdist=dist;
				closest=a;
				}
			}
		double sdist=w.scaleW2s(cdist);
		if(sdist<vicinityR*vicinityR)
			return closest;
		else
			return null;
		}
	
	
	public void mouseClicked(MouseEvent e, Component invoker)
		{
		EvContainer data=w.getRootObject();
		if(data!=null)
			{
			ChromaCountKJ a=getHoverAnnot(e);
			if(SwingUtilities.isLeftMouseButton(e))
				{
				//Create
				a=new ChromaCountKJ();
				a.group=curGroup;
				setPos(a,e);
				data.addMetaObject(a);
				data.setMetadataModified();
				w.updateImagePanel();
				}
			else if(SwingUtilities.isRightMouseButton(e))
				{
				if(a!=null)
					curGroup=a.group;
				else
					{
					curGroup=-1;
					boolean found;
					do
						{
						found=false;
						curGroup++;
						for(ChromaCountKJ c:data.getObjects(ChromaCountKJ.class))
							if(c.group==curGroup)
								found=true;
						} while(found);
					}
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
			ChromaCountKJ a=getHoverAnnot(e);
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

	private void setPos(ChromaCountKJ a, MouseEvent e)
		{
		Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
		a.pos.x=v.x;
		a.pos.y=v.y;
		a.pos.z=w.frameControl.getZ().doubleValue();
		}
	
	public void mouseMoved(MouseEvent e, int dx, int dy)
		{
		lastMouseX=e.getX();
		lastMouseY=e.getY();
		}
	public void keyPressed(KeyEvent e)
		{
		EvContainer data=w.getRootObject();
		System.out.println("kp "+e.getKeyChar()+" "+data);
		if(data!=null)
			{
			ChromaCountKJ a=getHoverAnnot(lastMouseX,lastMouseY);
			System.out.println("hover "+a);
			if(e.getKeyCode()==KeyEvent.VK_Z)
				{
				if(a!=null)
					{
					data.removeMetaObjectByValue(a);
					System.out.println("removed "+a);
					}
				else
					System.out.println("no object");
				w.updateImagePanel();
				}
			}
		}
	public void paintComponent(Graphics g){}
	public void keyReleased(KeyEvent e){}

	public void mouseExited(MouseEvent e)
		{
		if(activeAnnot!=null)
			{
			EvContainer data=w.getRootObject();
			if(data!=null)
				data.removeMetaObjectByValue(activeAnnot);
			activeAnnot=null;
			w.updateImagePanel();
			}
		}

	
	public void deselected() {}
	}
