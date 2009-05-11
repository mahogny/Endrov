package endrov.modelWindowImset.slice3d;

import java.awt.*;
import java.awt.image.*;
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
	EvDecimal lastframe; 

	int w, h;
	double resX,resY,resZ;
	Texture tex;      
	boolean rebuild;
	
	
	/**
	 * Dispose stack. Need GL context, forced by parameter.
	 */
	public void clean(GL gl)
		{
		if(tex!=null)
			{
			tex.dispose();
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
	public void build(GL gl,EvDecimal frame, Imageset im, EvChannel ch, EvDecimal zplane)
		{
		if(needBuild(frame))
			{
			//Prepare for a new build
			clean(gl);
			rebuild=false;

			EvDecimal cframe=ch.closestFrame(frame);
			zplane=ch.closestZ(cframe, zplane);

			lastframe=frame;

			//Load image
			EvStack stack=ch.imageLoader.get(cframe);
			EvImage evim=stack.get(zplane);
			EvPixels p=evim.getPixels();
			BufferedImage bim=p.quickReadOnlyAWT();
			w=p.getWidth();
			h=p.getHeight();
			resX=stack.resX/stack.binning;//evim.getResX()/evim.getBinning(); //px/um
			resY=stack.resY/stack.binning;//evim.getResY()/evim.getBinning();
			//resZ=im.meta.resZ;

			//Load bitmap, scale down. Not needed, little data.
			/*
			int bw=suitablePower2(w);
			resX/=w/(double)bw;
			w=bw;
			int bh=suitablePower2(h);
			resY/=h/(double)bh;
			h=bh;
			BufferedImage sim=new BufferedImage(w,h,bim.getType());
			Graphics2D g=(Graphics2D)sim.getGraphics();
			g.scale(w/(double)bim.getWidth(), h/(double)bim.getHeight()); //0.5 sec tot maybe
			g.drawImage(bim,0,0,Color.BLACK,null);
			bim=sim;
			 */
			
			tex=TextureIO.newTexture(bim,false);
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
	public void render(GL gl, Color color, EvDecimal zplane)
		{
		if(isBuilt())
			{
			double z=zplane.doubleValue();
			gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS); //bother to refine?
			
			gl.glDisable(GL.GL_CULL_FACE);

			//Select texture
			tex.enable();
			tex.bind();
			
			//Find size and position
			double w=this.w/resX;
			double h=this.h/resY;
			TextureCoords tc=tex.getImageTexCoords();
			
			gl.glBegin(GL.GL_QUADS);
			gl.glColor3d(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
			
			gl.glTexCoord2f(tc.left(), tc.top());	   gl.glVertex3d(0, 0, z); //check
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
