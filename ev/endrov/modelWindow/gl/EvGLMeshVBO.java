package endrov.modelWindow.gl;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Vector3f;

import endrov.basicWindow.EvColor;

/**
 * Mesh that has been prepared for efficient rendering
 * 
 * @author Johan Henriksson
 *
 */
public class EvGLMeshVBO
	{
	
	public static class MeshRenderSettings
		{
		public boolean drawSolid=true;
		public boolean drawNormals=false;
		//public boolean drawNormals=true;
		public boolean drawWireframe=false;
		public EvColor outlineColor=null;
		public float outlineWidth=5;
		}
		
	
	public boolean useVBO;
	public int vertVBO;
	public int normalsVBO;
	public int texVBO;
	public int vertexCount;
	public FloatBuffer vertices;
	public FloatBuffer normals;
	public FloatBuffer tex;
	
	
	
	public void render(GL2 gl, EvGLMaterial material, MeshRenderSettings settings)
		{
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);  
	

		if(!useVBO)
			{
			//Use vertex arrays
			vertices.rewind();
			tex.rewind();
			normals.rewind();
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertices); 
			gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, tex); 
			gl.glNormalPointer(GL.GL_FLOAT, 0, normals);
			}
		else
			{
			//Use VBO
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, texVBO);
			gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);
			
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertVBO);
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);    
			
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, normalsVBO);
			gl.glNormalPointer(GL.GL_FLOAT, 0, 0);
			}



		gl.glEnable(GL2.GL_NORMALIZE); //temp


		//Draw a solid mesh
		if(settings.drawSolid)
			{
			gl.glEnable(GL2.GL_CULL_FACE);
			
			material.set(gl);
			if(settings.drawWireframe)
				{
				for(int i=0;i<vertexCount;i+=3)
					gl.glDrawArrays(GL.GL_LINE_LOOP, i, 3);
				}
			else
				{
				gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexCount);  
				}
			
			
			
			
			}
	
		/*
		 //safe renderer
		if(true)
			{
			gl.glEnable(GL2.GL_NORMALIZE);

			new EvGLMaterialSolid().set(gl);
			
			vertices.rewind();
			for(int i=0;i<vertexCount/3;i++)
				{
				Vector3f v[]=new Vector3f[3];
				for(int j=0;j<3;j++)
					{
					v[j]=new Vector3f();
					v[j].x=vertices.get();
					v[j].y=vertices.get();
					v[j].z=vertices.get();
					}
				Vector3f vec1=new Vector3f(v[1]);
				vec1.sub(v[0]);
				Vector3f vec2=new Vector3f(v[2]);
				vec2.sub(v[0]);
				Vector3f norm=new Vector3f();
				norm.cross(vec1,vec2);
				norm.normalize();
				//gl.glBegin(GL.GL_LINE_LOOP);
				gl.glBegin(GL.GL_TRIANGLES);
				gl.glNormal3f(norm.x, norm.y, norm.z);
				gl.glColor3f(1.0f, 1.0f, 1.0f);
				for(int j=0;j<3;j++)
					gl.glVertex3f(v[j].x, v[j].y, v[j].z);
				gl.glEnd();
				
				}
			
			
			
			}
			*/
		
		gl.glDisable(GL2.GL_CULL_FACE);
		gl.glDisable(GL2.GL_LIGHTING);
	
		//Draw optional outline
		if(settings.outlineColor!=null && !settings.drawWireframe)
			{
			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);

			gl.glDisable(GL2.GL_LIGHTING);
			gl.glEnable(GL2.GL_CULL_FACE);

			
			gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, new float[]{1.0f,1.0f,1.0f}, 0);
			gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[]{1.0f,1.0f,1.0f}, 0);
			gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, new float[]{1.0f,1.0f,1.0f}, 0);
			gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 0);

			
			
			
			
			
			gl.glColor3d((float)settings.outlineColor.getRedDouble(),(float)settings.outlineColor.getGreenDouble(),(float)settings.outlineColor.getBlueDouble());
			gl.glLineWidth(settings.outlineWidth);
			//Back-facing polygons as wireframe
			gl.glPolygonMode(GL2.GL_BACK, GL2.GL_LINE ); 
			//Don't draw front-facing
			gl.glCullFace(GL2.GL_FRONT);     
			gl.glDepthFunc(GL2.GL_LEQUAL);

			gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexCount);  

			gl.glPopAttrib();
			}
		
		
		
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);  
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	
		//Draw optional normals
		if(settings.drawNormals)
			{
			normals.rewind();
			vertices.rewind();
			gl.glColor3f(1.0f, 1.0f, 1.0f);
			gl.glBegin(GL.GL_LINES);
			for(int i=0;i<vertexCount;i++)
				{
				float x=vertices.get();
				float y=vertices.get();
				float z=vertices.get();
				float nx=normals.get();
				float ny=normals.get();
				float nz=normals.get();
				
				gl.glVertex3f(x, y, z);
				gl.glVertex3f(x+nx, y+ny, z+nz);
				}
			gl.glEnd();
			}
		
		}
	
	public void destroy(GL2 gl)
		{
		gl.glDeleteBuffers(3, new int[]{vertVBO, normalsVBO, texVBO}, 0);
		}
	
	}