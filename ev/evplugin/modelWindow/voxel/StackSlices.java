package evplugin.modelWindow.voxel;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import javax.media.opengl.*;
import com.sun.opengl.util.texture.*;

import evplugin.ev.Vector3D;
import evplugin.imageset.*;
import evplugin.imageset.Imageset.ChannelImages;
import evplugin.modelWindow.Camera;
import evplugin.modelWindow.ModelWindow;
import evplugin.modelWindow.ModelWindowGrid;

//if one ever wish to build it in the background:
//GLContext glc=view.getContext();
//glc.makeCurrent(); 
//GL gl=glc.getGL();
// ... glc.release();


/**
 * Render stack as several textured slices
 * @author Johan Henriksson
 */
public class StackSlices
	{	
	int lastframe; 
	double resZ;
	TreeMap<Double,Vector<OneSlice>> texSlices=null;
	private final int skipForward=1; //later maybe allow this to change
	
	
	private static class OneSlice
		{
		int w, h;
		double z;
		double resX,resY;
		Texture tex;
		Color color;
		}
	
	
	/**
	 * Dispose stack. Need GL context, forced by parameter.
	 */
	public void clean(GL gl)
		{
		if(texSlices!=null)
			for(Vector<OneSlice> osv:texSlices.values())
				for(OneSlice os:osv)
					os.tex.dispose();
		texSlices=null;
		}
	
	
	public static class ChannelSelection
		{
		Imageset im;
		ChannelImages ch;
		Color color=new Color(0,0,0);
		}
	
	public boolean needBuild(double frame)
		{
		return frame!=lastframe || !isBuilt();
		}
	
	/**
	 * Load stack into memory. Need GL context, forced by parameter.
	 */
	public void build(GL gl,double frame, Collection<ChannelSelection> channels)
		{
		if(needBuild(frame))
			{
			//Prepare for a new build
			clean(gl);
			texSlices=new TreeMap<Double,Vector<OneSlice>>();

			long t=System.currentTimeMillis();
	
			for(ChannelSelection chsel:channels)
				{
	
				int cframe=chsel.ch.closestFrame((int)Math.round(frame));
	
	
				System.out.println("loading");
				lastframe=cframe;
				resZ=chsel.im.meta.resZ;
	
	
				TreeMap<Integer,EvImage> slices=chsel.ch.imageLoader.get(cframe);
				int skipcount=0;
				if(slices!=null)
					for(int i:slices.keySet())
						{
						skipcount++;
						if(skipcount>=skipForward)
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
	
							int bw=suitablePower2(os.w);
							os.resX/=os.w/(double)bw;
							os.w=bw;
							int bh=suitablePower2(os.h);
							os.resY/=os.h/(double)bh;
							os.h=bh;
	
							//Load bitmap, scale down
							BufferedImage sim=new BufferedImage(os.w,os.h,bim.getType());
							Graphics2D g=(Graphics2D)sim.getGraphics();
							g.scale(os.w/(double)bim.getWidth(), os.h/(double)bim.getHeight()); //0.5 sec tot maybe
							g.drawImage(bim,0,0,Color.BLACK,null);
							bim=sim;
	
							os.tex=TextureIO.newTexture(bim,false);
							os.color=chsel.color;
	
							//Put it texture into list
							Vector<OneSlice> texSlicesV=texSlices.get(os.z);
							if(texSlicesV==null)
								{
								texSlicesV=new Vector<OneSlice>();
								texSlices.put(os.z, texSlicesV);
								}
							texSlicesV.add(os);
							}
						}
	
				}
			System.out.println("loading ok:"+(System.currentTimeMillis()-t));
			}
		}
	
	/**
	 * Round to best 2^
	 */
	private static int suitablePower2(int s)
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
	
	
	private boolean isBuilt()
		{
		return texSlices!=null;
		}
	
	/**
	 * Render entire stack
	 */
	public void render(GL gl, Camera cam)
		{
		if(isBuilt())
			{
//			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_ONE_MINUS_SRC_COLOR);
			gl.glEnable(GL.GL_BLEND);
			gl.glDepthMask(false);
			gl.glDisable(GL.GL_CULL_FACE);

			//Sort planes, O(n) since pre-ordered
			SortedMap<Double,Vector<OneSlice>> frontMap=texSlices.headMap(cam.pos.z);
			SortedMap<Double,Vector<OneSlice>> backMap=texSlices.tailMap(cam.pos.z);
			LinkedList<Vector<OneSlice>> frontList=new LinkedList<Vector<OneSlice>>();
			LinkedList<Vector<OneSlice>> backList=new LinkedList<Vector<OneSlice>>();
			frontList.addAll(frontMap.values());
			for(Vector<OneSlice> os:backMap.values())
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
	public void render(GL gl, LinkedList<Vector<OneSlice>> list)
		{
		for(Vector<OneSlice> osv:list)
			{
			for(OneSlice os:osv)
				{
			
				//Select texture
				os.tex.enable();
				os.tex.bind();
				
				
				//Find size and position
				double w=os.w/os.resX;
				double h=os.h/os.resY;
				TextureCoords tc=os.tex.getImageTexCoords();
				
				gl.glBegin(GL.GL_QUADS);
				//gl.glColor3d(1, 1, 1);
				gl.glColor3d(os.color.getRed()/255.0, os.color.getGreen()/255.0, os.color.getBlue()/255.0);
				
	//			gl.glColor4d(1, 1, 1, 0.2);
				gl.glTexCoord2f(tc.left(), tc.top());	   gl.glVertex3d(0, 0, os.z); //check
				gl.glTexCoord2f(tc.right(),tc.top());    gl.glVertex3d(w, 0, os.z);
				gl.glTexCoord2f(tc.right(),tc.bottom()); gl.glVertex3d(w, h, os.z);
				gl.glTexCoord2f(tc.left(), tc.bottom()); gl.glVertex3d(0, h, os.z);
				gl.glEnd();
	
				os.tex.disable();
				}
			}
		
		}
	
	
	
	public void adjustScale(ModelWindow w)
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			OneSlice os=texSlices.get(texSlices.firstKey()).get(0);
			double width=os.w/os.resX;
			
			//pan speed
			w.view.panspeed=width/1000.0;
			
			//Select grid size
			double g=Math.pow(10, (int)Math.log10(width));
			if(g<1) g=1;
			ModelWindowGrid.setGridSize(w,g);
			}
		
		}
	
	
	/**
	 * Give suitable center of all objects
	 */
	public Vector3D autoCenterMid()
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			OneSlice os=texSlices.get(texSlices.firstKey()).get(0);
			double width=os.w/os.resX;
			double height=os.h/os.resY;
			return new Vector3D(width/2.0,height/2.0,(texSlices.firstKey()+texSlices.lastKey())/2.0);
			}
		else
			return null;
		}
	
	
	/**
	 * Given a middle position, figure out radius required to fit objects
	 */
	public Double autoCenterRadius(Vector3D mid, double FOV)
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			OneSlice os=texSlices.get(texSlices.firstKey()).get(0);
			double width=os.w/os.resX;
			double height=os.h/os.resY;
			
			double[] list={Math.abs(0-mid.x),Math.abs(0-mid.y),Math.abs(texSlices.firstKey()-mid.z), 
					Math.abs(width-mid.x), Math.abs(height-mid.y), Math.abs(texSlices.lastKey()-mid.z)};
			double max=list[0];
			for(double d:list)
				if(d>max)
					max=d;
			
			//Find how far away the camera has to be. really have FOV in here?
			return max/Math.sin(FOV);
			}
		else
			return null;
		}
	
	
	
	
	
	
	
	}
