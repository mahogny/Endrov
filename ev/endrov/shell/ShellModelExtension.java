package endrov.shell;

import java.util.*;

import javax.media.opengl.GL;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.*;
import endrov.modelWindow.*;
import endrov.util.EvDecimal;

public class ShellModelExtension implements ModelWindowExtension
	{
	public void newModelWindow(ModelWindow w)
		{
		w.modelWindowHooks.add(new Hook(w));
		}
	
	public class Hook implements ModelWindowHook
		{
		private ModelWindow w;
		
		public Hook(ModelWindow w)
			{
			this.w=w;
			}
		
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void fillModelWindomMenus(){}
		public void datachangedEvent(){}
		
		
		public boolean canRender(EvObject ob)
			{
			return ob instanceof Shell;
			}
		
		public void displaySelect(GL gl) {}
		public void select(int pixelid) {}
		
		public Collection<Vector3d> autoCenterMid()
			{
			List<Vector3d> col=new LinkedList<Vector3d>();
			for(Shell s:w.getVisibleObjects(Shell.class))
				col.add(new Vector3d(s.midx,s.midy,s.midz));
			return col;
			}
		public Collection<Double> autoCenterRadius(Vector3d mid, double FOV)
			{
			return Collections.emptySet();
			}
		
		
		public Collection<Double> adjustScale()
			{
			return Collections.emptySet();
			}
		
		public void displayInit(GL gl)
			{
			}
		
		/*
		public Collection<Shell> getVisibleShell()
			{
			return w.getVisibleObjects(Shell.class);
			}
		*/
		
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
			{
			for(Shell shell:w.getVisibleObjects(Shell.class))
				{
				gl.glPushMatrix();
				
				//Move into position
				gl.glTranslated(shell.midx,shell.midy,shell.midz);
				gl.glRotated(shell.angle*180/Math.PI, 0, 0, 1);
				
				//Render
				gl.glColor3d(1, 0, 0);			
				renderEllipse(gl, shell.major, shell.minor);
				gl.glBegin(GL.GL_LINE);
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
		private void renderEllipse(GL gl, double major, double minor)
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
