package evplugin.modelWindow.isosurf;

import java.nio.*;

import javax.media.opengl.*;

import com.sun.opengl.util.*;

public class CopyOfIsotest
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
	FloatBuffer vertb;
	FloatBuffer vertn;
	IntBuffer indb;
	
	
	public CopyOfIsotest()
		{
	
		
	
		}
	
	public void render(GL gl)
		{
		
		
		
		
		iso.generateSurface(ptScalarField, 0.5f, 3, 3, 3, 5.0f, 5.0f, 5.0f);


	
	vertb=BufferUtil.newFloatBuffer(iso.m_ppt3dVertices.length*3);
	for(int i=0;i<iso.m_ppt3dVertices.length;i++)
		{
		vertb.put(iso.m_ppt3dVertices[i].x);
		vertb.put(iso.m_ppt3dVertices[i].y);
		vertb.put(iso.m_ppt3dVertices[i].z);
		}

	vertn=BufferUtil.newFloatBuffer(iso.m_pvec3dNormals.length*3);
	for(int i=0;i<iso.m_pvec3dNormals.length;i++)
		{
		vertn.put(iso.m_pvec3dNormals[i].x);
		vertn.put(iso.m_pvec3dNormals[i].y);
		vertn.put(iso.m_pvec3dNormals[i].z);
		}
  
	indb=BufferUtil.newIntBuffer(iso.m_piTriangleIndices.length);
	for(int j=0;j<iso.m_piTriangleIndices.length;j++)
		indb.put(iso.m_piTriangleIndices[j]); //paranoid
	for(int j=0;j<iso.m_piTriangleIndices.length;j+=3)
		System.out.println("bah "+iso.m_piTriangleIndices[j]+" "+iso.m_piTriangleIndices[j+1]+" "+iso.m_piTriangleIndices[j+2]);
	
//	for(int i:iso.m_piTriangleIndices)
//		indb.put(i);
	
	
	System.out.println(" #T "+iso.m_piTriangleIndices.length/3+"   #v "+iso.m_ppt3dVertices.length+"   #n "+iso.m_pvec3dNormals.length);
	
	
		
		
		
		
		
		
		
		
		
//	http://www.java-tips.org/other-api-tips/jogl/vertex-buffer-objects-nehe-tutorial-jogl-port-2.html
		
		
		////////////////////////////////
		
	
		vertb.rewind();
		vertn.rewind();
		indb.rewind();
		gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
		gl.glEnableClientState( GL.GL_INDEX_ARRAY );
		//gl.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY ); 
		
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertb);
    gl.glNormalPointer(GL.GL_FLOAT, 0, vertn);
		//gl.glIndexPointer(GL.GL_INT, 0, indb);
		//gl.glColorPointer(3, GL.GL_FLOAT, 0, tmpColorsBuf);

 //   gl.glDrawArrays(GL.GL_LINE_LOOP, 0, iso.m_piTriangleIndices.length ); //Draw the vertices

		/*
		gl.glBegin(GL.GL_TRIANGLES);
		for(int i=0;i<iso.m_piTriangleIndices.length;i++)
			{
			gl.glArrayElement(iso.m_piTriangleIndices[i]);
			}
		gl.glEnd();
		*/
		
		gl.glDrawElements(GL.GL_TRIANGLES, iso.m_piTriangleIndices.length, GL.GL_UNSIGNED_INT, indb);
		
//  gl.glDrawArrays(GL.GL_TRIANGLES, 0, iso.m_piTriangleIndices.length/3 ); //Draw the vertices
//		gl.glDrawArrays(GL.GL_TRIANGLES, 0, 3 ); //Draw the vertices
		
		//gl.glDrawArrays(GL.GL_POINTS, 0, iso.m_piTriangleIndices.length ); //Draw the vertices

		
		gl.glDisableClientState( GL.GL_VERTEX_ARRAY );
//		gl.glDisableClientState( GL.GL_INDEX_ARRAY );
		//gl.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY ); 
		
	
	
		}
	

	public static void main(String[] s)
		{
		

		
	
		}
	}
