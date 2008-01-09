package evplugin.modelWindow;

import java.awt.*;
import java.awt.image.*;
import java.util.TreeMap;
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
	
	private static class OneSlice
		{
		int w, h;
		double resX,resY;
		Texture tex;
		}
	
	int lastframe;
	double resZ;
	TreeMap<Integer,OneSlice> texSlices=null;
	
	
	
	public void clean()
		{
		if(texSlices!=null)
			for(OneSlice os:texSlices.values())
				os.tex.dispose();
		texSlices=null;
		}
	
	
	public void build(double frame)
		{
		String channelName="DIC";
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
					texSlices=new TreeMap<Integer,OneSlice>();
					
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
								
								BufferedImage bim=evim.getJavaImage(); //1-2 sec tot
								
								os.w=bim.getWidth();
								os.h=bim.getHeight();
								os.resX=evim.getResX()/evim.getBinning(); //px/um
								os.resY=evim.getResY()/evim.getBinning();
								
								int bw=bestSize(os.w);
								os.resX*=os.w/(double)bw;
								os.w=bw;
								int bh=bestSize(os.h);
								os.resY*=os.h/(double)bh;
								os.h=bh;
								
								//Load bitmap, scale down
								BufferedImage sim=new BufferedImage(os.w,os.h,bim.getType());
								Graphics2D g=(Graphics2D)sim.getGraphics();
								g.scale(os.w/(double)bim.getWidth(), os.h/(double)bim.getHeight()); //0.5 sec tot maybe
								g.drawImage(bim,0,0,Color.BLACK,null);
								bim=sim;

								os.tex=TextureIO.newTexture(bim,false);
								texSlices.put(i,os);
								}
							}
					
					System.out.println("loading ok:"+(System.currentTimeMillis()-t));
					}
				}
			}
		else
			clean();
		}
	
	public static int bestSize(int s)
		{
		if(s>380)
			return 512;
		else if(s>192)
			return 256;
		else if(s>96)
			return 128;
		else
			return s;
		}
	
	
	//todo: render order
	
	public void render(GL gl, double frame)
		{
		build(frame);
		if(texSlices!=null)
			{
//			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_ONE_MINUS_SRC_COLOR);
			gl.glEnable(GL.GL_BLEND);
			gl.glDepthMask(false);
			gl.glDisable(GL.GL_CULL_FACE);
			
			for(int i:texSlices.keySet())
				{
				OneSlice os=texSlices.get(i);
				
				//Select texture
				os.tex.enable();
				os.tex.bind();
				
				//Find size and position
				double z=i/resZ;
				double w=os.w/os.resX;
				double h=os.h/os.resY;
				TextureCoords tc=os.tex.getImageTexCoords();
				
				gl.glBegin(GL.GL_QUADS);
				gl.glColor3d(1, 1, 1);
//				gl.glColor4d(1, 1, 1, 0.2);
				gl.glTexCoord2f(tc.left(), tc.top());	   gl.glVertex3d(0, 0, z); //check
				gl.glTexCoord2f(tc.right(),tc.top());    gl.glVertex3d(w, 0, z);
				gl.glTexCoord2f(tc.right(),tc.bottom()); gl.glVertex3d(w, h, z);
				gl.glTexCoord2f(tc.left(), tc.bottom()); gl.glVertex3d(0, h, z);
				gl.glEnd();

				os.tex.disable();
				}
			gl.glDisable(GL.GL_BLEND);
			gl.glDepthMask(true);
			gl.glEnable(GL.GL_CULL_FACE);
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
