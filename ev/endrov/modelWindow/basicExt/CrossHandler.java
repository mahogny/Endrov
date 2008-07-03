package endrov.modelWindow.basicExt;

import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.media.opengl.GL;
import javax.swing.SwingUtilities;
import javax.vecmath.Vector3d;

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
		public int color;
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
	private Integer crossListStartId=0;
	private Integer crossHoverId=null;
	
	
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
	

	/**
	 * Feedback from listening 
	 */
	private ModelView.GLSelectListener crossListener=new ModelView.GLSelectListener()
		{
		public void hover(int id)
			{
			crossHoverId=id;
			}
		public void hoverInit(int id)
			{
			crossHoverId=null;
			}
		};
	
	/**
	 * Display for selection
	 */
	public void displayCrossSelect(GL gl, ModelWindow w)
		{
		crossListStartId=null;
		ModelView view=w.view;
		gl.glPushAttrib(GL.GL_ENABLE_BIT);
		for(int i=0;i<view.numClipPlanesSupported;i++)
			gl.glDisable(GL.GL_CLIP_PLANE0+i);
		for(Cross c:crossList)
			{
			int col1=view.reserveSelectColor(crossListener);
			int col2=view.reserveSelectColor(crossListener);
			int col3=view.reserveSelectColor(crossListener);
			if(crossListStartId==null)
				crossListStartId=col1;
//			float size=crossSizeFactor*(float)ModelWindowGrid.getGridSize(w);
			float size=crossSizeFactor*(float)w.view.getRepresentativeScale();
			
			gl.glPushMatrix();
			gl.glTranslated(c.v.x, c.v.y, c.v.z);
			gl.glLineWidth(8);//can be made wider
			gl.glBegin(GL.GL_LINES);
			view.setReserveColor(gl, col1);
			gl.glVertex3f(-size, 0, 0);gl.glVertex3f(size, 0, 0);
			view.setReserveColor(gl, col2);
			gl.glVertex3f(0,-size,  0);gl.glVertex3f(0, size,  0);
			view.setReserveColor(gl, col3);
			gl.glVertex3f(0, 0, -size);gl.glVertex3f(0, 0, size);
			gl.glEnd();
			gl.glLineWidth(1);
			gl.glPopMatrix();
			c.color=col1;
			}
		gl.glPopAttrib();
		}	
	
	/**
	 * Display for viewing
	 */
	public void displayCrossFinal(GL gl, ModelWindow w)
		{
		ModelView view=w.view;
		gl.glPushAttrib(GL.GL_ENABLE_BIT);
		for(int i=0;i<view.numClipPlanesSupported;i++)
			gl.glDisable(GL.GL_CLIP_PLANE0+i);
		for(Cross c:crossList)
			{
			gl.glPushMatrix();
			gl.glTranslated(c.v.x, c.v.y, c.v.z);
			//float size=crossSizeFactor*(float)ModelWindowGrid.getGridSize(w);
			float size=crossSizeFactor*(float)w.view.getRepresentativeScale();
			gl.glLineWidth(4);
			gl.glBegin(GL.GL_LINES);
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
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){listener=null;}
		public void mousePressed(MouseEvent e)
			{
			if(crossHoverId!=null && SwingUtilities.isLeftMouseButton(e))
				{
				int i=crossHoverId-crossListStartId;
				listener=crossList.get(i/3).listener;
				axis=i%3;
				System.out.println("press");
				}
			}
		public void mouseReleased(MouseEvent e){listener=null;}
		};
	}
