package evplugin.modelWindow.voxel;

import java.awt.*;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.swing.SwingUtilities;
import javax.vecmath.Vector3d;


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
		int w, h, d; //size in pixels
		//double posZ; //start-z
		double resX,resY,resZ;
		Texture tex; //could be multiple textures, interleaved
		Color color;
		
		double realw, realh, reald; //size in um
		}
	
	//Maybe merge oneslice & texture? somehow
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
				}
			}
		public void upload(GL gl)
			{
			System.out.println("size "+width+height+depth);
			
			//b=ByteBuffer.allocate(width*height*depth);
/*			b.rewind();
			for(int k=0;k<depth;k++)
				for(int j=0;j<height;j++)
					for(int i=0;i<width;i++)*/
						/*
						if((i>2 && j>2 && i<10 && j<10) || (i>500 && j>500 && i<510 && j<510))
							b.put((byte)128);
						else
							b.get();
							*/
						/*
						if((i>10 && j>10 && i<510 && j<510))
							b.put((byte)128);
						else
							b.put((byte)5);
			*/
						
			int ids[]=new int[1];
			gl.glGenTextures(1, ids, 0);
			id=ids[0];
			bind(gl);

			gl.glEnable( GL.GL_TEXTURE_3D ); //does it have to be on here?
			gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
			gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
			gl.glTexParameteri(GL.GL_TEXTURE_3D, GL.GL_TEXTURE_WRAP_R, GL.GL_CLAMP);
//			gl.glTexImage3D(GL.GL_TEXTURE_3D, 0, GL.GL_ALPHA, width, height, depth, 0, GL.GL_ALPHA, GL.GL_UNSIGNED_BYTE, b);
			gl.glTexImage3D(GL.GL_TEXTURE_3D, 0, GL.GL_ALPHA, width, height, depth, 0, GL.GL_ALPHA, GL.GL_UNSIGNED_BYTE, b.rewind());
			System.out.println("error "+new GLU().gluErrorString(gl.glGetError()));
			gl.glDisable( GL.GL_TEXTURE_3D );

			}
		public void dispose(GL gl)
			{
			int texlist[]={id};
			gl.glDeleteTextures(1, texlist, 0);
			}
		
		
		public void bind(GL gl)
			{
			gl.glBindTexture(GL.GL_TEXTURE_3D, id);
			}
		
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
		//Put texture into list
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
			
			//im. cache safety issues
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
								os.d=ceilPower2(slices.size());

								int bw=suitablePower2(os.w);
								os.resX/=os.w/(double)bw;
								os.w=bw;
								int bh=suitablePower2(os.h);
								os.resY/=os.h/(double)bh;
								os.h=bh;


								System.out.println("osd "+os.d);
								os.resX=evim.getResX()/evim.getBinning(); //[px/um]
								os.resY=evim.getResY()/evim.getBinning();
								os.resZ=chsel.im.meta.resZ;
								
								double zscale=(1+slices.lastKey()-slices.firstKey())/slices.size();
								System.out.println("zscale "+zscale);
								os.resZ/=zscale; //TODO: check if correct. unit?
								//TODO: check slice2d if correct there
								
								os.color=chsel.color;
								texture.allocate(os.w, os.h, os.d);
								
								//real size
								os.realw=os.w/os.resX;
								os.realh=os.h/os.resY;
								os.reald=os.d/os.resZ;
								}
							
							


							//Load bitmap, scale down
							BufferedImage sim=new BufferedImage(os.w,os.h,BufferedImage.TYPE_BYTE_GRAY); 
//							BufferedImage sim=new BufferedImage(os.w,os.h,bim.getType()); //change type to byte or something? float better internally?
							Graphics2D g=(Graphics2D)sim.getGraphics();
							g.scale(os.w/(double)bim.getWidth(), os.h/(double)bim.getHeight()); //0.5 sec tot maybe
							g.drawImage(bim,0,0,Color.BLACK,null);
							
							//Convert to something suitable for texture upload?
							//DataBufferByte buf=(DataBufferByte)sim.getRaster().getDataBuffer();
							//texture.b.put(buf.getData());
							
							//theoretically slower but can be made safer
								Raster ras=sim.getRaster();
								for(int ay=0;ay<os.h;ay++)
									for(int ax=0;ax<os.w;ax++)
										{
										int pix[]=new int[3];
										ras.getPixel(ax, ay, pix);
										//texture.b.put((byte)ras.getSample(ax, ay, 0));
										texture.b.put((byte)pix[0]);
										}
							
							
							
							}
						}
				
				//Add black frames up to z power of 2
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
		if(s>256) return 512;
		else if(s>192) return 256;
		else if(s>96) return 128;
		else if(s>48) return 64;
		else if(s>24) return 32;
		else if(s>12) return 16;
		else if(s>6) return 8;
		else return 4;
		}
	
	/**
	 * Ceil to 2^
	 */
	private static int ceilPower2(int s)
		{
		//TODO: also larger sizes
		if(s>512) return 1024;
		else if(s>256) return 512;
		else if(s>256) return 256;
		else if(s>64) return 128;
		else if(s>32) return 64;
		else if(s>16) return 32;
		else if(s>8) return 16;
		else if(s>4) return 8;
		else return 4;
		}
	
	
	/**
	 * Render entire stack
	 */
	public void render(GL gl, Camera cam)
		{
		//int[] planes=new int[1];		gl.glGetIntegerv(GL.GL_MAX_CLIP_PLANES, planes, 0);		System.out.println("planes "+planes[0]);
		//6 planes on NVidia macpro
		
		
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
					os.tex.bind(gl);
/*
					//Render all planes
					//for(float i=0;i<1.0;i+=0.005)
					float i=0.2f;
						{
						float tz=i;
						float posz=(float)(i*os.reald);

						gl.glBegin(GL.GL_QUADS);
						gl.glColor3d(os.color.getRed()/255.0, os.color.getGreen()/255.0, os.color.getBlue()/255.0);
						gl.glTexCoord3f(0, 0, tz); gl.glVertex3d(0,        0,        posz); 
						gl.glTexCoord3f(1, 0, tz); gl.glVertex3d(os.realw, 0,        posz);
						gl.glTexCoord3f(1, 1, tz); gl.glVertex3d(os.realw, os.realh, posz);
						gl.glTexCoord3f(0, 1, tz); gl.glVertex3d(0,        os.realh, posz);
						gl.glEnd();
						}		
						*/
					System.out.println("=====================================");
					System.out.println("=====================================");
					System.out.println("=====================================");
					System.out.println("=====================================");
					renderPlane(gl, cam, os);
					}
				}




			gl.glDisable(GL.GL_TEXTURE_3D);
			gl.glDisable(GL.GL_BLEND);
			gl.glDepthMask(true);
			gl.glEnable(GL.GL_CULL_FACE);
			}
		}

	private static class Plane
		{
		public Plane(){}
		public Plane(double A, double B, double C, double D){this.A=A;this.B=B;this.C=C;this.D=D;}
		//Ax+By+Cz=D
		double A, B, C, D; 
		}

	//Lines over z
	private static Vector3d intersectPlane1(Plane p, OneSlice os)
		{
		if(p.C==0) return null;
		double z=p.D/p.C;
		if(z<0 || z>os.reald) return null;
		return new Vector3d(0,0,z);
		}
	private static Vector3d intersectPlane2(Plane p, OneSlice os)
		{
		if(p.C==0) return null;
		double z=(p.D-p.A*os.realw)/p.C;
		if(z<0 || z>os.reald) return null;
		return new Vector3d(os.realw,0,z);
		}
	private static Vector3d intersectPlane3(Plane p, OneSlice os)
		{
		if(p.C==0) return null;
		double z=(p.D-p.B*os.realh)/p.C;
		if(z<0 || z>os.reald) return null;
		return new Vector3d(0,os.realh,z);
		}
	private static Vector3d intersectPlane4(Plane p, OneSlice os)
		{
		if(p.C==0) return null;
		double z=(p.D-p.A*os.realw-p.B*os.realh)/p.C;
		if(z<0 || z>os.reald) return null;
		return new Vector3d(os.realw,os.realh,z);
		}
	
	//Lines over y
	private static Vector3d intersectPlane5(Plane p, OneSlice os)
		{
		if(p.B==0) return null;
		double y=(p.D)/p.B;
		if(y<0 || y>os.realh) return null;
		return new Vector3d(0,y,0);
		}
	private static Vector3d intersectPlane6(Plane p, OneSlice os)
		{
		if(p.B==0) return null;
		double y=(p.D-p.A*os.realw)/p.B;
		if(y<0 || y>os.realh) return null;
		return new Vector3d(os.realw,y,0);
		}
	private static Vector3d intersectPlane7(Plane p, OneSlice os)
		{
		if(p.B==0) return null;
		double y=(p.D-p.C*os.reald)/p.B;
		if(y<0 || y>os.realh) return null;
		return new Vector3d(0,y,os.reald);
		}
	private static Vector3d intersectPlane8(Plane p, OneSlice os)
		{
		if(p.B==0) return null;
		double y=(p.D-p.A*os.realw-p.C*os.reald)/p.B;
		if(y<0 || y>os.realh) return null;
		return new Vector3d(os.realw,y,os.reald);
		}

	//Lines over z
	private static Vector3d intersectPlane9(Plane p, OneSlice os)
		{
		if(p.A==0) return null;
		double x=(p.D)/p.A;
		if(x<0 || x>os.realw) return null;
		return new Vector3d(x, 0,0);
		}
	private static Vector3d intersectPlane10(Plane p, OneSlice os)
		{
		if(p.A==0) return null;
		double x=(p.D-p.C*os.reald)/p.A;
		if(x<0 || x>os.realw) return null;
		return new Vector3d(x, 0,os.reald);
		}
	private static Vector3d intersectPlane11(Plane p, OneSlice os)
		{
		if(p.A==0) return null;
		double x=(p.D-p.B*os.realh)/p.A;
		if(x<0 || x>os.realw) return null;
		return new Vector3d(x, os.realh,0);
		}
	private static Vector3d intersectPlane12(Plane p, OneSlice os)
		{
		if(p.A==0) return null;
		double x=(p.D-p.B*os.realh-p.C*os.reald)/p.A;
		if(x<0 || x>os.realw) return null;
		return new Vector3d(x, os.realh,os.reald);
		}
	
	

	private Vector3d[] compactPoint(Vector3d[] p, int numv)
		{
		int i=0;
		Vector3d[] np=new Vector3d[numv];
		for(Vector3d v:p)
			if(v!=null)
				{
				np[i]=v;
				i++;
				}
		return np;
		}
	

	/**
	 * Used to find special cases of cube cuttings
	 */
	private static class UnclassifiedPoint implements Comparable<UnclassifiedPoint>
		{
		int id;
		//Vector2d p;
		double angle;
		public int compareTo(UnclassifiedPoint o)
			{
			if(angle<o.angle)				return -1;
			else if(angle>o.angle)	return 1;
			else										return 0;
			}
		public String toString(){return ""+id;}
		}
	
	private void renderPlane(GL gl, Camera cam, OneSlice os)
		{
		
		//Figure out how large a polygon would have to be to cut the cube for sure
		double neededSize=os.realw;
		if(os.realh>neededSize)
			neededSize=os.realh;
		if(os.reald>neededSize)
			neededSize=os.reald;
		neededSize*=20; //Arbitrary value, just be sure it is big

//		for(float i=0;i<1.0;i+=0.005)
		//float i=0.2f;
		for(double q=0;q<os.realh*4;q+=os.realh/20)
			{

//			Plane p=new Plane(1,0,0,os.realw);
//			Plane p=new Plane(0,1,0,os.realh);
//			Plane p=new Plane(0,0,1,os.reald);
			Plane p=new Plane(1,1,1,q);
			
			Vector3d[] points=new Vector3d[]{
					intersectPlane1(p, os), intersectPlane2(p, os), intersectPlane4(p, os), intersectPlane3(p, os),
					intersectPlane5(p, os), intersectPlane6(p, os), intersectPlane8(p, os), intersectPlane7(p, os),
					intersectPlane9(p, os), intersectPlane10(p, os), intersectPlane12(p, os), intersectPlane11(p, os)};
			int activelist=0;
			int numv=0;
			for(int ai=0;ai<12;ai++)
				if(points[ai]!=null)
					{
					activelist+=1<<ai;
					numv++;
					}
			
			switch(activelist)
				{
				/*
				case 3168: //110001100000  ok
				case 3172: //110001100100  ok
				case 1092:
					points=new Vector3d[]{}; //temp
					break;
				
				case 3002: // 8 points!
					break;
					
				case 912: //fel
				case 2730: //fel
					points=new Vector3d[]{}; break; //temp
				
				case -1:
				
					points=invertOrder(compactPoint(points, numv));
					break;
				case 3002: //8 wtf
					break;
				*/	
					
				default:
					//Non-considered case. Generate what the code should look like
					if(numv>=3)
						{
						//Find a center position not overlapping
						Vector3d center=new Vector3d();
						for(Vector3d v:compactPoint(points, numv))
							center.add(v);
						center.scale(1.0/numv);

						//Get normal
						Vector3d normal=new Vector3d(p.A,p.B,p.C);
						normal.normalize();
						
						//Project points
						List<UnclassifiedPoint> ups=new ArrayList<UnclassifiedPoint>();
						for(int ap=0;ap<points.length;ap++)
							if(points[ap]!=null)
								{
								UnclassifiedPoint up=new UnclassifiedPoint();
								up.id=ap;
								Vector3d v=new Vector3d(points[ap]);
								v.sub(center);
								//Actually realize this step is useless except for centering. remove?
								Vector3d w=new Vector3d(normal);
								w.scale(v.dot(normal));
								v.sub(w);
								up.angle=Math.atan2(v.y, v.x); //This *will* explode in some instances. and if could solve
								//ups.put(up.angle,up);
								ups.add(up);
								}
						Collections.sort(ups);
						
						//Make final list: Clean up duplicates.
						List<Vector3d> newpoints=new LinkedList<Vector3d>();
						List<Integer> newindex=new LinkedList<Integer>();
						for(UnclassifiedPoint up:ups)
							if(newpoints.size()==0 || !points[up.id].equals(newpoints.get(newpoints.size()-1)))
								{
								newindex.add(up.id);
								newpoints.add(points[up.id]);
								}
						points=new Vector3d[newpoints.size()];
						int ap=0;
						for(Vector3d v:newpoints)
							points[ap++]=v;
						//System.out.println(ups);
						System.out.println(""+activelist+": "+newindex);
						}
					else
						{
						points=new Vector3d[]{};
						}
				
				}
			
			
//			gl.glBegin(GL.GL_LINE_LOOP);
		gl.glBegin(GL.GL_POLYGON);
		gl.glColor3d(os.color.getRed()/255.0, os.color.getGreen()/255.0, os.color.getBlue()/255.0);
			for(Vector3d po:points)
				if(po!=null)
					{
//					System.out.println(po);
					point(gl, os, po.x, po.y, po.z);
					}
			
			/*
			point(gl,os,0,0,posz);
			point(gl,os,neededSize,0,posz);
			point(gl,os,neededSize,neededSize,posz);
			*/
/*			point(gl,os,0,0,posz);
			point(gl,os,os.realw,0,posz);
			point(gl,os,os.realw,os.realh,posz);*/
			gl.glEnd();
			}
		}
	
	private void point(GL gl, OneSlice os, double x, double y, double z) //spatial coordinates
		{
		gl.glTexCoord3f((float)x/(float)os.realw, (float)y/(float)os.realh, (float)z/(float)os.reald); gl.glVertex3d(x,y,z);
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
