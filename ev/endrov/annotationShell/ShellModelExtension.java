/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.annotationShell;

import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.*;
import endrov.util.EvDecimal;
import endrov.windowViewer3D.*;

public class ShellModelExtension implements Viewer3DWindowExtension
	{
	public void newModelWindow(Viewer3DWindow w)
		{
		w.modelWindowHooks.add(new Hook(w));
		}
	
	public class Hook implements Viewer3DHook
		{
		private Viewer3DWindow w;
		
		public Hook(Viewer3DWindow w)
			{
			this.w=w;
			}
		
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void fillModelWindowMenus(){}
		public void datachangedEvent(){}
		
		
		public boolean canRender(EvObject ob)
			{
			return ob instanceof Shell;
			}
		
		public void displaySelect(GL gl) {}
		public void select(int pixelid) {}
		
		public Collection<Vector3d> autoCenterMid()
			{
			return Collections.emptySet();
			/*
			if(1==1)
				return Collections.emptySet();
			List<Vector3d> col=new LinkedList<Vector3d>();
			for(Shell s:w.getVisibleObjects(Shell.class))
				col.add(new Vector3d(s.midx,s.midy,s.midz));
			return col;
			*/
			}
		public double autoCenterRadius(Vector3d mid)
			{
			return 0; //TODO
			}
		
		
		public Collection<BoundingBox3D> adjustScale()
			{
			return Collections.emptySet();
			}
		
		
		public void initOpenGL(GL gl)
			{
			}

		
		public void displayInit(GL gl)
			{
			}
		
		
		public void displayFinal(GL glin,List<TransparentRenderer3D> transparentRenderers)
			{
			GL2 gl=glin.getGL2();
			for(Shell shell:w.getVisibleObjects(Shell.class))
				{
				gl.glPushMatrix();

				//Move into position
				gl.glTranslated(shell.midx,shell.midy,shell.midz);
				gl.glRotated(shell.angle*180/Math.PI, 0, 0, 1);

				//Render
				gl.glColor3d(1, 0, 0);			
				renderEllipse(gl, shell.major, shell.minor);
				gl.glBegin(GL.GL_LINES);
					gl.glVertex3d(shell.major, 0, 0);
					gl.glVertex3d(shell.major*1.1, 0, 0);
				gl.glEnd();
				gl.glRotated(90, 1, 0, 0);
				renderEllipse(gl, shell.major, shell.minor);
				gl.glRotated(90, 0, 1, 0);
				renderEllipse(gl, shell.minor, shell.minor);

				gl.glPopMatrix();
				}
			
			}
		
		
		/**
		 * Render an ellipse on xy-plane
		 */
		private void renderEllipse(GL2 gl, double major, double minor)
			{
			gl.glBegin(GL.GL_LINE_LOOP);
			for(double alpha=0;alpha<2*Math.PI;alpha+=0.2)
				gl.glVertex3d(major*Math.cos(alpha), minor*Math.sin(alpha), 0);
			gl.glEnd();
			}
		
		public EvDecimal getFirstFrame(){return null;}
		public EvDecimal getLastFrame(){return null;}
		}
	
	}
