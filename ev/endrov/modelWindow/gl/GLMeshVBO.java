package endrov.modelWindow.gl;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import endrov.basicWindow.EvColor;

/**
 * Mesh that has been prepared for efficient rendering
 * 
 * @author Johan Henriksson
 *
 */
public class GLMeshVBO
	{
	public boolean drawNormals=false;
	
	public boolean drawSolid=true;
	
	public EvColor outlineColor=null;
	public float outlineWidth=5;
	
	public boolean useVBO=false;
	public int vertVBO;
	public int normalsVBO;
	public int texVBO;
	public int vertexCount;
	public FloatBuffer vertices;
	public FloatBuffer normals;
	public FloatBuffer tex;
	
	
	
	public void render(GL2 gl, GLMaterial material)
		{
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);  
	
		if(!useVBO/*vertices!=null*/)
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
	
		



		

		gl.glEnable(GL2.GL_CULL_FACE);
	
		material.set(gl);
		
		
		if(drawSolid)
			{
			gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexCount);  
			}
		else
			{
			for(int i=0;i<vertexCount;i+=3)
				gl.glDrawArrays(GL.GL_LINE_LOOP, i, 3);
			}
		
	
		gl.glDisable(GL2.GL_CULL_FACE);
		gl.glDisable(GL2.GL_LIGHTING);
	
		//Draw optional outline
		if(outlineColor!=null)
			{
			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);

			gl.glDisable(GL2.GL_LIGHTING);
			gl.glEnable(GL2.GL_CULL_FACE);

			gl.glColor3d((float)outlineColor.getRedDouble(),(float)outlineColor.getGreenDouble(),(float)outlineColor.getBlueDouble());
			gl.glLineWidth(outlineWidth);
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
		if(drawNormals)
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