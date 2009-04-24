package endrov.gl;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;

/**
 * OpenGL 2 style Matrix stack. For use with OpenGL 3
 * 
 * I suspect it won't be needed a lot
 * 
 * @author mahogny
 *
 */
public class MatrixMode
	{
	ArrayList<Matrix4f> stack=new ArrayList<Matrix4f>();
	
	public MatrixMode()
		{
		Matrix4f s=new Matrix4f();
		s.setIdentity();
		stack.add(s);
		}
	
	
	
	
	
	
	}
