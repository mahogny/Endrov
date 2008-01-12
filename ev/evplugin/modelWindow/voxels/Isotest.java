package evplugin.modelWindow.voxels;

import java.nio.*;

import javax.media.opengl.*;

import com.sun.opengl.util.*;

public class Isotest
	{
	float ptScalarField[]={
			0,0,0,0,
			0,0,0,0,
			0,0,0,0,
			0,0,0,0,

			0,0,0,0,
			0,1,1,0,
			0,1,1,0,
			0,0,0,0,

			0,0,0,0,
			0,1,1,0,
			0,1,1,0,
			0,0,0,0,

			0,0,0,0,
			0,0,0,0,
			0,0,0,0,
			0,0,0,0};
	
	Isosurface iso=new Isosurface();
	
	
	public Isotest()
		{
	
		
		iso.generateSurface(ptScalarField, 0.5f, 3, 3, 3, 1.0f, 1.0f, 1.0f);
//		iso.generateSurface(ptScalarField, 0.5f, 4, 4, 4, 1.0f, 1.0f, 1.0f);

		
		
		System.out.println(" "+iso.m_piTriangleIndices.length+" "+iso.m_ppt3dVertices.length+" "+iso.m_pvec3dNormals.length);
		
		
		}
	
	public void render(GL gl)
		{
		FloatBuffer vertb=BufferUtil.newFloatBuffer(iso.m_ppt3dVertices.length);
		for(int i=0;i<iso.m_ppt3dVertices.length;i++)
			{
			vertb.put(iso.m_ppt3dVertices[i].x);
			vertb.put(iso.m_ppt3dVertices[i].y);
			vertb.put(iso.m_ppt3dVertices[i].z);
			}
		vertb.rewind();

		
		
		
	 for(int i = 0; i < indices.length; i++)
        indicesBuf.put(indices[i]);
      indicesBuf.rewind();
	
	gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertb);
    gl.glColorPointer(3, GL.GL_FLOAT, 0, tmpColorsBuf);
    
		gl.
		
		
		}
	

	public static void main(String[] s)
		{
		

		
	
		}
	}
