package evplugin.modelWindow;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import javax.media.opengl.*;
import com.sun.opengl.util.texture.*;

import evplugin.data.*;
import evplugin.imageset.*;

/**
 * Render stack as several textured slices
 * @author Johan Henriksson
 */
public class StackSlices
	{	
	int lastframe;
	double resZ;
	TreeMap<Double,OneSlice> texSlices=null;
	
	private static class OneSlice
		{
		int w, h;
		double z;
		double resX,resY;
		Texture tex;
		}
	
	
	/**
	 * Dispose stack
	 */
	public void clean()
		{
		if(texSlices!=null)
			for(OneSlice os:texSlices.values())
				os.tex.dispose();
		texSlices=null;
		}
	
	
	
	/**
	 * Load stack into memory
	 */
	public void build(double frame)
		{
//		GLContext glc=view.getContext();
		//glc.makeCurrent(); 
		//GL gl=glc.getGL();
		// ... glc.release();
		
		String channelName="GFP";
		if(!EvData.metadata.isEmpty() && EvData.metadata.get(0) instanceof Imageset)
			{
			Imageset im=(Imageset)EvData.metadata.get(0);
			
			if(im.channelImages.containsKey(channelName))
				{
				//todo: get nearest. framecontrol?
				int cframe=im.channelImages.get(channelName).closestFrame((int)Math.round(frame));
				if(texSlices==null || cframe!=lastframe)
					{
					clean();
					texSlices=new TreeMap<Double,OneSlice>();
					
					System.out.println("loading");
					lastframe=cframe;
					resZ=im.meta.resZ;
					
					long t=System.currentTimeMillis();

					TreeMap<Integer,EvImage> slices=im.channelImages.get(channelName).imageLoader.get(cframe);
					int skipcount=0;
					if(slices!=null)
						for(int i:slices.keySet())
							{
							skipcount++;
							if(skipcount>=1)
								{
								skipcount=0;
								OneSlice os=new OneSlice();
								EvImage evim=slices.get(i);
								
								BufferedImage bim=evim.getJavaImage(); //1-2 sec tot?
								
								os.w=bim.getWidth();
								os.h=bim.getHeight();
								os.resX=evim.getResX()/evim.getBinning(); //px/um
								os.resY=evim.getResY()/evim.getBinning();
								os.z=i/resZ;
								
								int bw=bestSize(os.w);
								os.resX/=os.w/(double)bw;
								os.w=bw;
								int bh=bestSize(os.h);
								os.resY/=os.h/(double)bh;
								os.h=bh;
								
								//Load bitmap, scale down
								BufferedImage sim=new BufferedImage(os.w,os.h,bim.getType());
								Graphics2D g=(Graphics2D)sim.getGraphics();
								g.scale(os.w/(double)bim.getWidth(), os.h/(double)bim.getHeight()); //0.5 sec tot maybe
								g.drawImage(bim,0,0,Color.BLACK,null);
								bim=sim;

								
								os.tex=TextureIO.newTexture(bim,false);
								texSlices.put(os.z,os);
								}
							}
					
					System.out.println("loading ok:"+(System.currentTimeMillis()-t));
					}
				}
			}
		else
			clean();
		}

	/**
	 * Round to best 2^
	 */
	private static int bestSize(int s)
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
	public void render(GL gl, Camera cam, double frame)
		{
		build(frame);
		if(texSlices!=null)
			{
//			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_ONE_MINUS_SRC_COLOR);
			gl.glEnable(GL.GL_BLEND);
			gl.glDepthMask(false);
			gl.glDisable(GL.GL_CULL_FACE);

			//Sort planes, O(n) since pre-ordered
			SortedMap<Double,OneSlice> frontMap=texSlices.headMap(cam.pos.z);
			SortedMap<Double,OneSlice> backMap=texSlices.tailMap(cam.pos.z);
			LinkedList<OneSlice> frontList=new LinkedList<OneSlice>();
			LinkedList<OneSlice> backList=new LinkedList<OneSlice>();
			frontList.addAll(frontMap.values());
			for(OneSlice os:backMap.values())
				backList.addFirst(os);
			
			render(gl, frontList);
			render(gl, backList);
			
			gl.glDisable(GL.GL_BLEND);
			gl.glDepthMask(true);
			gl.glEnable(GL.GL_CULL_FACE);
			}
		}
	
	/**
	 * Render list of slices
	 */
	public void render(GL gl, LinkedList<OneSlice> list)
		{
		for(OneSlice os:list)
			{
			//Select texture
			os.tex.enable();
			os.tex.bind();
			
			
			//Find size and position
			double w=os.w/os.resX;
			double h=os.h/os.resY;
			TextureCoords tc=os.tex.getImageTexCoords();
			
			gl.glBegin(GL.GL_QUADS);
			gl.glColor3d(1, 1, 1);
//			gl.glColor4d(1, 1, 1, 0.2);
			gl.glTexCoord2f(tc.left(), tc.top());	   gl.glVertex3d(0, 0, os.z); //check
			gl.glTexCoord2f(tc.right(),tc.top());    gl.glVertex3d(w, 0, os.z);
			gl.glTexCoord2f(tc.right(),tc.bottom()); gl.glVertex3d(w, h, os.z);
			gl.glTexCoord2f(tc.left(), tc.bottom()); gl.glVertex3d(0, h, os.z);
			gl.glEnd();

			os.tex.disable();
			}
		
		}
	
	/*
	public void foo(GL gl)
		{
		Buffer b=new Buffer.ByteBuffer(
		int w=512;
		int h=512;
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB8, w, h, 0, GL.GL_RED, GL.GL_BYTE, b);
		}
	*/
	}
