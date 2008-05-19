package evplugin.modelWindow.voxel;

import java.awt.*;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;
import javax.swing.SwingUtilities;


import evplugin.ev.Tuple;
import evplugin.ev.Vector3D;
import evplugin.filter.FilterSeq;
import evplugin.imageset.*;
import evplugin.imageset.Imageset.ChannelImages;
import evplugin.modelWindow.Camera;
import evplugin.modelWindow.ModelWindow;

//if one ever wish to build it in the background:
//GLContext glc=view.getContext();
//glc.makeCurrent(); 
//GL gl=glc.getGL();
// ... glc.release();


/**
 * should allow multiple texture units to be used, cut texture transfer rate when sorting
 */

/*
uploading texture in BG
http://lists.apple.com/archives/Mac-opengl/2007/Feb/msg00063.html
*/


					
					//gl.GL_MAX_3D_TEXTURE_SIZE
					//gl.GL_MAX_TEXTURE_UNITS
					
					
					/**
					 * 
					 * JOGL http://www.felixgers.de/teaching/jogl/texture3D.html
					 * 
					 * unsigned int texname;
glGenTextures(1, &texname);
glBindTexture(GL_TEXTURE_3D, texname);
glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_REPEAT);
glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_REPEAT);
glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_REPEAT);
glTexImage3D(GL_TEXTURE_3D, 0, GL_RGB8, WIDTH, HEIGHT, DEPTH, 0, GL_RGB, 
             GL_UNSIGNED_BYTE, texels);
             
             power of 2 in x,y,z
             
             */ 
             /*
             GL_MAX_TEXTURE_SIZE,
This is only an estimate
    glTexImage2D(GL_PROXY_TEXTURE_2D, level, internalFormat, width, height, border, format, type, NULL); 
Note the pixels parameter is NULL, because OpenGL doesn't load texel data when the target parameter is GL_PROXY_TEXTURE_2D. Instead, OpenGL merely considers whether it can accommodate a texture of the specified size and description. If the specified texture can't be accommodated, the width and height texture values will be set to zero. After making a texture proxy call, you'll want to query these values as follows:
    GLint width; glGetTexLevelParameteriv(GL_PROXY_TEXTURE_2D, 0, GL_TEXTURE_WIDTH, &width); if (width==0) { cannot use } 
             
					 */


/**
 * Render stack using 3d texture
 * @author Johan Henriksson
 */
public class Stack3D
	{	
	Double lastframe=null; 
	double resZ;
	private TreeMap<Double,Vector<OneSlice>> texSlices=null;
	private final int skipForward=1; //later maybe allow this to change
	public boolean needLoadGL=false;
	
	
	private static class OneSlice
		{
		int w, h, d; //new variable d
		double z; //start-z
		double resX,resY;
		Texture tex; //could be multiple textures, interleaved
		Color color;
		}
	
	
	private static class Texture
		{
		public int id;
		public ByteBuffer b=null;
		public int width, height, depth;
		public synchronized void allocate(int width, int height, int depth)
			{
			if(b==null)
				{
				b=ByteBuffer.allocate(width*height*depth);
				this.width=width;
				this.height=height;
				this.depth=depth;
				//b.pu
				}
			}
		public void upload(GL gl)
			{
			int ids[]=new int[1];
			gl.glGenTextures(1, ids, 1);
			id=ids[0];
			gl.glBindTexture(GL.GL_TEXTURE_3D, id);
			/*
glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_REPEAT);
glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_REPEAT);
glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_REPEAT);
*/
			gl.glTexImage3D(GL.GL_TEXTURE_3D, 0, GL.GL_ALPHA, width, height, depth, 0, GL.GL_ALPHA, GL.GL_UNSIGNED_BYTE, b);
			}
		public void dispose(GL gl)
			{
			int texlist[]={id};
			gl.glDeleteTextures(1, texlist, 0);
			}
		
		public void enable()
			{
			}
		public void disable()
			{
			}
		
		public void bind(GL gl)
			{
			gl.glBindTexture(GL.GL_TEXTURE_3D, id);
			}
		
		public float left(){return 0;}
		public float right(){return 1;}
		public float top(){return 0;}
		public float bottom(){return 1;}
		}
	
	public static class ChannelSelection
		{
		Imageset im;
		ChannelImages ch;
		FilterSeq filterSeq;
		Color color=new Color(0,0,0);
		}
	
	
	/**
	 * Get or create slices for one z. Has to be synchronized
	 */ 
	private synchronized Vector<OneSlice> getTexSlicesFrame(double z)
		{
		//Put it texture into list
		Vector<OneSlice> texSlicesV=texSlices.get(z);
		if(texSlicesV==null)
			{
			texSlicesV=new Vector<OneSlice>();
			texSlices.put(z, texSlicesV);
			}
		return texSlicesV;
		}
		
		
	
	
	
	
	/**
	 * Dispose stack. Need GL context, forced by parameter.
	 */
	public void clean(GL gl)
		{
		if(texSlices!=null)
			for(Vector<OneSlice> osv:texSlices.values())
				for(OneSlice os:osv)
					os.tex.dispose(gl);
		texSlices=null;
		}
	
	

	
	public boolean needSettings(double frame)
		{
		return lastframe==null || frame!=lastframe;// || !isBuilt();
		}
	
	
	public void startBuildThread(double frame, HashMap<ChannelImages, Stack3D.ChannelSelection> chsel,ModelWindow w)
		{
		stopBuildThread();
		buildThread=new BuildThread(frame, chsel, w);
		buildThread.start();
		}
	public void stopBuildThread()
		{
		if(buildThread!=null)
			buildThread.stop=true;
		}
	
	BuildThread buildThread=null;
	public class BuildThread extends Thread
		{
		private double frame;
		private HashMap<ChannelImages, Stack3D.ChannelSelection> chsel;
		public boolean stop=false;
		private ModelWindow w;
		public BuildThread(double frame, HashMap<ChannelImages, Stack3D.ChannelSelection> chsel,ModelWindow w)
			{
			this.frame=frame;
			this.chsel=chsel;
			this.w=w;
			}
		public void run()
			{
			SwingUtilities.invokeLater(new Runnable(){public void run(){w.progress.setValue(0);}});
			
			//im cache safety issues
			Collection<ChannelSelection> channels=chsel.values();
			procList.clear();
			int curchannum=0;
			for(ChannelSelection chsel:channels)
				{
				int cframe=chsel.ch.closestFrame((int)Math.round(frame));
				//Common resolution for all channels
				resZ=chsel.im.meta.resZ;


				//For every Z
				TreeMap<Integer,EvImage> slices=chsel.ch.imageLoader.get(cframe);
				int numz=slices.size();
				Texture texture=new Texture();
				OneSlice os=null;
				os.tex=texture;
				
				int skipcount=0;
				if(slices!=null)
					for(int i:slices.keySet())
						{
						if(stop)
							{
							SwingUtilities.invokeLater(new Runnable(){public void run(){w.progress.setValue(0);}});
							return; //Just stop thread if needed
							}
						skipcount++;
						if(skipcount>=skipForward)
							{
							final int progressSlices=i*1000/(channels.size()*slices.size());
							final int progressChan=1000*curchannum/channels.size();
							SwingUtilities.invokeLater(new Runnable(){public void run(){w.progress.setValue(progressSlices+progressChan);}});
							
							skipcount=0;
//							System.out.println("loading #"+i);
							EvImage evim=slices.get(i);
							if(!chsel.filterSeq.isIdentity())
								evim=chsel.filterSeq.applyReturnImage(evim);
							
							
							////////////////
							
							BufferedImage bim=evim.getJavaImage(); //1-2 sec tot?
							
							if(os==null)
								{
								os=new OneSlice();
								os.w=bim.getWidth();
								os.h=bim.getHeight();
								os.resX=evim.getResX()/evim.getBinning(); //px/um
								os.resY=evim.getResY()/evim.getBinning();
								//os.z=z/resZ;
								os.z=0;
								os.color=chsel.color;
								texture.allocate(os.w, os.h, numz);
								}
							
							

							int bw=suitablePower2(os.w);
							os.resX/=os.w/(double)bw;
							os.w=bw;
							int bh=suitablePower2(os.h);
							os.resY/=os.h/(double)bh;
							os.h=bh;

							//Load bitmap, scale down
							BufferedImage sim=new BufferedImage(os.w,os.h,BufferedImage.TYPE_BYTE_GRAY); 
//							BufferedImage sim=new BufferedImage(os.w,os.h,bim.getType()); //change type to byte or something? float better internally?
							Graphics2D g=(Graphics2D)sim.getGraphics();
							g.scale(os.w/(double)bim.getWidth(), os.h/(double)bim.getHeight()); //0.5 sec tot maybe
							g.drawImage(bim,0,0,Color.BLACK,null);
							
							//Convert to something suitable for texture upload?
							
							DataBufferByte buf=(DataBufferByte)sim.getRaster().getDataBuffer();
							////
							
							//use sim
							
							
							
							//Tuple<BufferedImage,OneSlice> proc=processImage(evim, i, chsel);
							processImage(evim, i, chsel, texture, os); //TODO
							//procList.add(proc);
							}
						}
				curchannum++;
				}

			needLoadGL=true;
			SwingUtilities.invokeLater(new Runnable(){public void run(){w.progress.setValue(0);w.repaint();}});
			}
		}
	
	

	
	public void addSlice(GL gl, List<Tuple<BufferedImage,OneSlice>> procList)
		{
		//int po=gl.glCreateProgramObjectARB()
		//int a=gl.glCreateShaderObjectARB(GL.GL_VERTEX_SHADER_ARB);
		//int my_fragment_shader = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);
		//gl.glShaderSourceARB(arg0, arg1, arg2, arg3, arg4)
		//gl.glCompileShaderARB(arg0)
		//gl.glAttachObjectARB(po, my_vertex_shader);
		//gl.glLinkProgramARB(po);
		//gl.glUseProgramObjectARB(po);
/*		void glDeleteObjectARB(GLhandleARB object)
		glGetInfoLogARB(GLhandleARB object, GLsizei maxLenght, GLsizei *length, GLcharARB *infoLog)
glUseProgramObjectARB(my_program);
int my_vec3_location = glGetUniformLocationARB(my_program, “my_3d_vector”);
glUniform3fARB(my_vec3_location, 1.0f, 4.0f, 3.0f);
http://nehe.gamedev.net/data/articles/article.asp?article=21 */
		
		clean(gl);
		texSlices=new TreeMap<Double,Vector<OneSlice>>();
		for(Tuple<BufferedImage,OneSlice> proc:procList)
			{
			OneSlice os=proc.snd();
//			os.tex=TextureIO.newTexture(proc.fst(),false);
			
			Vector<OneSlice> texSlicesV=getTexSlicesFrame(os.z);
			texSlicesV.add(os);
			}
		}
	
	LinkedList<Tuple<BufferedImage,OneSlice>> procList=new LinkedList<Tuple<BufferedImage,OneSlice>>();
	public void loadGL(GL gl/*,double frame, Collection<ChannelSelection> channels*/)
		{
		System.out.println("uploading to GL");
		long t=System.currentTimeMillis();
		addSlice(gl,procList);
		System.out.println("voxels loading ok:"+(System.currentTimeMillis()-t));
		}
	
	
	/**
	 * Round to best 2^
	 */
	private static int suitablePower2(int s)
		{
		//An option to restrict max texture size would be good
//		if(true)return 256;
		
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
	public void render(GL gl, Camera cam)
		{
		if(isBuilt())
			{
			gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_ONE_MINUS_SRC_COLOR);
//			gl.glEnable(GL.GL_BLEND); //disabled temp
			gl.glDepthMask(false);
			gl.glDisable(GL.GL_CULL_FACE);
			gl.glEnable(GL.GL_TEXTURE);
			
			
			for(Vector<OneSlice> osv:texSlices.values())
				{
				for(OneSlice os:osv)
					{
					//Select texture
					os.tex.enable();
					os.tex.bind(gl); //NOOPs

					//Find size and position
					double w=os.w/os.resX;
					double h=os.h/os.resY;

					gl.glBegin(GL.GL_QUADS);
					gl.glColor3d(os.color.getRed()/255.0, os.color.getGreen()/255.0, os.color.getBlue()/255.0);

					for(int i=0;i<10;i++)
						{
						float tz=i/10.0f;
						float posz=i; //+os.z

						gl.glTexCoord3f(os.tex.left(), os.tex.top(), tz);	  gl.glVertex3d(0, 0, posz); 
						gl.glTexCoord3f(os.tex.right(),os.tex.top(), tz);    gl.glVertex3d(w, 0, posz);
						gl.glTexCoord3f(os.tex.right(),os.tex.bottom(), tz); gl.glVertex3d(w, h, posz);
						gl.glTexCoord3f(os.tex.left(), os.tex.bottom(), tz); gl.glVertex3d(0, h, posz);
						gl.glEnd();
						}		
					os.tex.disable();
					}
				}





			gl.glDisable(GL.GL_TEXTURE);
			gl.glDisable(GL.GL_BLEND);
			gl.glDepthMask(true);
			gl.glEnable(GL.GL_CULL_FACE);
			}
		}

	
	private boolean isBuilt()
		{
		return texSlices!=null;
		}

	
	
	
	
	public Collection<Double> adjustScale(ModelWindow w)
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			OneSlice os=texSlices.get(texSlices.firstKey()).get(0);
			double width=os.w/os.resX;
			
			return Collections.singleton(width);
			}
		else
			return Collections.emptySet();
		}
	
	
	/**
	 * Give suitable center of all objects
	 */
	public Collection<Vector3D> autoCenterMid()
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			OneSlice os=texSlices.get(texSlices.firstKey()).get(0);
			double width=os.w/os.resX;
			double height=os.h/os.resY;
			return Collections.singleton(new Vector3D(width/2.0,height/2.0,(texSlices.firstKey()+texSlices.lastKey())/2.0));
			}
		else
			return Collections.emptySet();
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
