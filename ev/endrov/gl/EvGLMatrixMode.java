/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
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
public class EvGLMatrixMode
	{
	ArrayList<Matrix4f> stack=new ArrayList<Matrix4f>();
	
	public EvGLMatrixMode()
		{
		Matrix4f s=new Matrix4f();
		s.setIdentity();
		stack.add(s);
		}
	
	
	
	
	
	
	}
