package endrov.modelWindowImset.voxel;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;
import javax.vecmath.Vector3d;

import com.sun.opengl.util.j2d.TextureRenderer;
import com.sun.opengl.util.texture.*;

import endrov.imageset.*;
import endrov.modelWindow.Camera;
import endrov.modelWindow.ModelWindow;
import endrov.modelWindow.Shader;
import endrov.modelWindow.TransparentRender;
import endrov.util.EvDecimal;
import endrov.util.Tuple;
import endrov.modelWindow.ModelWindow.ProgressMeter;

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
public class Stack2D extends StackInterface
	{	
	private TreeMap<Double,Vector<OneSlice>> texSlices=null; //Z->slices for one plane
	private final int skipForward=1; //later maybe allow this to change
	private boolean needLoadGL=false;
	
	
	private static class OneSlice
		{
		int w, h;
		double z;
		double resX,resY;
		TextureRenderer rend;
		Texture tex;
		Color color;
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
					{
					os.tex.dispose();
					if(os.rend!=null)
						os.rend.dispose();
					}
		if(shader2d!=null)
			shader2d.delete(gl);
		shader2d=null;
		texSlices=null;
		}
	
	/*
	public void setLastFrame(EvDecimal frame)
		{
		lastframe=frame;
		}

	
	public boolean needSettings(EvDecimal frame)
		{
		return lastframe==null || !frame.equals(lastframe);// || !isBuilt();
		}
	
	*/
	
	public boolean newCreate(ProgressMeter pm, EvDecimal frame, HashMap<EvChannel, VoxelExtension.ChannelSelection> chsel2, ModelWindow w)
		{
		//im cache safety issues
		Collection<VoxelExtension.ChannelSelection> channels=chsel2.values();
		procList.clear();
		int curchannum=0;
		for(VoxelExtension.ChannelSelection chsel:channels)
			{
			EvDecimal cframe=chsel.ch.closestFrame(frame);
			//Common resolution for all channels
			//resZ=chsel.im.meta.resZ;

			//For every Z
			EvStack stack=chsel.ch.imageLoader.get(cframe);
			int skipcount=0;
			if(stack!=null)
				for(EvDecimal i:stack.keySet())
					{
					if(stopBuildThread)
						return false;
					skipcount++;
					if(skipcount>=skipForward)
						{
						final int progressSlices=i.multiply(1000).intValue()/(channels.size()*stack.size());
						final int progressChan=1000*curchannum/channels.size();
						pm.set(progressSlices+progressChan);

						skipcount=0;
						EvImage evim=stack.get(i);
						if(!chsel.filterSeq.isIdentity())
							evim=chsel.filterSeq.applyReturnImage(evim);
						Tuple<TextureRenderer,OneSlice> proc=processImage(evim, i, chsel);
						procList.add(proc);
						}
					}
			curchannum++;
			}

		needLoadGL=true;
		return true;
		}
	
	

	public Tuple<TextureRenderer,OneSlice> processImage(EvImage evim, EvDecimal z, VoxelExtension.ChannelSelection chsel)
		{
		BufferedImage bim=evim.getJavaImage(); //1-2 sec tot?
		OneSlice os=new OneSlice();
		
		os.w=bim.getWidth();
		os.h=bim.getHeight();
		os.resX=evim.getResX()/evim.getBinning(); //px/um
		os.resY=evim.getResY()/evim.getBinning();
		os.z=z/*.divide(resZ)*/.doubleValue();
		os.color=chsel.color;

		int bw=suitablePower2(os.w);
		os.resX/=os.w/(double)bw;
		os.w=bw;
		int bh=suitablePower2(os.h);
		os.resY/=os.h/(double)bh;
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
		texSlices=new TreeMap<Double,Vector<OneSlice>>();
		for(Tuple<TextureRenderer,OneSlice> proc:procList)
			{
			OneSlice os=proc.snd();
			os.tex=proc.fst().getTexture();
			
			Vector<OneSlice> texSlicesV=getTexSlicesFrame(os.z);
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
	public void render(GL gl,List<TransparentRender> transparentRenderers, Camera cam, final boolean solidColor, final boolean drawEdges, final boolean mixColors)
		{
		if(isBuilt())
			{
			//Load shader
			//if(shader2d==null)
//				shader2d=new Shader(gl,Stack3D.class.getResource("2dvert.glsl"),Stack3D.class.getResource("2dfrag.glsl"));

			//Draw edges
			if(drawEdges && !texSlices.isEmpty())
				{
				OneSlice os=texSlices.get(texSlices.lastKey()).lastElement();
				double w=os.w/os.resX;
				double h=os.h/os.resY;
				double d=os.z;
				renderEdge(gl, w, h, d);
				}

			
			TransparentRender.RenderState renderstate=new TransparentRender.RenderState(){
			public void activate(GL gl)
				{
				gl.glDisable(GL.GL_CULL_FACE);
				if(!solidColor)
					{
					if(mixColors)
						gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
					else
						gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_ONE_MINUS_SRC_COLOR);
					gl.glEnable(GL.GL_BLEND);
					gl.glDepthMask(false);
					//shader2d.use(gl); //currently not needed
					}
				}
			public boolean optimizedSwitch(GL gl, TransparentRender.RenderState currentState){return false;}
			public void deactivate(GL gl)
				{
				gl.glDisable(GL.GL_BLEND);
				gl.glDepthMask(true);
				gl.glEnable(GL.GL_CULL_FACE);
				//shader2d.stopUse(gl);
				}
			}; 

		
			
			
			//Sort planes, O(n) since pre-ordered
			SortedMap<Double,Vector<OneSlice>> frontMap=texSlices.headMap(cam.pos.z);
			SortedMap<Double,Vector<OneSlice>> backMap=texSlices.tailMap(cam.pos.z);
			LinkedList<Vector<OneSlice>> frontList=new LinkedList<Vector<OneSlice>>();
			LinkedList<Vector<OneSlice>> backList=new LinkedList<Vector<OneSlice>>();
			frontList.addAll(frontMap.values());
			for(Vector<OneSlice> os:backMap.values())
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
	private Shader shader2d=null;
	
	
	/**
	 * Render list of slices
	 */
	public void render(GL gl,List<TransparentRender> transparentRenderers, Camera cam, TransparentRender.RenderState renderstate, LinkedList<Vector<OneSlice>> list)
		{
		//Get direction of camera as vector, and z-position
		Vector3d camv=cam.transformedVector(0, 0, 1);
		double camz=cam.pos.dot(camv);
		
		//For all stacks
		for(Vector<OneSlice> osv:list)
			{
			//For all planes
			for(final OneSlice os:osv)
				{
				final double w=os.w/os.resX;
				final double h=os.h/os.resY;
				
				TransparentRender renderer=new TransparentRender(){public void render(GL gl)
					{
					//Select texture
					os.tex.enable();
					os.tex.bind();

					//Find size and position
					TextureCoords tc=os.tex.getImageTexCoords();

					gl.glBegin(GL.GL_QUADS);
					gl.glColor3d(os.color.getRed()/255.0, os.color.getGreen()/255.0, os.color.getBlue()/255.0);
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
	public Double autoCenterRadius(Vector3d mid, double FOV)
		{
		if(texSlices!=null && !texSlices.isEmpty())
			{
			OneSlice os=texSlices.get(texSlices.firstKey()).lastElement();
			double width=os.w/os.resX;
			double height=os.h/os.resY;
			double depth=os.z;
			
			double dx=Math.max(Math.abs(0-mid.x), Math.abs(width-mid.x));
			double dy=Math.max(Math.abs(0-mid.y), Math.abs(height-mid.y));
			double dz=Math.max(Math.abs(0-mid.z), Math.abs(depth-mid.z));
			double d=Math.sqrt(dx*dx+dy*dy+dz*dz);
			
			//Find how far away the camera has to be. really have FOV in here?
			return d/Math.sin(FOV);
			}
		else
			return null;
		}
	
	
	
	
	
	
	
	}
