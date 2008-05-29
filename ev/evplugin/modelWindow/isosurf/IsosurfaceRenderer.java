package evplugin.modelWindow.isosurf;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Collections;

import javax.media.opengl.GL;

import evplugin.ev.Vector3D;
import evplugin.modelWindow.ModelWindow;



//can replace float with short, half memory => twice basic speed
//*should* load images while rendering, skipping the ptscalarstep

//Use VBO to only upload once

//role of generator half moved here. just need the list management. or register callbacks?

/**
 * Renderer of isosurfaces
 * @author Johan Henriksson
 */
public class IsosurfaceRenderer
	{
	public FloatBuffer vertb;
	public FloatBuffer vertn;
	public IntBuffer indb;
	public float maxX,maxY,maxZ,minX,minY,minZ;
	
	public Collection<Double> adjustScale(ModelWindow w)
		{
		return Collections.singleton((double)(maxX-minX));
		}
	
	/**
	 * Give suitable center of all objects
	 */
	public Vector3D autoCenterMid()
		{
		return new Vector3D((maxX+minX)/2,(maxY+minY)/2,(maxZ+minZ)/2);
		}
	
	
	/**
	 * Given a middle position, figure out radius required to fit objects
	 */
	public Double autoCenterRadius(Vector3D mid, double FOV)
		{
		double[] list={Math.abs(minX-mid.x),Math.abs(minY-mid.y),Math.abs(minZ-mid.z),
				Math.abs(maxX-mid.x), Math.abs(maxY-mid.y), Math.abs(maxZ-mid.z)};
		double max=list[0];
		for(double d:list)
			if(d>max)
				max=d;
		//Find how far away the camera has to be. really have FOV in here?
		return max/Math.sin(FOV);
		}
	
	
	/**
	 * Render surface
	 */
	public void render(GL gl, float red, float green, float blue, float trans)
		{
		if(vertb!=null)
			{
			gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS); //bother to refine?
	
			float lightDiffuse[]=new float[]{1f, 1f, 1f, 0};
			float lightAmbient[] = { 0.3f, 0.3f, 0.3f, 0 }; 
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);   
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);
			//GL_LIGHT_MODEL_TWO_SIDE false right now
	
			boolean doTransparent=trans<0.99;
	
			if(doTransparent)
				{
				//NEHE does additive instead
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
				gl.glEnable(GL.GL_BLEND);
				gl.glDepthMask(false);
				gl.glDisable(GL.GL_CULL_FACE);
	
				gl.glColorMaterial ( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE ) ;
				gl.glEnable ( GL.GL_COLOR_MATERIAL ) ;
				}
	
			vertb.rewind();
			vertn.rewind();
			indb.rewind();
			gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
			gl.glEnableClientState( GL.GL_NORMAL_ARRAY );
			gl.glDisable(GL.GL_CULL_FACE);
			gl.glEnable(GL.GL_LIGHTING);
			gl.glEnable(GL.GL_LIGHT0);
			gl.glEnable ( GL.GL_COLOR_MATERIAL ) ;
			gl.glColor4f(red,green,blue,trans);
	
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertb);
			gl.glNormalPointer(GL.GL_FLOAT, 0, vertn);
			gl.glDrawElements(GL.GL_TRIANGLES, indb.remaining(), GL.GL_UNSIGNED_INT, indb);
			}
		gl.glPopAttrib();
		}
	
	}
