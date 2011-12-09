package endrov.modelWindow.gl;

import javax.media.opengl.GL2;

import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * Solid color material
 * 
 * @author Johan Henriksson
 *
 */
public class EvGLMaterialSolid implements EvGLMaterial
	{
	public static final String metadataType="materialsolid";

	
	public float[] diffuse;
	public float[] specular;
	public float[] ambient;
	public float shininess;
	
	//TODO what about telling how to render? transparency, wireframe etc?
	
	/**
	 * Default OpenGL material
	 */
	public EvGLMaterialSolid()
		{
		this(null, null, null, null);
		}
	
	/**
	 * Create a material. All vectors have 4 elements. null means use GL default
	 */
	public EvGLMaterialSolid(float[] diffuse, float[] specular, float[] ambient, Float shininess)
		{
		this.diffuse = diffuse;
		this.specular = specular;
		this.ambient = ambient;
		if(shininess!=null)
			this.shininess = shininess;
		else
			this.shininess = 0;
	
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
	
	
	public Element toXML()
		{
		Element e=new Element(metadataType);
		
		for(int i=0;i<4;i++)
			{
			e.setAttribute("d"+i, Double.toString(diffuse[i]));
			e.setAttribute("s"+i, Double.toString(specular[i]));
			e.setAttribute("a"+i, Double.toString(ambient[i]));
			}
		e.setAttribute("shin", Double.toString(shininess));
		
		return e;
		}
	
	
	public static EvGLMaterialSolid fromXML(Element e)
		{
		try
			{
			float[] diffuse=new float[4];
			float[] specular=new float[4];
			float[] ambient=new float[4];
			
			for(int i=0;i<4;i++)
				{
				diffuse[i]=e.getAttribute("d"+i).getFloatValue();
				specular[i]=e.getAttribute("s"+i).getFloatValue();
				ambient[i]=e.getAttribute("a"+i).getFloatValue();			
				}
			float shininess=e.getAttribute("shin").getFloatValue();
			
			return new EvGLMaterialSolid(diffuse, specular, ambient, shininess);
			}
		catch (DataConversionException e1)
			{
			throw new RuntimeException(e1.getMessage());
			}
		
		
		}
	
	
	public static EvGLMaterialSolid fromColor(float colR, float colG, float colB)
		{
		EvGLMaterialSolid m=new EvGLMaterialSolid(
				new float[]{colR, colG, colB}, //diffuse
				new float[]{colR*0.01f, colG*0.01f, colB*0.01f}, //specular
				new float[]{0,0,0}, //ambient
				0.1f);
		return m;
		}
	}
	

