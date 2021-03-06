/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer3Dimset.voxel;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;
import javax.vecmath.Vector3d;

import endrov.gl.EvGLCamera;
import endrov.gl.EvGLShader;
import endrov.gl.EvGLTexture3D;
import endrov.typeImageset.*;
import endrov.util.ProgressHandle;
import endrov.util.collection.Tuple;
import endrov.util.math.EvDecimal;
import endrov.windowViewer3D.BoundingBox3D;
import endrov.windowViewer3D.Viewer3DView;
import endrov.windowViewer3D.Viewer3DWindow;
import endrov.windowViewer3D.TransparentRenderer3D;
import endrov.windowViewer3D.Viewer3DWindow.ProgressMeter;


//XXX: NPOT mode?


/**
 * Render stack using 3d texture
 * @author Johan Henriksson
 */
public class Stack3D extends StackRendererInterface
	{	
	private Vector<VoxelStack> texSlices=new Vector<VoxelStack>();
	private final int skipForward=1; //later maybe allow this to change
	
	
	
	/**
	 * One voxel stack
	 */
	private static class VoxelStack
		{
		public int texW, texH, texD; //Texture size
		//need starting position
		public EvGLTexture3D tex; //could be multiple textures, interleaved
		public double realw, realh, reald; //size [um]
		
		public boolean needLoadGL=false;
		public StackRendererInterface.ChanProp prop;
		}
	
	/**
	 * Dispose all stacks. Need GL context, forced by parameter.
	 */
	public void clean(GL gl)
		{
		for(VoxelStack os:texSlices)
			os.tex.dispose(gl);
		texSlices.clear();
		if(shader3d!=null)
			shader3d.delete(gl);
		shader3d=null;
		}
	
	
	public boolean newCreate(ProgressHandle progh, ProgressMeter pm, EvDecimal frame, List<StackRendererInterface.ChannelSelection> chsel2, Viewer3DWindow w)
		{
		//im. cache safety issues
		Collection<StackRendererInterface.ChannelSelection> channels=chsel2;
		procList.clear();
		int curchannum=0;
		for(StackRendererInterface.ChannelSelection chsel:channels)
			{
			//For every Z
			EvDecimal cframe=chsel.ch.closestFrame(frame);
			EvStack stack=chsel.ch.getStack(cframe);
			
			
			int skipcount=0;
			if(stack!=null)
				{
				VoxelStack os=new VoxelStack();
				os.texW=stack.getWidth();
				os.texH=stack.getHeight();
				os.texD=stack.getDepth();//ceilPower2(stack.getDepth());

				os.prop=chsel.prop;
				
				EvGLTexture3D texture=os.tex=EvGLTexture3D.allocate(os.texW, os.texH, os.texD, w.view);

				//Size of the stack
				//TODO support rotated stacks
				os.realw=stack.getRes().x*stack.getWidth();
				os.realh=stack.getRes().y*stack.getHeight();
				os.reald=stack.getRes().z*stack.getDepth();
				
				System.out.println("stack size "+os.realw+" "+os.realh+" "+os.reald);
				System.out.println("stack res "+stack.getRes().x+" "+stack.getRes().y+" "+stack.getRes().z);
				
				for(int az=0;az<stack.getDepth();az++)
					{
					if(stopBuildThread) //Allow to just stop thread if needed
						return false;
					skipcount++;
					if(skipcount>=skipForward)
						{
						skipcount=0;
						int progressSlices=az*1000/(channels.size()*stack.getDepth());
						int progressChan=1000*curchannum/channels.size();
						pm.set(progressSlices+progressChan);
						
						//Get image for this plane
						EvImagePlane evim=stack.getPlane(az);
						EvPixels p=evim.getPixels(progh);
						BufferedImage bim=p.quickReadOnlyAWT();   //TODO this is BAD; handle more types

						//Load bitmap, scale down
						BufferedImage sim=new BufferedImage(os.texW,os.texH,BufferedImage.TYPE_BYTE_GRAY); 
						//BufferedImage sim=new BufferedImage(os.w,os.h,bim.getType()); //change type to byte or something? float better internally?
						Graphics2D g=(Graphics2D)sim.getGraphics();
						g.scale(os.texW/(double)bim.getWidth(), os.texH/(double)bim.getHeight()); //0.5 sec tot maybe
						g.drawImage(bim,0,0,Color.BLACK,null);

						//Convert to something suitable for texture upload?
						//DataBufferByte buf=(DataBufferByte)sim.getRaster().getDataBuffer();
						//texture.b.put(buf.getData());

						//theoretically slower but can be made safer
						Raster ras=sim.getRaster();
						for(int ay=0;ay<os.texH;ay++)
							for(int ax=0;ax<os.texW;ax++)
								{
								int pix[]=new int[3];
								ras.getPixel(ax, ay, pix);
								texture.b.put((byte)pix[0]);
								}

						}
					}
				
				
				//TODO This is a crap way of handling it!
				//Add black frames up to z power of 2
				int pixToWrite=os.texW*os.texH*os.texD-texture.b.position();
				texture.b.put(new byte[pixToWrite], 0, 0);


				texSlices.add(os);
				os.needLoadGL=true;
				}


			curchannum++;
			}

		return true;
		}







	
	
	
	
	
	/**
	 * Return suitable scale of model for navigation purposes
	 */
	public Collection<BoundingBox3D> adjustScale(Viewer3DWindow w)
		{
		if(!texSlices.isEmpty())
			{
			VoxelStack os=texSlices.get(0);
			return Collections.singleton(new BoundingBox3D(
					0.0, os.realw,
					0.0, os.realh,
					0.0, os.reald
			));
			}
		else
			return Collections.emptySet();
		}
	
	
	/**
	 * Give suitable center of all objects
	 */
	public Collection<Vector3d> autoCenterMid()
		{
		if(!texSlices.isEmpty())
			{
			VoxelStack os=texSlices.get(0);
			return Collections.singleton(new Vector3d(os.realw/2.0,os.realh/2.0,os.reald/2.0));
			}
		else
			return Collections.emptySet();
		}
	
	
	/**
	 * Given a middle position, figure out radius required to fit objects
	 */
	public double autoCenterRadius(Vector3d mid)
		{
		if(!texSlices.isEmpty())
			{
			VoxelStack os=texSlices.get(0);
			double dx=Math.max(Math.abs(0-mid.x), Math.abs(os.realw-mid.x));
			double dy=Math.max(Math.abs(0-mid.y), Math.abs(os.realh-mid.y));
			double dz=Math.max(Math.abs(0-mid.z), Math.abs(os.reald-mid.z));
			double d=Math.sqrt(dx*dx+dy*dy+dz*dz);
			return d;
			}
		else
			return 0;
		}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Plane rendering ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////

	/** List of new special cases */
	private TreeMap<Integer, String> newcases=new TreeMap<Integer, String>();
	
	/**
	 * Internal class representing a plane using affine form: Ax+By+Cz=D 
	 */
	private static class Plane
		{
		public Plane(double A, double B, double C, double D){this.A=A;this.B=B;this.C=C;this.D=D;}
		public double A, B, C, D; 
		}

	//Lines over z
	private static Vector3d intersectPlane1(Plane p, VoxelStack os)
		{
		if(p.C==0) return null;
		double z=p.D/p.C;
		if(z<0 || z>os.reald) return null;
		return new Vector3d(0,0,z);
		}
	private static Vector3d intersectPlane2(Plane p, VoxelStack os)
		{
		if(p.C==0) return null;
		double z=(p.D-p.A*os.realw)/p.C;
		if(z<0 || z>os.reald) return null;
		return new Vector3d(os.realw,0,z);
		}
	private static Vector3d intersectPlane3(Plane p, VoxelStack os)
		{
		if(p.C==0) return null;
		double z=(p.D-p.B*os.realh)/p.C;
		if(z<0 || z>os.reald) return null;
		return new Vector3d(0,os.realh,z);
		}
	private static Vector3d intersectPlane4(Plane p, VoxelStack os)
		{
		if(p.C==0) return null;
		double z=(p.D-p.A*os.realw-p.B*os.realh)/p.C;
		if(z<0 || z>os.reald) return null;
		return new Vector3d(os.realw,os.realh,z);
		}
	
	//Lines over y
	private static Vector3d intersectPlane5(Plane p, VoxelStack os)
		{
		if(p.B==0) return null;
		double y=(p.D)/p.B;
		if(y<0 || y>os.realh) return null;
		return new Vector3d(0,y,0);
		}
	private static Vector3d intersectPlane6(Plane p, VoxelStack os)
		{
		if(p.B==0) return null;
		double y=(p.D-p.A*os.realw)/p.B;
		if(y<0 || y>os.realh) return null;
		return new Vector3d(os.realw,y,0);
		}
	private static Vector3d intersectPlane7(Plane p, VoxelStack os)
		{
		if(p.B==0) return null;
		double y=(p.D-p.C*os.reald)/p.B;
		if(y<0 || y>os.realh) return null;
		return new Vector3d(0,y,os.reald);
		}
	private static Vector3d intersectPlane8(Plane p, VoxelStack os)
		{
		if(p.B==0) return null;
		double y=(p.D-p.A*os.realw-p.C*os.reald)/p.B;
		if(y<0 || y>os.realh) return null;
		return new Vector3d(os.realw,y,os.reald);
		}

	//Lines over z
	private static Vector3d intersectPlane9(Plane p, VoxelStack os)
		{
		if(p.A==0) return null;
		double x=(p.D)/p.A;
		if(x<0 || x>os.realw) return null;
		return new Vector3d(x, 0,0);
		}
	private static Vector3d intersectPlane10(Plane p, VoxelStack os)
		{
		if(p.A==0) return null;
		double x=(p.D-p.C*os.reald)/p.A;
		if(x<0 || x>os.realw) return null;
		return new Vector3d(x, 0,os.reald);
		}
	private static Vector3d intersectPlane11(Plane p, VoxelStack os)
		{
		if(p.A==0) return null;
		double x=(p.D-p.B*os.realh)/p.A;
		if(x<0 || x>os.realw) return null;
		return new Vector3d(x, os.realh,0);
		}
	private static Vector3d intersectPlane12(Plane p, VoxelStack os)
		{
		if(p.A==0) return null;
		double x=(p.D-p.B*os.realh-p.C*os.reald)/p.A;
		if(x<0 || x>os.realw) return null;
		return new Vector3d(x, os.realh,os.reald);
		}
	
	
	/**
	 * Keep only non-null vectors
	 */
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
	 * Internal class used to find special cases of cube cuts
	 */
	private static class UnclassifiedPoint implements Comparable<UnclassifiedPoint>
		{
		public int id;
		public double angle;
		public int compareTo(UnclassifiedPoint o)
			{
			if(angle<o.angle)				return -1;
			else if(angle>o.angle)	return 1;
			else										return 0;
			}
		public String toString(){return ""+id;}
		}

	/**
	 * Draw point. Spatial coordinates given. Generates texture coordinates
	 */
	private void point(GL2 gl, VoxelStack os, double x, double y, double z) 
		{
		gl.glTexCoord3f((float)x/(float)os.realw, (float)y/(float)os.realh, (float)z/(float)os.reald); gl.glVertex3d(x,y,z);
		}

	
	/**
	 * Render the place through one voxel stack given plane
	 */
	private void renderPlane(GL2 gl, EvGLCamera cam, VoxelStack os, Plane p)
		{
		//color
		Color tempColor=os.prop.color;
		
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
			//Auto-generated special cases. Can be removed, the default will handle everything.
			//22ms average time -> 0 to 2 average time
			case 0: points=new Vector3d[]{}; break;
			case 2: points=new Vector3d[]{}; break;
			case 4: points=new Vector3d[]{}; break;
			case 8: points=new Vector3d[]{}; break;
			case 15: points=new Vector3d[]{points[0],points[1],points[2],points[3]}; break;
			case 32: points=new Vector3d[]{}; break;
			case 34: points=new Vector3d[]{}; break;
			case 36: points=new Vector3d[]{}; break;
			case 51: points=new Vector3d[]{points[4],points[0],points[1],points[5]}; break;
			case 60: points=new Vector3d[]{points[5],points[2],points[3],points[4]}; break;
			case 64: points=new Vector3d[]{}; break;
			case 66: points=new Vector3d[]{}; break;
			case 68: points=new Vector3d[]{}; break;
			case 128: points=new Vector3d[]{}; break;
			case 136: points=new Vector3d[]{}; break;
			case 195: points=new Vector3d[]{points[0],points[1],points[6],points[7]}; break;
			case 204: points=new Vector3d[]{points[7],points[6],points[2],points[3]}; break;
			case 240: points=new Vector3d[]{points[4],points[7],points[6],points[5]}; break;
			case 273: points=new Vector3d[]{points[0],points[8],points[4]}; break;
			case 286: points=new Vector3d[]{points[4],points[8],points[1],points[2],points[3]}; break;
			case 290: points=new Vector3d[]{points[8],points[1],points[5]}; break;
			case 301: points=new Vector3d[]{points[0],points[8],points[5],points[2],points[3]}; break;
			case 466: points=new Vector3d[]{points[8],points[1],points[6],points[7],points[4]}; break;
			case 481: points=new Vector3d[]{points[7],points[0],points[8],points[5],points[6]}; break;
			case 512: points=new Vector3d[]{}; break;
			case 514: points=new Vector3d[]{}; break;
			case 576: points=new Vector3d[]{}; break;
			case 578: points=new Vector3d[]{points[9],points[1],points[6]}; break;
			case 589: points=new Vector3d[]{points[0],points[9],points[6],points[2],points[3]}; break;
			case 625: points=new Vector3d[]{points[4],points[0],points[9],points[6],points[5]}; break;
			case 640: points=new Vector3d[]{}; break;
			case 641: points=new Vector3d[]{points[0],points[9],points[7]}; break;
			case 654: points=new Vector3d[]{points[7],points[9],points[1],points[2],points[3]}; break;
			case 690: points=new Vector3d[]{points[9],points[1],points[5],points[4],points[7]}; break;
			case 864: points=new Vector3d[]{points[8],points[9],points[6],points[5]}; break;
			case 912: points=new Vector3d[]{points[8],points[9],points[7],points[4]}; break;
			case 1024: points=new Vector3d[]{}; break;
			case 1028: points=new Vector3d[]{}; break;
			case 1032: points=new Vector3d[]{}; break;
			case 1088: points=new Vector3d[]{}; break;
			case 1092: points=new Vector3d[]{points[6],points[2],points[10]}; break;
			case 1099: points=new Vector3d[]{points[0],points[1],points[6],points[10],points[3]}; break;
			case 1144: points=new Vector3d[]{points[5],points[6],points[10],points[3],points[4]}; break;
			case 1152: points=new Vector3d[]{}; break;
			case 1159: points=new Vector3d[]{points[7],points[0],points[1],points[2],points[10]}; break;
			case 1160: points=new Vector3d[]{points[10],points[3],points[7]}; break;
			case 1204: points=new Vector3d[]{points[7],points[4],points[5],points[2],points[10]}; break;
			case 1370: points=new Vector3d[]{points[4],points[8],points[1],points[6],points[10],points[3]}; break;
			case 1445: points=new Vector3d[]{points[7],points[0],points[8],points[5],points[2],points[10]}; break;
			case 1542: points=new Vector3d[]{points[9],points[1],points[2],points[10]}; break;
			case 1545: points=new Vector3d[]{points[0],points[9],points[10],points[3]}; break;
			case 1816: points=new Vector3d[]{points[8],points[9],points[10],points[3],points[4]}; break;
			case 1828: points=new Vector3d[]{points[9],points[8],points[5],points[2],points[10]}; break;
			case 2048: points=new Vector3d[]{}; break;
			case 2052: points=new Vector3d[]{}; break;
			case 2056: points=new Vector3d[]{}; break;
			case 2071: points=new Vector3d[]{points[4],points[0],points[1],points[2],points[11]}; break;
			case 2072: points=new Vector3d[]{points[4],points[11],points[3]}; break;
			case 2080: points=new Vector3d[]{}; break;
			case 2084: points=new Vector3d[]{}; break;
			case 2091: points=new Vector3d[]{points[0],points[1],points[5],points[11],points[3]}; break;
			case 2260: points=new Vector3d[]{points[4],points[7],points[6],points[2],points[11]}; break;
			case 2280: points=new Vector3d[]{points[6],points[5],points[11],points[3],points[7]}; break;
			case 2310: points=new Vector3d[]{points[8],points[1],points[2],points[11]}; break;
			case 2313: points=new Vector3d[]{points[0],points[8],points[11],points[3]}; break;
			case 2645: points=new Vector3d[]{points[4],points[0],points[9],points[6],points[2],points[11]}; break;
			case 2730: points=new Vector3d[]{points[7],points[9],points[1],points[5],points[11],points[3]}; break;
			case 2884: points=new Vector3d[]{points[8],points[9],points[6],points[2],points[11]}; break;
			case 2952: points=new Vector3d[]{points[9],points[8],points[11],points[3],points[7]}; break;
			case 3168: points=new Vector3d[]{points[5],points[6],points[10],points[11]}; break;
			case 3216: points=new Vector3d[]{points[4],points[7],points[10],points[11]}; break;
			case 3394: points=new Vector3d[]{points[8],points[1],points[6],points[10],points[11]}; break;
			case 3457: points=new Vector3d[]{points[0],points[8],points[11],points[10],points[7]}; break;
			case 3601: points=new Vector3d[]{points[4],points[0],points[9],points[10],points[11]}; break;
			case 3618: points=new Vector3d[]{points[9],points[1],points[5],points[11],points[10]}; break;
			case 3840: points=new Vector3d[]{points[8],points[9],points[10],points[11]}; break;


				
			default:
				//Non-considered case. Generate what the code should look like
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
						up.angle=Math.atan2(v.y, v.x); //This *will* explode in some instances. and an if could solve
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
				if(points.length>=3)
					{
					String out="case "+activelist+": points=new Vector3d[]{";
					boolean first=true;
					for(UnclassifiedPoint up:ups)
						{
						if(!first)
							out+=",";
						first=false;
						out+="points["+up.id+"]";
						}
					out+="}; break;";
					newcases.put(activelist,out);
					}
				else
					{
					points=new Vector3d[]{};
					String out="case "+activelist+": points=new Vector3d[]{}; break;";
					newcases.put(activelist,out);
					}
			}
	
		//Draw polygon
		gl.glBegin(GL2.GL_POLYGON);
		gl.glColor3d(tempColor.getRed()/255.0, tempColor.getGreen()/255.0, tempColor.getBlue()/255.0);
		for(Vector3d po:points)
			point(gl, os, po.x, po.y, po.z);
		gl.glEnd();
		}
	
	
	
	

	
	LinkedList<Tuple<BufferedImage,VoxelStack>> procList=new LinkedList<Tuple<BufferedImage,VoxelStack>>();
	public void loadGL(GL gl)
		{
//		cleanDisposable(gl);

		//Since upload can be called after the real upload call, just loop through everything
//		for(Vector<VoxelStack> osv:texSlices.values()) 
//			for(VoxelStack os:osv)
		for(VoxelStack os:texSlices)
				if(os.needLoadGL)
					{
					os.tex.prepare(gl);
					os.needLoadGL=false;
					}
		}
	


	
	/**
	 * Render entire stack
	 */
	public void render(GL glin,List<TransparentRenderer3D> transparentRenderers, EvGLCamera cam, boolean solidColor, boolean drawEdges, boolean mixColors, Viewer3DView view)
		{
		GL2 gl=glin.getGL2();

		Viewer3DView.checkerr(gl);

		//Draw edges
		if(drawEdges)
			for(VoxelStack os:texSlices)
					renderEdge(gl, os.realw, os.realh, os.reald);

		Viewer3DView.checkerr(gl);

		//Draw voxels
		for(VoxelStack os:texSlices)
			renderVoxelStack(gl, transparentRenderers, cam, os, solidColor, mixColors, view);
		}
	
	/**
	 * TODO move to voxext?
	 */
	private EvGLShader shader3d=null;
	
	
	public abstract class Stack3DRenderState implements TransparentRenderer3D.RenderState{}
	
	/**
	 * Render all planes through a voxel stack
	 */
	private void renderVoxelStack(GL2 gl,List<TransparentRenderer3D> transparentRenderers, final EvGLCamera cam, final VoxelStack os, final boolean solidColor, final boolean mixColors,
			Viewer3DView view)
		{
		Viewer3DView.checkerr(gl);

		//Load shader
		if(shader3d==null)
			shader3d=new EvGLShader(gl,Stack3D.class.getResource("3dvert.glsl"),Stack3D.class.getResource("3dfrag.glsl"), view);

		Viewer3DView.checkerr(gl);

		//Get direction of camera as vector, and z-position
		Vector3d camv=cam.rotateVector(0, 0, 1);
		double camz=cam.pos.dot(camv);
		
		//Find out settings
		final float contrast=(float)os.prop.contrast;
		final float brightness=(float)os.prop.brightness;
		
		Stack3DRenderState renderstate=new Stack3DRenderState(){
			public void activate(GL gl1)
				{
				GL2 gl=gl1.getGL2();

				Viewer3DView.checkerr(gl);

//			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
				if(!solidColor)
					{
					if(mixColors)
						gl.glBlendFunc(GL2.GL_SRC_COLOR, GL2.GL_ONE_MINUS_SRC_COLOR);
					else
						gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
					}
				gl.glDepthMask(false);
				gl.glDisable(GL2.GL_CULL_FACE);
				gl.glEnable(GL2.GL_TEXTURE_3D);   //Not needed with a vertex program!!!
				gl.glEnable(GL2.GL_BLEND);
					//int texUnit=0;  //NEW
					//gl.glActiveTexture(GL2.GL_TEXTURE0 + texUnit); //NEW
				os.tex.bind(gl);
				shader3d.prepareShader(gl);
				shader3d.use(gl);

				Viewer3DView.checkerr(gl);

				int posContrast=shader3d.getUniformLocation(gl, "contrast");
				int posBrightness=shader3d.getUniformLocation(gl, "brightness");
				gl.glUniform1f(posContrast, contrast);
				gl.glUniform1f(posBrightness, brightness);
				
				Viewer3DView.checkerr(gl);
				}
			public boolean optimizedSwitch(GL gl, TransparentRenderer3D.RenderState currentState)
				{
				if(currentState instanceof Stack3DRenderState)
					{
					os.tex.bind(gl);
					return true;
					}
				return false;
				}
			public void deactivate(GL gl)
				{
				shader3d.stopUse(gl);
				gl.glDisable(GL2.GL_TEXTURE_3D);
				gl.glDisable(GL2.GL_BLEND);
				gl.glDepthMask(true);
				gl.glEnable(GL2.GL_CULL_FACE);
			//	gl.glPopAttrib();
				}
		}; 
		
		//Figure out interval for q
		double boxCornerDistances[]=new double[]{
				new Vector3d(0,0,0).dot(camv),
				new Vector3d(os.realw,0,0).dot(camv),
				new Vector3d(os.realw,os.realh,0).dot(camv),
				new Vector3d(0,os.realh,0).dot(camv),
				new Vector3d(0,0,os.reald).dot(camv),
				new Vector3d(os.realw,0,os.reald).dot(camv),
				new Vector3d(os.realw,os.realh,os.reald).dot(camv),
				new Vector3d(0,os.realh,os.reald).dot(camv)
		};
		double minBoxCornerDistances=boxCornerDistances[0],	maxBoxCornerDistances=boxCornerDistances[0];
		for(double d:boxCornerDistances)
			{
			if(d<minBoxCornerDistances) minBoxCornerDistances=d;
			if(d>maxBoxCornerDistances) maxBoxCornerDistances=d;
			}
	
		//Figure out stepping
		double shortestSide=os.realw;
		if(shortestSide<os.realh)
			shortestSide=os.realh;
		if(shortestSide<os.reald)
			shortestSide=os.reald;
		double stepsize=shortestSide/200;

		boolean drawDirectly=false;

		Viewer3DView.checkerr(gl);

		//Generate all planes
		if(drawDirectly) renderstate.activate(gl);
		for(double q=minBoxCornerDistances;q<maxBoxCornerDistances;q+=stepsize)
			{
			final Plane p=new Plane(camv.x,camv.y,camv.z,q);
			if(drawDirectly)
				renderPlane(gl, cam, os, p);
			else
				{
				TransparentRenderer3D renderer=new TransparentRenderer3D(){public void render(GL glin)
					{
					GL2 gl=glin.getGL2();
					renderPlane(gl, cam, os, p);
					}};
				renderer.renderState=renderstate;
				renderer.z=q-camz;
				transparentRenderers.add(renderer);
				}
			}
		if(drawDirectly) renderstate.deactivate(gl);
		
		Viewer3DView.checkerr(gl);

		//Print new discovered cases
		for(String s:newcases.values())
			System.out.println(s);
		if(!newcases.isEmpty())
			System.out.println("# cases: "+newcases.size());
		
		Viewer3DView.checkerr(gl);

		}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Misc helpers //////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Round to best 2^
	 */
	/*
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
		}*/
	
	/**
	 * Ceil to nearest 2^
	 */
	public static int ceilPower2(int s)
		{
		if(s>1024)
			{
			double log2=Math.log(2);
			double x=Math.ceil(Math.log(s)/log2);
			int v=(int)Math.exp(Math.log(2)*x);
			System.out.println("ceilpow2 "+s+" => "+v);
			return v;
			}
		else if(s>512) return 1024;
		else if(s>256) return 512;
		else if(s>128) return 256;
		else if(s>64) return 128;
		else if(s>32) return 64;
		else if(s>16) return 32;
		else if(s>8) return 16;
		else if(s>4) return 8;
		else return 4;
		}
	
	}
