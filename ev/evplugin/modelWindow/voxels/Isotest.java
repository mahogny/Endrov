package evplugin.modelWindow.voxels;

import java.nio.*;

import javax.media.opengl.*;
import javax.vecmath.Vector3f;

import com.sun.opengl.util.*;



//http://www.java-tips.org/other-api-tips/jogl/vertex-buffer-objects-nehe-tutorial-jogl-port-2.html


public class Isotest
	{
	
	
	Isosurface iso=new Isosurface();
	FloatBuffer vertb;
	FloatBuffer vertn;
	IntBuffer indb;
	
	
	public Isotest()
		{
		int w=400,h=400,z=200;

		
		float ptScalarField[]=new float[w*h*z];
		for(int i=0;i<w;i++)
			for(int j=0;j<h;j++)
				for(int k=0;k<z;k++)
					{
					ptScalarField[i*z*h + j*z + k]=new Vector3f(i-w/2,j-h/2,k-z/2).length();
					}
		
		long time=System.currentTimeMillis();
		
		//3s for 400x400x200
		iso.generateSurface(ptScalarField, (float)(w/4), w-1, h-1, z-1, 2.0f, 2.0f, 2.0f);

		long time3=System.currentTimeMillis();
		
		//15ms for 400x400x200
		vertb=BufferUtil.newFloatBuffer(iso.m_ppt3dVertices.length*3);
		for(int i=0;i<iso.m_ppt3dVertices.length;i++)
			{
			vertb.put(iso.m_ppt3dVertices[i].x);
			vertb.put(iso.m_ppt3dVertices[i].y);
			vertb.put(iso.m_ppt3dVertices[i].z);
			}

		long time4=System.currentTimeMillis();

		vertn=BufferUtil.newFloatBuffer(iso.m_pvec3dNormals.length*3);
		for(int i=0;i<iso.m_pvec3dNormals.length;i++)
			{
			vertn.put(iso.m_pvec3dNormals[i].x);
			vertn.put(iso.m_pvec3dNormals[i].y);
			vertn.put(iso.m_pvec3dNormals[i].z);
			}

		long time5=System.currentTimeMillis();

		//27ms for 400x400x200 
		indb=BufferUtil.newIntBuffer(iso.m_piTriangleIndices.length); 
		for(int i:iso.m_piTriangleIndices)
			indb.put(i);
	
		long time2=System.currentTimeMillis();
		System.out.println("load time: "+(time2-time)+" / "+ (time3-time)+" "+(time4-time3)+" "+(time5-time4)); 
		//TOTAL 0.3 s for 20x20x20   0.6s for 200x200x200   3.2s for 400x400x200

		}
	
	public void render(GL gl)
		{
		vertb.rewind();
		vertn.rewind();
		indb.rewind();
		gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
		gl.glEnableClientState( GL.GL_NORMAL_ARRAY );

		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);
		gl.glColor3d(1,1,1);


		long time=System.currentTimeMillis();
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertb);
		gl.glNormalPointer(GL.GL_FLOAT, 0, vertn);
		gl.glDrawElements(GL.GL_TRIANGLES, iso.m_piTriangleIndices.length, GL.GL_UNSIGNED_INT, indb);
		long time2=System.currentTimeMillis();

		System.out.println("rnder time: "+(time2-time)); // 10milli for 200x200x200   //60milli for w=400,h=400,z=200;
		
//		phong shading TODO

		gl.glDisable(GL.GL_LIGHTING);

		gl.glDisableClientState( GL.GL_VERTEX_ARRAY );
		gl.glDisableClientState( GL.GL_NORMAL_ARRAY );
		}
	

	}
