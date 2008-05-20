package evplugin.modelWindow.voxel;

import java.awt.*;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.swing.SwingUtilities;


import evplugin.ev.Tuple;
import evplugin.ev.Vector3D;
import evplugin.filter.FilterSeq;
import evplugin.imageset.*;
import evplugin.imageset.Imageset.ChannelImages;
import evplugin.modelWindow.Camera;
import evplugin.modelWindow.ModelWindow;



/**
 * Render stack using 3d texture
 * @author Johan Henriksson
 */
public class Stack3D
	{	
	Double lastframe=null; 
	double resZ;
	//private TreeMap<Double,Vector<OneSlice>> texSlices=null;
	private TreeMap<Double,Vector<OneSlice>> texSlices=new TreeMap<Double,Vector<OneSlice>>();
	private final int skipForward=1; //later maybe allow this to change
	public boolean needLoadGL=false;
	
	
	private static class OneSlice
		{
		int w, h, d; //new variable d
		//double posZ; //start-z
		double resX,resY,resZ;
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
			System.out.println("size "+width+height+depth);
			//depth=64; //temp
			
			
			//b=ByteBuffer.allocate(width*height*depth);
			b.rewind();
			for(int k=0;k<depth;k++)
				for(int j=0;j<height;j++)
					for(int i=0;i<width;i++)
						{
						//if(i>2 && j>2 && i<10 && j<10)
						if((i>2 && j>2 && i<10 && j<10) || (i>500 && j>500 && i<510 && j<510))
							b.put((byte)128);
						else
							b.get();
//							b.put((byte)0);
						//b.put((byte)(((i+j+k)/5)%128));
						}
			
			gl.glEnable( GL.GL_TEXTURE_3D ); //does it have to be on here?

			System.out.println("error "+new GLU().gluErrorString(gl.glGetError()));

			
			System.out.println(""+width+" "+height+" "+depth+" "+b.position()+" "+b.remaining());
			int ids[]=new int[1];
			gl.glGenTextures(1, ids, 0);
			id=ids[0];
			bind(gl);
//			gl.glBindTexture(GL.GL_TEXTURE_3D, id);

			gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			
			gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
			gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
			gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_R, GL.GL_CLAMP);
			
//			gl.glTexImage3D(GL.GL_TEXTURE_3D, 0, GL.GL_ALPHA, width, height, depth, 0, GL.GL_ALPHA, GL.GL_UNSIGNED_BYTE, b);
System.out.println("error "+new GLU().gluErrorString(gl.glGetError()));

			gl.glTexImage3D(GL.GL_TEXTURE_3D, 0, GL.GL_ALPHA, 
					width, height, depth, 0, 
					GL.GL_ALPHA, GL.GL_UNSIGNED_BYTE, b.rewind());
/*			gl.glTexImage3D(GL.GL_TEXTURE_3D, 0, GL.GL_ALPHA, 
					width, height, depth, 0, 
					GL.GL_ALPHA, GL.GL_UNSIGNED_BYTE, b.rewind());*/
			//gl.glTexImage3D(GL.GL_TEXTURE_3D, 0, GL.GL_RGB, 
					//width, height, depth, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, b.rewind());
			
			
			System.out.println("error "+new GLU().gluErrorString(gl.glGetError()));
			
			gl.glDisable( GL.GL_TEXTURE_3D );

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
				Texture texture=new Texture();
				OneSlice os=null;
				
				
				int picpos=0;
				
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
								os.tex=texture;
								os.w=bim.getWidth();
								os.h=bim.getHeight();
								os.d=suitablePower2(slices.size());

								int bw=suitablePower2(os.w);
								os.resX/=os.w/(double)bw;
								os.w=bw;
								int bh=suitablePower2(os.h);
								os.resY/=os.h/(double)bh;
								os.h=bh;


								System.out.println("osd "+os.d);
								os.resX=evim.getResX()/evim.getBinning(); //px/um
								os.resY=evim.getResY()/evim.getBinning();
								os.resZ=chsel.im.meta.resZ;
								
								//TODO: compensate resZ by   (slices.lastKey()-slices.firstEntry())/slices.size() 
								//TODO: check slice2d if correct there
								
								os.color=chsel.color;
								texture.allocate(os.w, os.h, os.d);
								}
							
							


							//Load bitmap, scale down
							BufferedImage sim=new BufferedImage(os.w,os.h,BufferedImage.TYPE_BYTE_GRAY); 
//							BufferedImage sim=new BufferedImage(os.w,os.h,bim.getType()); //change type to byte or something? float better internally?
							Graphics2D g=(Graphics2D)sim.getGraphics();
							g.scale(os.w/(double)bim.getWidth(), os.h/(double)bim.getHeight()); //0.5 sec tot maybe
							g.drawImage(bim,0,0,Color.BLACK,null);
							
							//Convert to something suitable for texture upload?
							DataBufferByte buf=(DataBufferByte)sim.getRaster().getDataBuffer();
							//texture.b.put(buf.getData());
							
								Raster ras=sim.getRaster();
								for(int ay=0;ay<os.h;ay++)
									for(int ax=0;ax<os.w;ax++)
										{
										int pix[]=new int[3];
										ras.getPixel(ax, ay, pix);
										//texture.b.put((byte)ras.getSample(ax, ay, 0));
										texture.b.put((byte)pix[0]);
										}
				
							
							//use sim
							
//							break; //only one plane
							
							}
						}
				
				//Add black frames up to power of 2
				int pixToWrite=os.w*os.h*os.d-texture.b.position();
				texture.b.put(new byte[pixToWrite], 0, 0);
				
				getTexSlicesFrame(frame).add(os);
				
				curchannum++;
				}

			needLoadGL=true;
			SwingUtilities.invokeLater(new Runnable(){public void run(){w.progress.setValue(0);w.repaint();}});
			}
		}
	
	

	
	
	
	LinkedList<Tuple<BufferedImage,OneSlice>> procList=new LinkedList<Tuple<BufferedImage,OneSlice>>();
	public void loadGL(GL gl/*,double frame, Collection<ChannelSelection> channels*/)
		{
		//clean(gl); //need to clean somewhere
		System.out.println("uploading to GL");
		long t=System.currentTimeMillis();
		
//		for(getTexSlicesFrame(frame))
		if(texSlices!=null)
			for(Vector<OneSlice> osv:texSlices.values()) //TODO: replace
				for(OneSlice os:osv)
					{
					os.tex.upload(gl);
					}
		
		System.out.println("voxels loading ok:"+(System.currentTimeMillis()-t));
		}
	
	
	/**
	 * Round to best 2^
	 */
	private static int suitablePower2(int s)
		{
		//An option to restrict max texture size would be good
		if(true)return 256;
		
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
			//gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_ONE_MINUS_SRC_COLOR); //used before, makes no sense with alpha
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL.GL_BLEND); //disabled temp
			gl.glDepthMask(false);
			gl.glDisable(GL.GL_CULL_FACE);
			gl.glEnable(GL.GL_TEXTURE_3D);

			
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
					double d=os.d/os.resZ;

					for(float i=0;i<1.0;i+=0.005)
					//float i=0.5f;
						{
						float tz=i;
						float posz=(float)(i*d);

						gl.glBegin(GL.GL_QUADS);
						gl.glColor3d(os.color.getRed()/255.0, os.color.getGreen()/255.0, os.color.getBlue()/255.0);
						gl.glTexCoord3f(os.tex.left(), os.tex.top(),    tz); gl.glVertex3d(0, 0, posz); 
						gl.glTexCoord3f(os.tex.right(),os.tex.top(),    tz); gl.glVertex3d(w, 0, posz);
						gl.glTexCoord3f(os.tex.right(),os.tex.bottom(), tz); gl.glVertex3d(w, h, posz);
						gl.glTexCoord3f(os.tex.left(), os.tex.bottom(), tz); gl.glVertex3d(0, h, posz);
						gl.glEnd();
						}		
						

					os.tex.disable();
					}
				}




			gl.glDisable(GL.GL_TEXTURE_3D);
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
