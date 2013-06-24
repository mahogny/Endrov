package endrov.gl;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import endrov.windowViewer3D.Viewer3DView;

/**
 * Class to upload and manage 3D textures
 */
public class EvGLTexture3D
	{
	private Integer id;
	public ByteBuffer b;
	public int width, height, depth;
	
	private EvGLTexture3D()
		{
		}
	
	
	public void needReinit()
		{
		id=null;
		}
	
	
	public static EvGLTexture3D allocate(int width, int height, int depth, Viewer3DView view)
		{
		
		EvGLTexture3D texture=new EvGLTexture3D();
		texture.b=ByteBuffer.allocate(width*height*depth);
		
		texture.width=width;
		texture.height=height;
		texture.depth=depth;

		view.registerTexture(texture);

		return texture;
		}
	
	/** 
	 * Upload texture to GL2. can be called multiple times, action is only taken first time 
	 */
	public void prepare(GL glin)
		{
		GL2 gl=glin.getGL2();
		if(id==null)
			{
			int ids[]=new int[1];
			gl.glGenTextures(1, ids, 0);
			id=ids[0];
			bind(gl);

			System.out.println("size "+width+" "+height+" "+depth+" "+id);

			gl.glEnable( GL2.GL_TEXTURE_3D ); //does it have to be on here?
			gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
			gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
			gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP);
			//gl.glTexImage3D(GL2.GL_TEXTURE_3D, 0, GL2.GL_ALPHA, width, height, depth, 0, GL2.GL_ALPHA, GL2.GL_UNSIGNED_BYTE, b);
			gl.glTexImage3D(GL2.GL_TEXTURE_3D, 0, GL2.GL_ALPHA, width, height, depth, 0, GL2.GL_ALPHA, GL2.GL_UNSIGNED_BYTE, b.rewind());
//				System.out.println("error "+new GLU().gluErrorString(gl.glGetError()));
			gl.glDisable( GL2.GL_TEXTURE_3D );
			Viewer3DView.checkerr(gl);
			
			//TODO handle the case of no support for non-POT textures. Either interpolate, or disable volume rendering on these
			}
		}
	
	
	public void dispose(GL gl)
		{
		if(id!=null)
			{
			int texlist[]={id};
			gl.glDeleteTextures(1, texlist, 0);
			}
		}
	
	
	public void bind(GL gl)
		{
		prepare(gl);
		
		//here we can do all the multitexturing stuff! clever caching should be enough that different stack
		//renderers need not know about each other
		gl.glBindTexture(GL2.GL_TEXTURE_3D, id);
		}
	}