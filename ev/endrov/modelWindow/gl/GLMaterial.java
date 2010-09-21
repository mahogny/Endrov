/**
 * 
 */
package endrov.modelWindow.gl;

import javax.media.opengl.GL2;

public class GLMaterial
	{
	public float[] diffuse;
	public float[] specular;
	public float[] ambient;
	public float shininess;
	
	//TODO what about telling how to render? transparency, wireframe etc?
	
	public GLMaterial(float[] diffuse, float[] specular, float[] ambient,	float shininess)
		{
		super();
		this.diffuse = diffuse;
		this.specular = specular;
		this.ambient = ambient;
		this.shininess = shininess;
		}
	
	public void set(GL2 gl)
		{
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, diffuse, 0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular, 0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, ambient, 0);
		gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess);
		}
	
	}