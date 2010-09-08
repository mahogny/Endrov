/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindowImset.slice3d;

import java.awt.*;
import java.nio.DoubleBuffer;
import java.util.Collection;
import java.util.Collections;

import javax.media.opengl.*;
import javax.vecmath.Vector3d;

import com.sun.opengl.util.texture.*;

import endrov.imageset.*;
import endrov.util.EvDecimal;
import endrov.util.ImVector3d;

/**
 * Render one slice in 3d
 * @author Johan Henriksson
 */
public class Slice3D
	{	
	private EvDecimal lastframe; 

	private int w, h;
	private double resX,resY/*,resZ*/;
	private Texture tex;      
	private boolean rebuild;
	private double worldZ;
	
	
	/**
	 * Dispose stack. Need GL context, forced by parameter.
	 */
	public void clean(GL gl)
		{
		if(tex!=null)
			{
			tex.destroy(gl);
			tex=null;
			}
		}
	
	public void rebuild()
		{
		rebuild=true;
		}
	
	
	public static class ChannelSelection
		{
		
		EvChannel ch;
		Color color=new Color(0,0,0);
		}
	
	public boolean needBuild(EvDecimal frame)
		{
		return rebuild || !frame.equals(lastframe) || !isBuilt();
		}

	private boolean isBuilt()
		{
		return tex!=null && !rebuild;
		}
	
	/**
	 * Load stack into memory. Need GL context, forced by parameter.
	 */
	public void build(GL gl,EvDecimal frame, Imageset im, EvChannel ch, int zplane)
		{
		if(needBuild(frame))
			{
			//Prepare for a new build
			clean(gl);
			rebuild=false;

			EvDecimal cframe=ch.closestFrame(frame);
			
			lastframe=frame;

			//Load image
			EvStack stack=ch.imageLoader.get(cframe);
			if(zplane<0)
				zplane=0;
			if(zplane>stack.getDepth())
				zplane=stack.getDepth();
			worldZ=zplane*stack.resZ;
			EvImage evim=stack.getInt(zplane);
			EvPixels p=evim.getPixels();
			w=p.getWidth();
			h=p.getHeight();
			resX=stack.resX;
			resY=stack.resY;

			DoubleBuffer buffer=DoubleBuffer.allocate(w*h);
			buffer.put(p.convertToDouble(true).getArrayDouble());
			
			TextureData tdata=new TextureData(
					GL2.GL_ALPHA, w,h, 0, GL2.GL_ALPHA, GL2.GL_FLOAT, false, false, false, buffer, null);
			
			
			/*
			BufferedImage bim=p.quickReadOnlyAWT();
			tex=TextureIO.newTexture(bim,false);
			*/
			tex=TextureIO.newTexture(tdata);
			}
		}
	
	/**
	 * Round to best 2^
	 */
	public static int suitablePower2(int s)
		{
//		return 128;
		if(s>380) return 512;
		else if(s>192) return 256;
		else if(s>96) return 128;
		else if(s>48) return 64;
		else if(s>24) return 32;
		else if(s>12) return 16;
		else if(s>6) return 8;
		else return 4;
		}
	
	
	
	/**
	 * Render entire stack
	 */
	public void render(GL glin, Color color, boolean project)
		{
		GL2 gl=glin.getGL2();
		if(isBuilt())
			{
			double z=worldZ;
			if(project)
				z=0;
			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS); //bother to refine?
			
			gl.glDisable(GL2.GL_CULL_FACE);

			//Select texture
			tex.enable();
			tex.bind();
			
			//Find size and position
			double w=this.w*resX;
			double h=this.h*resY;
			TextureCoords tc=tex.getImageTexCoords();
			
			gl.glBegin(GL2.GL_QUADS);
			gl.glColor3d(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
			
			gl.glTexCoord2f(tc.left(), tc.top());	   gl.glVertex3d(0, 0, z);
			gl.glTexCoord2f(tc.right(),tc.top());    gl.glVertex3d(w, 0, z);
			gl.glTexCoord2f(tc.right(),tc.bottom()); gl.glVertex3d(w, h, z);
			gl.glTexCoord2f(tc.left(), tc.bottom()); gl.glVertex3d(0, h, z);
			gl.glEnd();

			tex.disable();
			
			gl.glPopAttrib();
			}
		}
	
	
	public Collection<Double> adjustScale()
		{
		if(tex!=null)
			return Collections.singleton(this.w/resX);
		else
			return Collections.emptySet();
		}
	
	/**
	 * Give suitable center of all objects
	 * TODO currently useless requiring z. need to move this value
	 */
	public Collection<ImVector3d> autoCenterMid(double z)
		{
		if(tex!=null)
			{
			double width=w/resX;
			double height=h/resY;
			return Collections.singleton(new ImVector3d(width/2.0,height/2.0,z));
			}
		else
			return Collections.emptySet();
		}
	
	
	/**
	 * Given a middle position, figure out radius required to fit objects
	 */
	public Collection<Double> autoCenterRadius(Vector3d mid, double FOV)
		{
		if(tex!=null)
			{
			double width=w/resX;
			double height=h/resY;
			
			double[] list={Math.abs(0-mid.x),Math.abs(0-mid.y), 
					Math.abs(width-mid.x), Math.abs(height-mid.y)};
			double max=list[0];
			for(double d:list)
				if(d>max)
					max=d;
			
			//Find how far away the camera has to be. really have FOV in here?
			return Collections.singleton(max/Math.sin(FOV));
			}
		else
			return Collections.emptySet();
///			return null;
		}
	
	
	}
