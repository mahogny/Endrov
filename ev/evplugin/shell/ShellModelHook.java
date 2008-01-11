package evplugin.shell;

import javax.media.opengl.GL;

import org.jdom.Element;

import evplugin.data.*;
import evplugin.ev.*;
import evplugin.modelWindow.*;

public class ShellModelHook implements ModelWindowHook
	{
	private ModelWindow w;
	
	public ShellModelHook(ModelWindow w)
		{
		this.w=w;
		}
	
	public void readPersonalConfig(Element e){}
	public void savePersonalConfig(Element e){}

	
	public boolean canRender(EvObject ob)
		{
		return ob instanceof Shell;
		}
	
	public void displaySelect(GL gl) {}
	public void select(int pixelid) {}

	public Vector3D autoCenterMid()
		{
		//TODO
		return null;
		}
	public Double autoCenterRadius(Vector3D mid, double FOV)
		{
		//TODO
		return null;
		}

	
	public void adjustScale()
		{
		//TODO
		}
	
	public void displayInit(GL gl)
		{
		}
	
	public void displayFinal(GL gl)
		{
		EvData metadata=w.view.getMetadata();
		if(metadata!=null)
			for(EvObject ob:metadata.metaObject.values())
				{
				if(ob instanceof Shell)
					renderShell(gl, (Shell)ob);
				}
		else
			if(EV.debugMode)
				System.out.println("No meta");
		}
	
	
	/**
	 * Render shell
	 */
	private void renderShell(GL gl, Shell shell)
		{
		//Check if shell really exists
		if(shell!=null && (shell.midx!=0 || shell.midy!=0 || shell.midz!=0))
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
	
	}
