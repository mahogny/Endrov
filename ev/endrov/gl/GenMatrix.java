package endrov.gl;

import javax.vecmath.Matrix4f;

/**
 * Generate OpenGL transformation matrices
 * @author Johan Henriksson
 *
 */
public class GenMatrix
	{
	/**
	 * NOTE: fovy in radians
	 */
	public static void gluPerspective(Matrix4f m, float fovy, float aspect, float znear, float zfar)
		{
		//http://steinsoft.net/index.php?site=Programming/Code Snippets/OpenGL/gluperspective
		float ymax=(float)(znear*Math.tan(fovy));
		float ymin=-ymax;
		float xmin=ymin*aspect;
		float xmax=ymax*aspect;
		glFrustum(m, xmin, xmax, ymin, ymax, znear, zfar);
		}
  
  
  public static void glFrustum(Matrix4f m, float left, float right, float bottom, float top, float znear, float zfar)
		{
		//http://www.glprogramming.com/red/appendixf.html
		m.m00=2*znear/(right-left);	m.m02=(right+left)/(right-left);
		m.m11=2*znear/(top-bottom); m.m12=(top+bottom)/(top-bottom);
		m.m22=-(zfar+znear)/(zfar-znear);	m.m23=-2*zfar*znear/(zfar-znear);
		m.m32=-1;
		}
  
  
  
	}
