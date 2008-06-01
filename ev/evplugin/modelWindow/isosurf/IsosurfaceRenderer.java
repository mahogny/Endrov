package evplugin.modelWindow.isosurf;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.opengl.util.BufferUtil;

import evplugin.modelWindow.Camera;
import evplugin.modelWindow.ModelView;
import evplugin.modelWindow.ModelWindow;
import evplugin.modelWindow.TransparentRender;

//possible optimization: if there are several objects in need of VBO, these lists need to be merged.
//we add another class then, like Shader, VertexList, with automatic VBO<->VArray support.
//the final optimization is to NOT unbind the object until it has to be released to improve caching further.

//real optimization: reduce the number of polygons with some algorithm. 2x improvement right away.

//can replace float with short, half memory => twice basic speed
//*should* load images while rendering, skipping the ptscalarstep

//TODO: mode with no array buffers at all, when there is a need to switch back and forth all the time

//role of generator half moved here. just need the list management. or register callbacks?

/**
 * Renderer of isosurfaces
 * @author Johan Henriksson
 */
public class IsosurfaceRenderer
	{
	public FloatBuffer vertb;
	public FloatBuffer vertn;
	public IntBuffer indb;
	private int[] VBOVertices = new int[1];
	private int[] VBONormals = new int[1];
	public float maxX,maxY,maxZ,minX,minY,minZ;
	private IsosurfTranspRenderer[] transpList = null;
	

	boolean VBOsupported;
	boolean hasVBO=false;
	
	public void uploadData(ModelView view, FloatBuffer vertb, FloatBuffer vertn, IntBuffer indb)
		{
		this.vertb=vertb;
		this.vertn=vertn;
		this.indb=indb;
		VBOsupported=view.VBOsupported;
		VBOsupported=false;
		}
	
	
	private static void checkGLerr(GL gl, String pos)
		{
		int errcode=gl.glGetError();
		if(errcode!=GL.GL_NO_ERROR)
			System.out.println("error ("+pos+")"+new GLU().gluErrorString(errcode));
		}
	
	
	
	public Collection<Double> adjustScale(ModelWindow w)
		{
		return Collections.singleton((double)(maxX-minX));
		}
	
	/**
	 * Give suitable center of all objects
	 */
	public Vector3d autoCenterMid()
		{
		return new Vector3d((maxX+minX)/2,(maxY+minY)/2,(maxZ+minZ)/2);
		}
	
	
	/**
	 * Given a middle position, figure out radius required to fit objects
	 */
	public Double autoCenterRadius(Vector3d mid, double FOV)
		{
		double[] list={Math.abs(minX-mid.x),Math.abs(minY-mid.y),Math.abs(minZ-mid.z),
				Math.abs(maxX-mid.x), Math.abs(maxY-mid.y), Math.abs(maxZ-mid.z)};
		double max=list[0];
		for(double d:list)
			if(d>max)
				max=d;
		//Find how far away the camera has to be. really have FOV in here?
		return max/Math.sin(FOV);
		}
	
	
	public abstract class IsosurfRenderState implements TransparentRender.RenderState
		{
		public boolean isTransparent;
		}

	public abstract class IsosurfTranspRenderer extends TransparentRender
		{
		public Vector3d midpos;
		}

	/**
	 * Remove any allocated resources
	 */
	public void clean(GL gl)
		{
		if(hasVBO)
			{
			gl.glDeleteBuffers(1, VBOVertices, 0);
			gl.glDeleteBuffers(1, VBONormals, 0);
			}
		}
	
	
	/**
	 * Render surface
	 */
	public void render(GL gl,List<TransparentRender> transparentRenderers, Camera cam, final float red, final float green, final float blue, final float trans)
		{
		//Upload buffers if supported
		if(VBOsupported && !hasVBO)
			{
			vertb.rewind();
			vertn.rewind();
			int num=vertn.remaining();
			//Vertices
			gl.glGenBuffersARB(1, VBOVertices, 0);
			gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOVertices[0]);
			gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB,num * 
					BufferUtil.SIZEOF_FLOAT, vertb, GL.GL_STATIC_DRAW_ARB);
			//Normals
			gl.glGenBuffersARB(1, VBONormals, 0);
			gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBONormals[0]);
			gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, num * 
					BufferUtil.SIZEOF_FLOAT, vertn, GL.GL_STATIC_DRAW_ARB);
			//vertb=vertn=null; //has to wait. vertn needed for z sort
			hasVBO=true;
			
			checkGLerr(gl, "upload");
			}
		
		//Draw if data exists
		if(vertb!=null || hasVBO)
			{
			final boolean doTransparent=trans<0.99;
	
			IsosurfRenderState renderState=new IsosurfRenderState(){
				public void activate(GL gl)
					{
					gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
					
					float lightDiffuse[]=new float[]{1f, 1f, 1f, 0};
					float lightAmbient[] = { 0.3f, 0.3f, 0.3f, 0 }; 
					gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);   
					gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);
					//GL_LIGHT_MODEL_TWO_SIDE false right now

					if(doTransparent)
						enableBlend(gl);

					//Decide on vertex buffer objects or vertex arrays
					if(!doTransparent)
						{
						enableArray(gl);
						gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
						gl.glEnableClientState( GL.GL_NORMAL_ARRAY );
						}
						
					gl.glDisable(GL.GL_CULL_FACE);
					gl.glEnable(GL.GL_LIGHTING);
					gl.glEnable(GL.GL_LIGHT0);
					gl.glEnable ( GL.GL_COLOR_MATERIAL ) ;
					gl.glColor4f(red,green,blue,trans);
					}
				public void enableBlend(GL gl)
					{
					//NEHE does additive instead
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
					gl.glEnable(GL.GL_BLEND);
					gl.glDepthMask(false);
					gl.glDisable(GL.GL_CULL_FACE);
					gl.glColorMaterial ( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE ) ;
					gl.glEnable(GL.GL_COLOR_MATERIAL);
					}
				public void enableArray(GL gl)
					{
					if(hasVBO)
						{
						gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOVertices[0]);
						gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
						gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBONormals[0]);
						gl.glNormalPointer(GL.GL_FLOAT, 0, 0);
						checkGLerr(gl, "bind VBO");
						}
					else
						{
						vertb.rewind();
						vertn.rewind();
						gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertb);
						gl.glNormalPointer(GL.GL_FLOAT, 0, vertn);
						}
					}
				public boolean optimizedSwitch(GL gl, TransparentRender.RenderState currentState)
					{
					if(currentState instanceof IsosurfRenderState)
						{
						if(!doTransparent)
							enableArray(gl);
						gl.glColor4f(red,green,blue,trans);
						return true;
						}
					else
						return false;
					}
				public void deactivate(GL gl)
					{
					if(hasVBO && !doTransparent)
						{
						gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
						checkGLerr(gl, "unbind VBO");
						}
					gl.glPopAttrib();
					}
			};  
			renderState.isTransparent=doTransparent;
			
			if(doTransparent)
				{
				//If transparent, we need to queue these
				//For now, use one index. this will give glitches: need average coordinate
				indb.rewind();
				int numi=indb.remaining();

				//Get direction of camera as vector, and z-position
				Vector3d camv=cam.transformedVector(0, 0, 1);
				double camz=cam.pos.dot(camv);

				//Pregenerate data
				if(transpList==null)
					{
					transpList=new IsosurfTranspRenderer[numi/3];
					
					//Generate transparent surfaces
					for(int i=0;i<numi;i+=3)
						{
						//Pre-catch data
						final Vector3f[] vn=new Vector3f[3];
						final Vector3f[] vb=new Vector3f[3];
						for(int j=0;j<3;j++)
							{
							indb.position(i+j);
							int index=indb.get();
							vertb.position(index*3);
							vertn.position(index*3);
							vn[j]=new Vector3f(vertn.get(),vertn.get(),vertn.get());
							vb[j]=new Vector3f(vertb.get(),vertb.get(),vertb.get());
							}
						
						IsosurfTranspRenderer renderer=new IsosurfTranspRenderer(){
						public void render(GL gl)
							{
							gl.glBegin(GL.GL_TRIANGLES);
							for(int j=0;j<3;j++)
								{
								gl.glNormal3f(vn[j].x,vn[j].y,vn[j].z);
								gl.glVertex3f(vb[j].x,vb[j].y,vb[j].z);
								}
							gl.glEnd();
							}
						};
						indb.position(i);
						int index=indb.get();
						vertb.position(index*3);
						renderer.midpos=new Vector3d(vertb.get(),vertb.get(),vertb.get());
						transpList[i/3]=renderer;
						}
					}
				for(IsosurfTranspRenderer r:transpList)
					{
					r.z=r.midpos.dot(camv)-camz;
					r.renderState=renderState;
					transparentRenderers.add(r);
					}
				
				}
			else
				{
				//If opaque, we do not postpone rendering
				renderState.activate(gl);
				indb.rewind();
				gl.glDrawElements(GL.GL_TRIANGLES, indb.remaining(), GL.GL_UNSIGNED_INT, indb);
				renderState.deactivate(gl);
				}
			}
		

		}
	
	
	
	
	
	
	
	
	
	
	
	/*
	public void render(GL gl,List<TransparentRender> transparentRenderers, Camera cam, final float red, final float green, final float blue, final float trans)
		{
		//Upload buffers if supported
		if(VBOsupported && !hasVBO)
			{
			vertb.rewind();
			vertn.rewind();
			int num=vertn.remaining();
			//Vertices
			gl.glGenBuffersARB(1, VBOVertices, 0);
			gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOVertices[0]);
			gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB,num * 
					BufferUtil.SIZEOF_FLOAT, vertb, GL.GL_STATIC_DRAW_ARB);
			//Normals
			gl.glGenBuffersARB(1, VBONormals, 0);
			gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBONormals[0]);
			gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, num * 
					BufferUtil.SIZEOF_FLOAT, vertn, GL.GL_STATIC_DRAW_ARB);
			//vertb=vertn=null; //has to wait. vertn needed for z sort
			hasVBO=true;
			
			checkGLerr(gl, "upload");
			}
		
		//Draw if data exists
		if(vertb!=null || hasVBO)
			{
			final boolean doTransparent=trans<0.99;
	
			IsosurfRenderState renderState=new IsosurfRenderState(){
				public void activate(GL gl)
					{
					gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
					
					float lightDiffuse[]=new float[]{1f, 1f, 1f, 0};
					float lightAmbient[] = { 0.3f, 0.3f, 0.3f, 0 }; 
					gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);   
					gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);
					//GL_LIGHT_MODEL_TWO_SIDE false right now

					if(doTransparent)
						enableBlend(gl);

					//Decide on vertex buffer objects or vertex arrays
					enableArray(gl);
						
					gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
					gl.glEnableClientState( GL.GL_NORMAL_ARRAY );
					gl.glDisable(GL.GL_CULL_FACE);
					gl.glEnable(GL.GL_LIGHTING);
					gl.glEnable(GL.GL_LIGHT0);
					gl.glEnable ( GL.GL_COLOR_MATERIAL ) ;
					gl.glColor4f(red,green,blue,trans);
					}
				public void enableBlend(GL gl)
					{
					//NEHE does additive instead
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
					gl.glEnable(GL.GL_BLEND);
					gl.glDepthMask(false);
					gl.glDisable(GL.GL_CULL_FACE);
					gl.glColorMaterial ( GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE ) ;
					gl.glEnable(GL.GL_COLOR_MATERIAL);
					}
				public void enableArray(GL gl)
					{
					if(hasVBO)
						{
						gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOVertices[0]);
						gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
						gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBONormals[0]);
						gl.glNormalPointer(GL.GL_FLOAT, 0, 0);
						checkGLerr(gl, "bind VBO");
						}
					else
						{
						vertb.rewind();
						vertn.rewind();
						gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertb);
						gl.glNormalPointer(GL.GL_FLOAT, 0, vertn);
						}
					}
				public boolean optimizedSwitch(GL gl, TransparentRender.RenderState currentState)
					{
					if(currentState instanceof IsosurfRenderState)
						{
						IsosurfRenderState other=(IsosurfRenderState)currentState;
						if(other.isTransparent!=isTransparent)
							{
							if(isTransparent)
								enableBlend(gl);
							else
								gl.glDisable(GL.GL_BLEND);
							}
						enableArray(gl);
						return true;
						}
					else
						return false;
					}
				public void deactivate(GL gl)
					{
					if(hasVBO)
						{
						gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
						checkGLerr(gl, "unbind VBO");
						}
					gl.glPopAttrib();
					}
			};  
			renderState.isTransparent=doTransparent;
			
			if(doTransparent)
				{
				//If transparent, we need to queue these
				//For now, use one index. this will give glitches: need average coordinate
				indb.rewind();
				int numi=indb.remaining();

				//Get direction of camera as vector, and z-position
				Vector3d camv=cam.transformedVector(0, 0, 1);
				double camz=cam.pos.dot(camv);

				//Calculate mids once and for all
				if(mids==null)
					{
					mids=new Vector3d[numi/3];
					for(int i=0;i<numi;i+=3)
						{
						//TODO: optimize
						indb.position(i);
						int index=indb.get();
						vertb.position(index*3);
						Vector3d v=new Vector3d(vertb.get(),vertb.get(),vertb.get());
						mids[i/3]=v;
						}
					}
				//Generate transparent surfaces
				for(int i=0;i<numi;i+=3)
					{
					final int fi=i;
					TransparentRender renderer=new TransparentRender(){
					public void render(GL gl)
						{
						indb.rewind();
						indb.position(fi);
						gl.glDrawElements(GL.GL_TRIANGLES, 3, GL.GL_UNSIGNED_INT, indb);
						}
					};
					renderer.renderState=renderState;
//					renderer.z=0;
					renderer.z=mids[i/3].dot(camv)-camz;
					transparentRenderers.add(renderer);
					}
				}
			else
				{
				//If opaque, we do not postpone rendering
				renderState.activate(gl);
				indb.rewind();
				gl.glDrawElements(GL.GL_TRIANGLES, indb.remaining(), GL.GL_UNSIGNED_INT, indb);
				renderState.deactivate(gl);
				}
			}
		

		}
	*/
	
	
	
	
	
	
	
	
	}
