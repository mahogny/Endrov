/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindow.basicExt;

import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.vecmath.Vector3d;

import endrov.data.EvSelectObject;
import endrov.data.EvSelection;
import endrov.modelWindow.ModelView;
import endrov.modelWindow.ModelWindow;
import endrov.modelWindow.ModelWindowMouseListener;

/**
 * Movable crosses in model window
 * @author Johan Henriksson
 *
 */
public class CrossHandler
	{
	private static final float crossSizeFactor=0.15f;

	/**
	 * One cross
	 */
	private static class Cross
		{
		public Vector3d v;
		public CrossListener listener;
		//public int selectColorID;
		}

	/**
	 * Listener for movements of a cross
	 * @author Johan Henriksson
	 */
	public static interface CrossListener
		{
		public void crossmove(Vector3d diff);
		}

	private LinkedList<Cross> crossList=new LinkedList<Cross>();
	
	
	public void addCross(Vector3d v, CrossListener listener)
		{
		Cross cross=new Cross();
		cross.v=v;
		cross.listener=listener;
		crossList.add(cross);
		}
	public void resetCrossList()
		{
		crossList.clear();
		}
	

	
	
	public class EvSelectCross extends EvSelectObject<Cross>
		{
		private int axis;
		public EvSelectCross(Cross mesh, int axis)
			{
			super(mesh);
			this.axis=axis;
			}
		
		@Override
		public boolean equals(Object obj)
			{
			if(obj instanceof EvSelectCross)
				{
				EvSelectCross o=(EvSelectCross)obj;
				return o.axis==axis && o.getObject()==getObject();
				}
			else
				return false;
			}
		
		}
	
	
	/**
	 * Display for selection
	 */
	public void displayCrossSelect(GL glin, ModelWindow w)
		{
		GL2 gl=glin.getGL2();
		//crossListStartId=null;
		ModelView view=w.view;
		gl.glPushAttrib(GL2.GL_ENABLE_BIT);
		for(int i=0;i<view.numClipPlanesSupported;i++)
			gl.glDisable(GL2.GL_CLIP_PLANE0+i);
		for(Cross c:crossList)
			{
			int col1=view.reserveSelectColor(new EvSelectCross(c, 0));
			int col2=view.reserveSelectColor(new EvSelectCross(c, 1));
			int col3=view.reserveSelectColor(new EvSelectCross(c, 2));
			//if(crossListStartId==null)
			//	crossListStartId=col1;
			float size=crossSizeFactor*(float)w.view.getRepresentativeScale();
			
			gl.glPushMatrix();
			gl.glTranslated(c.v.x, c.v.y, c.v.z);
			gl.glLineWidth(8);//can be made wider
			gl.glBegin(GL2.GL_LINES);
			view.setReserveColor(gl, col1);
			gl.glVertex3f(-size, 0, 0);gl.glVertex3f(size, 0, 0);
			view.setReserveColor(gl, col2);
			gl.glVertex3f(0,-size,  0);gl.glVertex3f(0, size,  0);
			view.setReserveColor(gl, col3);
			gl.glVertex3f(0, 0, -size);gl.glVertex3f(0, 0, size);
			gl.glEnd();
			gl.glLineWidth(1);
			gl.glPopMatrix();
			//c.selectColorID=col1;
			}
		gl.glPopAttrib();
		}	
	
	/**
	 * Display for viewing
	 */
	public void displayCrossFinal(GL2 gl, ModelWindow w)
		{
		ModelView view=w.view;
		gl.glPushAttrib(GL2.GL_ENABLE_BIT);
		for(int i=0;i<view.numClipPlanesSupported;i++)
			gl.glDisable(GL2.GL_CLIP_PLANE0+i);
		for(Cross c:crossList)
			{
			gl.glPushMatrix();
			gl.glTranslated(c.v.x, c.v.y, c.v.z);
			//float size=crossSizeFactor*(float)ModelWindowGrid.getGridSize(w);
			float size=crossSizeFactor*(float)w.view.getRepresentativeScale();
			gl.glLineWidth(4);
			gl.glBegin(GL2.GL_LINES);
			gl.glColor3f(1,0,0);
			gl.glVertex3f(-size, 0, 0);gl.glVertex3f(size, 0, 0);
			gl.glColor3f(0,1,0);
			gl.glVertex3f(0,-size,  0);gl.glVertex3f(0, size,  0);
			gl.glColor3f(0,0,1);
			gl.glVertex3f(0, 0, -size);gl.glVertex3f(0, 0, size);
			gl.glEnd();
			gl.glLineWidth(1);
			gl.glPopMatrix();
			}
		gl.glPopAttrib();
		}
	
	
	/**
	 * Mouse listener
	 */
	public ModelWindowMouseListener crossMListener=new ModelWindowMouseListener()
		{
		private CrossListener listener=null; 
		private int axis=0;
		public boolean mouseDragged(MouseEvent e, int dx, int dy)
			{
			if(listener!=null)
				{
				//Project axis. dot with mouse
				Vector3d v=new Vector3d();
				if(axis==0)
					v.x+=dx;
				else if(axis==1)
					v.y+=dx;
				else if(axis==2)
					v.z+=dx;
				listener.crossmove(v);
				return true;
				}
			else
				return false;
			}
		public void mouseMoved(MouseEvent e){}
		public boolean mouseClicked(MouseEvent e, JPopupMenu menu){return false;}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){listener=null;}
		public void mousePressed(MouseEvent e)
			{
			if(EvSelection.currentHover!=null && EvSelection.currentHover instanceof EvSelectCross)
				{
				EvSelectCross crossSel=(EvSelectCross)EvSelection.currentHover;
				
				if(SwingUtilities.isLeftMouseButton(e))
					{
					listener=crossSel.getObject().listener;
					//int i=crossHoverId-crossListStartId;
					//listener=crossList.get(i/3).listener;
					axis=crossSel.axis;
					//axis=i%3;
					System.out.println("press");
					}
				}
			
			
			
			}
		public void mouseReleased(MouseEvent e){listener=null;}
		};
	}
