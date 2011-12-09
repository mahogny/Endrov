/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindow.gl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.LinkedList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;


import com.sun.opengl.util.BufferUtil;
import endrov.modelWindow.ModelView;

//www.typhoonlabs.com/tutorials/glsl/Chapter_5.pdf

/**
 * GLSL shader
 * 
 * TODO replace by http://download.java.net/media/jogl/jogl-2.x-docs/com/sun/opengl/util/glsl/ShaderState.html
 * it has all the goodies already!
 * 
 * @author Johan Henriksson
 */
public class EvGLShader
	{
	private Integer idf;
	private Integer idv;
	private Integer prog;

	private URL srcv, srcf;
	private boolean needInit;
	
	private String uploadURL(GL glin, int thisid, URL src, String type) throws IOException
		{
		GL2 gl=glin.getGL2();
		BufferedReader brf = new BufferedReader(new InputStreamReader(src.openStream()));
		String fsrc = "";
		String line;
		while ((line=brf.readLine()) != null)
		  fsrc += line + "\n";
		gl.glShaderSource(thisid, 1, new String[]{fsrc}, new int[]{fsrc.length()}, 0);
		gl.glCompileShader(thisid);
	  checkLogInfo(gl,thisid,type);
		return fsrc;
		}
	
	
	/**
	 * Create a shader. srcv and srcf can be null.
	 * use .class.getResource(...) to obtain URL.
	 */
	public EvGLShader(GL glin, URL srcv, URL srcf, ModelView view)
		{
		this.srcv=srcv;
		this.srcf=srcf;
		needInit=true;
		prepareShader(glin);
		view.registerShader(this);
		}
	
	public void needReinit()
		{
		needInit=true;
		idv=null;
		idf=null;
		prog=null;
		}
	
	public void prepareShader(GL glin)
		{
		if(needInit)
			{
			GL2 gl=glin.getGL2();
			try
				{
				ModelView.checkerr(gl);
				if(srcv!=null)
					{
					idv=gl.glCreateShader(GL2.GL_VERTEX_SHADER);
					uploadURL(gl, idv, srcv,"v");
					}
				if(srcf!=null)
					{
					idf=gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
					uploadURL(gl, idf, srcf,"f");
					}
				if(srcv==null && srcf==null)
					throw new RuntimeException("Tried to create shaded with neither vertex nor fragment program");
				
				ModelView.checkerr(gl);
				prog = gl.glCreateProgram();
				ModelView.checkerr(gl);
				if(idv!=null)	gl.glAttachShader(prog, idv);
				if(idf!=null)	gl.glAttachShader(prog, idf);
				ModelView.checkerr(gl);
				gl.glLinkProgram(prog);
				ModelView.checkerr(gl);
				gl.glValidateProgram(prog);
				ModelView.checkerr(gl);
				System.out.println("prog "+prog+" "+idv+" "+idf);
				
				needInit=false;
				}
			catch (IOException e)
				{
				System.out.println("Couldn't read sources "+srcv+" "+srcf);
				e.printStackTrace();
				}
			}
		}

	
  private void checkLogInfo(GL glin, int obj, String type)
  	{
		GL2 gl=glin.getGL2();
  	IntBuffer iVal = BufferUtil.newIntBuffer(1);
  	gl.glGetObjectParameterivARB(obj, GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, iVal);
  	int length = iVal.get();

  	if (length > 2) 
  		{
  		ByteBuffer infoLog = BufferUtil.newByteBuffer(length);

  		iVal.flip();
  		gl.glGetInfoLogARB(obj, length, iVal, infoLog);

  		byte[] infoBytes = new byte[length];
  		infoLog.get(infoBytes);
  		System.out.println("GLSL Validation ("+type+") >> " + new String(infoBytes));
  		}
  	}

	
	public void use(GL glin)
		{
		GL2 gl=glin.getGL2();

		ModelView.checkerr(gl);
		ModelView.checkerr(gl);
		gl.glUseProgram(prog); 	// http://www.opengl.org/sdk/docs/man/xhtml/glUseProgram.xml
		ModelView.checkerr(gl);
		
		//before bind
		int texUnit=0;
//		gl.glActiveTexture(GL2.GL_TEXTURE0 + texUnit);		
		
		ModelView.checkerr(gl);
		
		int texLoc=gl.glGetUniformLocation(prog, "tex");
		ModelView.checkerr(gl);
		gl.glUniform1i(texLoc, texUnit);

		ModelView.checkerr(gl);
		}
	
	public void stopUse(GL glin)
		{
		GL2 gl=glin.getGL2();
		ModelView.checkerr(gl);
		ModelView.checkerr(gl);
		gl.glUseProgram(0);
		ModelView.checkerr(gl);
		}
	
	public void delete(GL glin)
		{
		GL2 gl=glin.getGL2();
		if(idv!=null)	{gl.glDetachShader(prog, idv); gl.glDeleteShader(idv);}
		if(idf!=null)	{gl.glDetachShader(prog, idf); gl.glDeleteShader(idf);}
		if(prog!=null)
			gl.glDeleteProgram(prog);
		}

	/**
	 * Get the uniform variable names
	 */
	public Collection<String> getUniformNames(GL2 glin)
		{
		LinkedList<String> uniforms=new LinkedList<String>();
		IntBuffer total=IntBuffer.allocate(1);
		glin.glGetProgramiv( prog, GL2.GL_ACTIVE_UNIFORMS, total); 
		for(int i=0; i<total.get(0); ++i)
			{
			IntBuffer name_len=IntBuffer.allocate(1);
			IntBuffer num=IntBuffer.allocate(1);
			IntBuffer type=IntBuffer.allocate(1);
			ByteBuffer oneName=ByteBuffer.allocate(100);
			glin.glGetActiveUniform( prog, i, 100-1, name_len, num, type, oneName );
			String oneNameS="";
			oneName.rewind();
			while(oneName.hasRemaining())
				oneNameS=oneNameS+(char)oneName.get();

			uniforms.add(oneNameS);
			}
		return uniforms;
		}
	
	/**
	 * Get the OpenGL ID of a uniform variable
	 */
	public int getUniformLocation(GL2 glin, String name)
		{
		int id=glin.glGetUniformLocation(prog, name);
		if(id==-1)
			throw new RuntimeException("Cannot find uniform: "+name+", These are the uniforms: "+getUniformNames(glin));
		else
			return id;
		}
	
	}
