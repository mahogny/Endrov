package endrov.modelWindow.gl;

import javax.media.opengl.GL2;

/**
 * Solid color material
 * 
 * @author Johan Henriksson
 *
 */
public class GLMaterialSolid implements GLMaterial
	{
	public float[] diffuse;
	public float[] specular;
	public float[] ambient;
	public float shininess;
	
	//TODO what about telling how to render? transparency, wireframe etc?
	
	/**
	 * Default OpenGL material
	 */
	public GLMaterialSolid()
		{
		this(null, null, null, 0);
		}
	
	/**
	 * Create a material. All vectors have 4 elements 
	 */
	public GLMaterialSolid(float[] diffuse, float[] specular, float[] ambient,	float shininess)
		{
		this.diffuse = diffuse;
		this.specular = specular;
		this.ambient = ambient;
		this.shininess = shininess;
	
		//Use GL default colors if none specified
		if(this.diffuse==null)
			this.diffuse=new float[]{0.8f,0.8f,0.8f,1f};
		if(this.specular==null)
			this.specular=new float[]{0f,0f,0f,1f};
		if(this.ambient==null)
			this.ambient=new float[]{0.2f,0.2f,0.2f,1f};
		
		}
	
	public void set(GL2 gl)
		{
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, diffuse, 0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular, 0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, ambient, 0);
		gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess);
		}
	
	@Override
	public String toString()
		{
		return "material, d:"+arrToString(diffuse)+
		" spec:"+arrToString(specular)+
		" amb:"+arrToString(ambient);
		}
	
	private static String arrToString(float[] arr)
		{
		return "["+arr[0]+","+arr[1]+","+arr[2]+","+arr[3]+"]";
		
		}
	
	
	}
	

