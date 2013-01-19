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

import com.sun.opengl.util.awt.TextureRenderer;
import com.sun.opengl.util.texture.*;

import endrov.gl.EvGLCamera;
import endrov.gl.EvGLShader;
import endrov.imageset.*;
import endrov.util.EvDecimal;
import endrov.util.ProgressHandle;
import endrov.util.Tuple;
import endrov.windowViewer3D.BoundingBox3D;
import endrov.windowViewer3D.Viewer3DView;
import endrov.windowViewer3D.Viewer3DWindow;
import endrov.windowViewer3D.TransparentRenderer3D;
import endrov.windowViewer3D.Viewer3DWindow.ProgressMeter;

//if one ever wish to build it in the background:
//GLContext glc=view.getContext();
//glc.makeCurrent(); 
//GL gl=glc.getGL();
// ... glc.release();

/**
 * Benchmark: to upload to card
 * 512x512 bufferedimage 2720ms 
 * 512x512 texturerenderer 740ms 
 */

/*
uploading texture in BG
http://lists.apple.com/archives/Mac-opengl/2007/Feb/msg00063.html
*/

/**
 * Render stack as several textured slices
 * @author Johan Henriksson
 */
public class Stack2D extends StackRendererInterface
	{	
	private TreeMap<Double,List<OneSlice>> texSlices=null; //Z->slices for one plane
	private final int skipForward=1; //later maybe allow this to change
	private boolean needLoadGL=false;
	
	
	private static class OneSlice
		{
		int w, h;
		double z;
		double resX,resY;
		TextureRenderer rend;
		Texture tex;
		StackRendererInterface.ChanProp prop;
//		Color color;
		}
	
	
	
	/**
	 * Get or create slices for one z. Has to be synchronized
	 */ 
	private synchronized List<OneSlice> getTexSlicesFrame(double z)
		{
		//Put it texture into list
		List<OneSlice> texSlicesV=texSlices.get(z);
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
			for(List<OneSlice> osv:texSlices.values())
				for(OneSlice os:osv)
					{
					os.tex.destroy(gl);
					if(os.rend!=null)
						os.rend.dispose();
					}
		if(shader2d!=null)
			shader2d.delete(gl);
		shader2d=null;
		texSlices=null;
		}
	
	
	public boolean newCreate(ProgressHandle progh, ProgressMeter pm, EvDecimal frame, List<StackRendererInterface.ChannelSelection> chsel2, Viewer3DWindow w)
		{
		//im cache safety issues
		procList.clear();
		int curchannum=0;
		for(StackRendererInterface.ChannelSelection chsel:chsel2)
			{
			EvDecimal cframe=chsel.ch.closestFrame(frame);

			//For every Z
			EvStack stack=chsel.ch.getStack(cframe);
			int skipcount=0;
			if(stack!=null)
				for(int az=0;az<stack.getDepth();az++)
					{
					if(stopBuildThread)
						return false;
					skipcount++;
					if(skipcount>=skipForward)
						{
						final int progressSlices=(az*1000/(chsel2.size()*stack.getDepth()));//az.multiply(1000).intValue()/(channels.size()*stack.getDepth());
						final int progressChan=1000*curchannum/chsel2.size();
						pm.set(progressSlices+progressChan);

						///// TODO better interaction with progress meter
						
						skipcount=0;
						EvImage evim=stack.getInt(az);
						Tuple<TextureRenderer,OneSlice> proc=processImage(progh, stack, evim, az, chsel);
						procList.add(proc);
						}
					}
			curchannum++;
			}

		needLoadGL=true;
		return true;
		}
	
	

	public Tuple<TextureRenderer,OneSlice> processImage(ProgressHandle progh, EvStack stack, EvImage evim, int az, StackRendererInterface.ChannelSelection chsel)
		{
		EvPixels p=evim.getPixels(progh);
		BufferedImage bim=p.quickReadOnlyAWT();
		OneSlice os=new OneSlice();
		
		os.w=p.getWidth();
		os.h=p.getHeight();
		os.resX=stack.resX;
		os.resY=stack.resY;
		os.z=stack.resZ*az;
		os.prop=chsel.prop;

		int bw=suitablePower2(os.w);
		os.resX*=os.w/(double)bw;
		os.w=bw;
		int bh=suitablePower2(os.h);
		os.resY*=os.h/(double)bh;
		os.h=bh;

		//Load bitmap, scale down
		TextureRenderer rend=TextureRenderer.createAlphaOnlyRenderer(os.w, os.h);
		Graphics2D g=rend.createGraphics();
		
		g.scale(os.w/(double)bim.getWidth(), os.h/(double)bim.getHeight()); //0.5 sec tot maybe
		g.drawImage(bim,0,0,Color.BLACK,null);
		
		return new Tuple<TextureRenderer, OneSlice>(rend,os);
		}
	
	
	public void addSlice(GL gl, List<Tuple<TextureRenderer,OneSlice>> procList)
		{
		clean(gl);
		texSlices=new TreeMap<Double,List<OneSlice>>();
		for(Tuple<TextureRenderer,OneSlice> proc:procList)
			{
			OneSlice os=proc.snd();
			os.tex=proc.fst().getTexture();
			
			List<OneSlice> texSlicesV=getTexSlicesFrame(os.z);
			texSlicesV.add(os);
			}
		}
	
	LinkedList<Tuple<TextureRenderer,OneSlice>> procList=new LinkedList<Tuple<TextureRenderer,OneSlice>>();
	public void loadGL(GL gl)
		{
		if(needLoadGL)
			{
			needLoadGL=false;
			System.out.println("uploading to GL");
			long t=System.currentTimeMillis();
			addSlice(gl,procList);
			System.out.println("voxels loading ok:"+(System.currentTimeMillis()-t));
			}
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
	public void render(GL glin,List<TransparentRenderer3D> transparentRenderers, EvGLCamera cam,
			final boolean solidColor, final boolean drawEdges, final boolean mixColors, Viewer3DView view)
		{
		GL2 gl=glin.getGL2();
		if(isBuilt())
			{
			//Load shader
			//if(shader2d==null)
//				shader2d=new Shader(gl,Stack3D.class.getResource("2dvert.glsl"),Stack3D.class.getResource("2dfrag.glsl"));

			//Draw edges
			if(drawEdges && !texSlices.isEmpty())
				{
				List<OneSlice> slices=texSlices.get(texSlices.lastKey());
				OneSlice os=slices.get(slices.size()-1);
				double w=os.w*os.resX;
				double h=os.h*os.resY;
				double d=os.z;
				renderEdge(gl, w, h, d);
				}

			
			TransparentRenderer3D.RenderState renderstate=new TransparentRenderer3D.RenderState(){
			public void activate(GL gl)
				{
				gl.glDisable(GL2.GL_CULL_FACE);
				if(!solidColor)
					{
					if(mixColors)
						gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
					else
						gl.glBlendFunc(GL2.GL_SRC_COLOR, GL2.GL_ONE_MINUS_SRC_COLOR);
					gl.glEnable(GL2.GL_BLEND);
					gl.glDepthMask(false);
					//prepare shader
					//shader2d.use(gl); //currently not needed
					}
				}
			public boolean optimizedSwitch(GL gl, TransparentRenderer3D.RenderState currentState){return false;}
			public void deactivate(GL gl)
				{
				gl.glDisable(GL2.GL_BLEND);
				gl.glDepthMask(true);
				gl.glEnable(GL2.GL_CULL_FACE);
				//shader2d.stopUse(gl);
				}
			}; 

		
			
			
			//Sort planes, O(n) since pre-ordered
			SortedMap<Double,List<OneSlice>> frontMap=texSlices.headMap(cam.pos.z);
			SortedMap<Double,List<OneSlice>> backMap=texSlices.tailMap(cam.pos.z);
			LinkedList<List<OneSlice>> frontList=new LinkedList<List<OneSlice>>();
			LinkedList<List<OneSlice>> backList=new LinkedList<List<OneSlice>>();
			frontList.addAll(frontMap.values());
			for(List<OneSlice> os:backMap.values())
				backList.addFirst(os);
			
			//renderstate.activate(gl);
			render(gl, transparentRenderers, cam, renderstate, frontList);
			render(gl, transparentRenderers, cam, renderstate, backList);
			//renderstate.deactivate(gl);
			
			}
		}

	
	private boolean isBuilt()
		{
		return texSlices!=null;
		}

	
	/**
	 * TODO move to voxext?
	 */
	private EvGLShader shader2d=null;
	
	
	/**
	 * Render list of slices
	 */
	public void render(GL glin,List<TransparentRenderer3D> transparentRenderers, EvGLCamera cam, TransparentRenderer3D.RenderState renderstate, LinkedList<List<OneSlice>> list)
		{
		//Get direction of camera as vector, and z-position
		Vector3d camv=cam.rotateVector(0, 0, 1);
		double camz=cam.pos.dot(camv);
		
		//For all stacks
		for(List<OneSlice> osv:list)
			{
			//For all planes
			for(final OneSlice os:osv)
				{
				final double w=os.w*os.resX;
				final double h=os.h*os.resY;
				
				TransparentRenderer3D renderer=new TransparentRenderer3D(){public void render(GL glin)
					{
					GL2 gl=glin.getGL2();
					//Select texture
					os.tex.enable();
					os.tex.bind();

					//Find size and position
					TextureCoords tc=os.tex.getImageTexCoords();

					Color col=os.prop.color;
					gl.glBegin(GL2.GL_QUADS);
					gl.glColor3d(col.getRed()/255.0, col.getGreen()/255.0, col.getBlue()/255.0);
					gl.glTexCoord2f(tc.left(), tc.top());	   gl.glVertex3d(0, 0, os.z);
					gl.glTexCoord2f(tc.right(),tc.top());    gl.glVertex3d(w, 0, os.z);
					gl.glTexCoord2f(tc.right(),tc.bottom()); gl.glVertex3d(w, h, os.z);
					gl.glTexCoord2f(tc.left(), tc.bottom()); gl.glVertex3d(0, h, os.z);
					gl.glEnd();

					os.tex.disable();
				}};
				renderer.renderState=renderstate;
				renderer.z=new Vector3d(w/2,h/2,os.z).dot(camv)-camz;
				transparentRenderers.add(renderer);
				
				}
			}
		}
	
	
	
	public Collection<BoundingBox3D> adjustScale(Viewer3DWindow w)
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			List<OneSlice> slices=texSlices.get(texSlices.lastKey());
			OneSlice os=slices.get(slices.size()-1);
			
			return Collections.singleton(
					new BoundingBox3D(
							0.0, os.w*os.resX,
							0.0, os.h*os.resY,
							0.0, os.z)
					);
			}
		else
			return Collections.emptySet();
		}
	
	
	/**
	 * Give suitable center of all objects
	 */
	public Collection<Vector3d> autoCenterMid()
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			OneSlice os=texSlices.get(texSlices.firstKey()).get(0);
			double width=os.w/os.resX;
			double height=os.h/os.resY;
			return Collections.singleton(new Vector3d(width/2.0,height/2.0,(texSlices.firstKey()+texSlices.lastKey())/2.0));
			}
		else
			return Collections.emptySet();
		}
	
	
	/**
	 * Given a middle position, figure out radius required to fit objects
	 */
	public double autoCenterRadius(Vector3d mid)
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			List<OneSlice> slices=texSlices.get(texSlices.firstKey());
			OneSlice os=slices.get(slices.size()-1);
			double width=os.w/os.resX;
			double height=os.h/os.resY;
			double depth=os.z;
			
			double dx=Math.max(Math.abs(0-mid.x), Math.abs(width-mid.x));
			double dy=Math.max(Math.abs(0-mid.y), Math.abs(height-mid.y));
			double dz=Math.max(Math.abs(0-mid.z), Math.abs(depth-mid.z));
			double d=Math.sqrt(dx*dx+dy*dy+dz*dz);
			return d;
			}
		else
			return 0;
		}
	
	
	
	
	
	
	
	}
