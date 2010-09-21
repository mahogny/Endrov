package endrov.modelWindow.gl;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 * Mesh that has been prepared for efficient rendering
 * 
 * @author Johan Henriksson
 *
 */
public class GLMeshVBO
	{
	public int vertVBO;
	public int normVBO;
	public int texVBO;
	public int vertexCount;
	
	public FloatBuffer vertices;
	public FloatBuffer norms;
	public FloatBuffer tex;
	
	public boolean hasVBO;
	
	public void render(GL2 gl, GLMaterial material)
		{
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);  
	
		if(vertices!=null)
			{
			//Use vertex arrays
			vertices.rewind();
			tex.rewind();
			norms.rewind();
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertices); 
			gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, tex); 
			gl.glNormalPointer(GL.GL_FLOAT, 0, norms);
			}
		else
			{
			//Use VBO
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, texVBO);
			gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);    
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertVBO);
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);    
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, normVBO);
			gl.glNormalPointer(GL.GL_FLOAT, 0, 0);
			}
	
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_CULL_FACE);
	
		material.set(gl);
		
		//        gl.glDrawArrays(GL.GL_LINES, 0, vertexCount);  
		gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexCount);  
	
		gl.glDisable(GL2.GL_CULL_FACE);
		gl.glDisable(GL2.GL_LIGHTING);
	
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);  
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	
		}
	
	public void destroy(GL2 gl)
		{
		gl.glDeleteBuffers(3, new int[]{vertVBO, normVBO, texVBO}, 0);
		}
	
	}