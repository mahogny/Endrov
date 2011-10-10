/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindow;

import java.util.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.nio.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.sun.opengl.util.awt.TextRenderer;

import endrov.coordinateSystem.CoordinateSystem;
import endrov.ev.*;
import endrov.modelWindow.TransparentRender.RenderState;
import endrov.modelWindow.gl.GLCamera;
import endrov.modelWindow.gl.GLMeshVBO;
import endrov.util.EvDecimal;


//NEED GLJPanel
//GLCanvas fast

//http://fivedots.coe.psu.ac.th/~ad/jg2/ch15/jogl1v4.pdf
//talks about -Dsun.java2d.opengl=true, uses gljpanel

/**
 * A panel for displaying the model
 */
public class ModelView extends GLJPanel //GLCanvas
	{
	public static final long serialVersionUID=0;
	
	//method: display(). faster than repaint.
	
  private static GLCapabilities caps;

  public boolean showSelectChannel=false;
  
	static
	{
	System.out.println("Default gl profile "+GLProfile.getDefault());
	GLProfile prof=GLProfile.get(GLProfile.GL2);
	
	caps = new GLCapabilities(prof);
  caps.setAlphaBits(8);
	}
	
	/** Number of clipping planes supported by this context */
	public int numClipPlanesSupported;
  //6 planes on NVidia macpro
	/** Maximum 3D texture size */
	public int max3DTextureSize;
	//2048 on GeForce 8400 GS/PCI/SSE2
	/** Number of texture units */
	public int numTextureUnits;
	//4 on GeForce 8400 GS/PCI/SSE2
	
	public boolean VBOsupported;
	public boolean shaderSupported;
	
	/** Common data */
	private ModelWindow window;

	/** Camera coordinates */
	public GLCamera camera=new GLCamera();
	private final double FOV=45.0/180.0*Math.PI;	
	/** Current frame */
	public EvDecimal frame=EvDecimal.ZERO;
	

	/** Scaling factor for panning */
	private double panspeed=1;

	/** Scale to which everything should adapt */
	//also panspeed?
	private double representativeScale=1;
	
		
	/** Current mouse coordinate */
	public int mouseX=-1, mouseY=-1;	
	public TextRenderer renderer;

	/** Render axis arrows in the corner */
	public boolean renderAxisArrows=true;
	
	public Color bgColor=Color.BLACK;
	
	private Map<Object,GLMeshVBO> meshs=new HashMap<Object, GLMeshVBO>();
	private Set<Object> keepMeshs=new HashSet<Object>();
	

	
	
	/**
	 * Get a cached mesh. If the mesh is not get:ed every rendering loop it will be removed
	 */
	public GLMeshVBO getMesh(Object o)
		{
		GLMeshVBO vbo=meshs.get(o);
		if(vbo!=null)
			keepMeshs.add(o);
		return vbo;
		}
	
	/**
	 * Cache mesh until later
	 */
	public void setMesh(Object o, GLMeshVBO m)
		{
		meshs.put(o, m);
		keepMeshs.add(o);
		}
	
	/**
	 * Unload meshs not used this rendering loop
	 */
	private void removeUnusedMesh(GL2 gl)
		{
		Map<Object, GLMeshVBO> copy=new HashMap<Object, GLMeshVBO>(meshs);
		copy.keySet().removeAll(keepMeshs);
		for(Object key:copy.keySet())
			{
			GLMeshVBO m=copy.get(key);
			m.destroy(gl);
			meshs.remove(key);
			}
		keepMeshs.clear();
		}
	
	
	
	/**
	 * Construct new component with access to common program data
	 */
	public ModelView(ModelWindow window)
		{
		this.window=window;
		addGLEventListener(glEventListener);
		}
	
	/**
	 * Listener for select changes. hoverinit is always called first once, then hover with the id
	 * if it is hovered
	 */
	public interface GLSelectListener
		{
		public void hoverInit(int id);
		public void hover(int id);
		}
	
	private int selectColorNum;
	private final HashMap<Integer,GLSelectListener> selectColorExtensionMap=new HashMap<Integer,GLSelectListener>();
	private void resetSelectColor()
		{
		selectColorNum=0;
		selectColorExtensionMap.clear();
		}
	public int reserveSelectColor(GLSelectListener ext)
		{
		//Obtain unique color. 
		selectColorNum++;
		selectColorExtensionMap.put(selectColorNum, ext);
		return selectColorNum;
		}
	public void setReserveColor(GL gl, int selectColorNum)
		{
		//Currently makes strong assumption of at least 24-bit colors.
		//it might bug out by signedness. need to be checked.
		//GL_BYTE could be used instead.
		byte colR=(byte)((selectColorNum    ) & 0x7F);
		byte colG=(byte)((selectColorNum>>7 ) & 0x7F);
		byte colB=(byte)((selectColorNum>>14));
//		System.out.println("out "+selectColorNum+" "+colR+" "+colG+" "+colB);
		gl.getGL2().glColor3ub(colR,colG,colB);
		}
	
	
	private boolean force=false;
	public void forceRepaint()
		{
		force=true;
		System.out.println("force enabled");
		}
	
	public Matrix4d projectionMatrix=new Matrix4d();
	private void setProjectMatrix(Matrix4d m)
		{
		projectionMatrix=m;
		}
	public Matrix4d getProjectionMatrix()
		{
		return projectionMatrix;
		}
	
	
	private GLEventListener glEventListener=new GLEventListener()
		{		
		HashSet<ModelWindowHook> hasInited=new HashSet<ModelWindowHook>();
		
		float light_position[][] = new float[][]{
					{ 0f, 0f, 100.0f, 0.0f },
					//{ 100.0f, 100.0f, 100.0f, 0.0f },
					//{ -100.0f, -100.0f, 100.0f, 0.0f }
				};

		/**
		 * Called once when OpenGL is inititalized
		 */
		public void init(GLAutoDrawable drawable)
			{
			hasInited.clear();
			
			//Get debug info
			if(EV.debugMode)
				drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
			
			//Get GL context
			GL2 gl = drawable.getGL().getGL2();

			checkerr(gl);
			
			//Switch off vertical synchronization. Might speed up
			gl.setSwapInterval(1);
		
			//GL states that won't change
			gl.glEnable(GL2.GL_CULL_FACE);
			gl.glEnable(GL2.GL_DEPTH_TEST);
			gl.glEnable(GL2.GL_NORMALIZE);
			gl.glShadeModel(GL2.GL_SMOOTH);
			
			checkerr(gl);
			
	    renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 72));

	    //Number of clipping planes
	    int[] queryArr=new int[1];
	    gl.glGetIntegerv(GL2.GL_MAX_CLIP_PLANES, queryArr, 0);
	    numClipPlanesSupported=queryArr[0];

	    checkerr(gl);

	    //3D texture support
	    gl.glGetIntegerv(GL2.GL_MAX_3D_TEXTURE_SIZE, queryArr, 0);
	    max3DTextureSize=queryArr[0];

	    checkerr(gl);

	    //Texture units
	    gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, queryArr, 0);
	    numTextureUnits=queryArr[0];
	    
	    checkerr(gl);
	    
	    //VBO support
	    VBOsupported= 
	    gl.isFunctionAvailable("glGenBuffersARB") &&
	    gl.isFunctionAvailable("glBindBufferARB") &&
	    gl.isFunctionAvailable("glBufferDataARB") &&
	    gl.isFunctionAvailable("glDeleteBuffersARB");
	    
	    //Shader support
	    shaderSupported=
			gl.isFunctionAvailable("glCreateShader") &&
			gl.isFunctionAvailable("glShaderSource") &&
			gl.isFunctionAvailable("glCompileShader") &&
			gl.isFunctionAvailable("glCreateProgram") &&
			gl.isFunctionAvailable("glAttachShader") &&
			gl.isFunctionAvailable("glLinkProgram") &&
			gl.isFunctionAvailable("glValidateProgram") &&
			gl.isFunctionAvailable("glUseProgram");

	    
	    if(true)
	    	{
	    	//crashed here, started working after syso. postpone swing?
				EvLog.printLog("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
				EvLog.printLog("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
				EvLog.printLog("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
				EvLog.printLog("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));
				EvLog.printLog("clipping planes supported: "+numClipPlanesSupported);
				EvLog.printLog("max 3D texture size: "+max3DTextureSize);
				EvLog.printLog("num texture units: "+numTextureUnits);
				EvLog.printLog("VBO supported: "+VBOsupported);
	    	}
	    
	    checkerr(gl);
			}

		
		/**
		 * Called when component is resized. Adjust OpenGL2.
		 */
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
			{
			GL2 gl = drawable.getGL().getGL2();
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();
			GLU glu=new GLU();
			glu.gluPerspective(FOV*180.0/Math.PI,(float)width/(float)height,0.1,30000);
			setProjectMatrix(getGluProjectionMatrix(FOV*180.0/Math.PI,(float)width/(float)height,0.1,30000));
			
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
			
			
			System.out.println("---- reshape ---- ");
			}
		

		
		public Matrix4d getGluProjectionMatrix(double fovy, double aspect, double zNear, double zFar)
			{
			double f=1.0/Math.tan(fovy/2);
			Matrix4d m=new Matrix4d();
			m.m00=f/aspect;
			m.m11=f;
			m.m22=(zFar+zNear)/(zNear-zFar);
			m.m23=2*zFar*zNear/(zNear-zFar);
			m.m32=-1;
			return m;
			}
		
		
		/**
		 * Called when it is time to render
		 */
		public void display(GLAutoDrawable drawable)
			{
			//System.out.println("render model");
			if(force)
				System.out.println("===forced set===");
			force=false;

			//Adjust scale
			double avdist=0;
			int numdist=0;
			for(ModelWindowHook h:window.modelWindowHooks)
				for(double dist:h.adjustScale())
					{
					avdist+=dist;
					numdist++;
					}
			avdist/=numdist;
			//Select pan speed
			panspeed=avdist/1000.0;
			//Select representative scale
			double g=Math.pow(10, (int)Math.log10(avdist));
			if(g<1) g=1;
			representativeScale=g;

			//Here it would be possible to auto-center the camera if it is totally out of range
			
			
			
			GL2 gl = drawable.getGL().getGL2();
			//Store away unaffected matrix
			//gl.glPushMatrix();
			
			checkerr(gl); //TODO upon start getting this
			
			
			
			window.crossHandler.resetCrossList();
			
			checkerr(gl);
			
			//(Re-)Init OpenGL if needed. Resizing window sometimes causes a need to re-init 
			for(ModelWindowHook h:window.modelWindowHooks)
				if(!hasInited.contains(h))
					{
					h.initOpenGL(gl);
					hasInited.add(h);
					}

			//Prepare render extensions
			for(ModelWindowHook h:window.modelWindowHooks)
				h.displayInit(gl);
			
			checkerr(gl);
			
			/////////////////////////////////
			// Render for selection
			/////////////////////////////////
			
			//Get camera into position
			gl.glLoadIdentity();
			camera.transformGL(gl);

			checkerr(gl);
			
			//Skip this step if mouse isn't even within the window
			if(mouseX>=0 && mouseY>=0)
				{
				for(Map.Entry<Integer,GLSelectListener> sel:selectColorExtensionMap.entrySet())
					sel.getValue().hoverInit(sel.getKey());
					
				//This could later be replaced by line-sphere intersection. it would be
				//a bit more cpu-intensive but cheap gfx-wise
				
				//Clear buffers
				gl.glClearColor(0f,0f,0f,0f);
				gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
				resetSelectColor();
				
				gl.glDisable(GL2.GL_LIGHTING);

				checkerr(gl);

				//Render extensions
				for(ModelWindowHook h:window.modelWindowHooks)
					{
					h.displaySelect(gl);
					checkerr(gl,h);
					}

				//Render cross. could be an extension, but order need be right
				window.crossHandler.displayCrossSelect(gl,window);
				
				
				//Figure out where the mouse is
				ByteBuffer rpix=ByteBuffer.allocate(3);
				gl.glReadPixels(mouseX,getHeight()-mouseY,1,1,GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, rpix);
				int colR=rpix.get(0);
				int colG=rpix.get(1)<<7;
				int colB=rpix.get(2)<<14;
				int pixelid=colR + colG + colB;
//				System.out.println("curhover "+colR+" "+colG+" "+colB+" %% "+mouseX+" "+mouseY+" && "+pixelid);

				//Update hover
				if(selectColorExtensionMap.containsKey(pixelid))
					selectColorExtensionMap.get(pixelid).hover(pixelid);
				}

			/////////////////////////////////
			// Render for viewing
			/////////////////////////////////
			

			if(!showSelectChannel)
				{
				gl.glLoadIdentity();

				//Set light to follow camera
				for(int i=0;i<light_position.length;i++)
					gl.glLightfv(GL2.GL_LIGHT0+i, GL2.GL_POSITION, light_position[i],0);
		    setupLight(gl);
				
				//Get camera into position
				camera.transformGL(gl);

				//Clear buffers
				gl.glClearColor((float)bgColor.getRed()/255.0f,(float)bgColor.getGreen()/255.0f,(float)bgColor.getBlue()/255.0f,0.0f);
				gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

				gl.glEnable(GL2.GL_LIGHTING);

				
				
				//Render cross. could be an extension
				window.crossHandler.displayCrossFinal(gl,window);
				
				checkerr(gl);
				
				//Render extensions
				List<TransparentRender> transparentRenderers=new LinkedList<TransparentRender>();
				for(ModelWindowHook h:window.modelWindowHooks)
					{
					h.displayFinal(gl, transparentRenderers);
					checkerr(gl,h);
					}
				
				
				//Take care of transparent renderers
				Collections.sort(transparentRenderers);
				RenderState currentRenderState=null;
				for(TransparentRender r:transparentRenderers)
					{
					//System.out.println("z "+r.z);
					if(r.renderState!=currentRenderState)
						{
						if(r.renderState!=null && r.renderState.optimizedSwitch(gl, currentRenderState))
							currentRenderState=r.renderState;
						else
							{
							if(currentRenderState!=null) currentRenderState.deactivate(gl);
							currentRenderState=r.renderState;
							if(currentRenderState!=null) currentRenderState.activate(gl);
							}
						}
					r.render(gl);
					checkerr(gl,r);
					}
				if(currentRenderState!=null)
					currentRenderState.deactivate(gl);
				
				}
			
			//Restore unaffected matrix
			//gl.glPopMatrix();

			if(!showSelectChannel)
				{
				//Axis rendering
				//Overlays everything else, has to be done absolutely last
				if(renderAxisArrows)
					renderAxisArrows(gl);
				}
			
			checkerr(gl);
			
			removeUnusedMesh(gl);
			
			checkerr(gl);
			//System.out.println("end of render");
			}

		
		
		public void setupLight(GL2 gl)
			{
			
			float lightAmbient[] = { 1.0f, 1.0f, 1.0f, 0.0f };
			float lightDiffuse[]=new float[]{1.0f,1.0f,1.0f};
			float lightSpecular[]=new float[]{1.0f,1.0f,1.0f};
/*
			float lightAmbient[] = { 0.3f, 0.3f, 0.3f, 0.0f };
			float lightDiffuse[]=new float[]{1.0f,1.0f,1.0f};
			float lightSpecular[]=new float[]{0.5f,0.5f,0.5f};
*/
			/*
			float lightAmbient[] ={0.2f, 0.2f, 0.2f, 1.0f};
			float lightDiffuse[] ={1.0f, 1.0f, 1.0f, 1.0f};
			float lightSpecular[]={1.0f, 1.0f, 1.0f, 0.0f};
			*/

			for(int i=0;i<light_position.length;i++)
				{
				gl.glLightfv(GL2.GL_LIGHT0+i, GL2.GL_AMBIENT, lightAmbient, 0);   
				gl.glLightfv(GL2.GL_LIGHT0+i, GL2.GL_DIFFUSE, lightDiffuse, 0);
				gl.glLightfv(GL2.GL_LIGHT0+i, GL2.GL_SPECULAR, lightSpecular, 0);
				gl.glEnable(GL2.GL_LIGHT0+i);
				}
			gl.glShadeModel(GL2.GL_SMOOTH);
			}
		
		private void renderAxisArrows(GL2 gl)
			{
			gl.glDisable(GL2.GL_LIGHTING);
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			GLU glu=new GLU();
			gl.glTranslatef(-0.9f,-0.9f,0);
			glu.gluPerspective(FOV*180.0/Math.PI,(float)getWidth()/(float)getHeight(),0.1,30000);
			//glu.gluOrtho2D(arg0, arg1, arg2, arg3)
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
			
			gl.glColor3f(1,1,1);
			Matrix3d camMat=new Matrix3d(camera.getRotationMatrixReadOnly());
			camMat.invert();
			double axisSize=0.05;
			float axisScale=0.8f;
			Vector3d axisX=new Vector3d(axisSize,0,0);
			Vector3d axisY=new Vector3d(0,axisSize,0);
			Vector3d axisZ=new Vector3d(0,0,axisSize);
			camMat.transform(axisX);
			camMat.transform(axisY);
			camMat.transform(axisZ);
			gl.glLineWidth(1);
			gl.glBegin(GL2.GL_LINES);
			gl.glVertex3f(0.0f,0.0f,-1.0f);
			gl.glVertex3f((float)axisX.x*axisScale,(float)axisX.y*axisScale,(float)(axisX.z*axisScale-1));
			gl.glVertex3f(0.0f,0.0f,-1.0f);
			gl.glVertex3f((float)axisY.x*axisScale,(float)axisY.y*axisScale,(float)(axisY.z*axisScale-1));
			gl.glVertex3f(0.0f,0.0f,-1.0f);
			gl.glVertex3f((float)axisZ.x*axisScale,(float)axisZ.y*axisScale,(float)(axisZ.z*axisScale-1));
			gl.glEnd();

			gl.glLineWidth(3f);
			float fsize=0.007f;
			gl.glColor3f(1,0,0);
			gl.glBegin(GL2.GL_LINES);
			gl.glVertex3f((float)axisX.x-fsize,(float)axisX.y-fsize,(float)(axisX.z-1));
			gl.glVertex3f((float)axisX.x+fsize,(float)axisX.y+fsize,(float)(axisX.z-1));
			gl.glVertex3f((float)axisX.x+fsize,(float)axisX.y-fsize,(float)(axisX.z-1));
			gl.glVertex3f((float)axisX.x-fsize,(float)axisX.y+fsize,(float)(axisX.z-1));
			gl.glVertex3f((float)axisY.x,(float)axisY.y,(float)(axisY.z-1));
			gl.glVertex3f((float)axisY.x-fsize,(float)axisY.y+fsize,(float)(axisY.z-1));
			gl.glVertex3f((float)axisY.x,(float)axisY.y,(float)(axisY.z-1));
			gl.glVertex3f((float)axisY.x+fsize,(float)axisY.y+fsize,(float)(axisY.z-1));
			gl.glVertex3f((float)axisY.x,(float)axisY.y,(float)(axisY.z-1));
			gl.glVertex3f((float)axisY.x,(float)axisY.y-fsize,(float)(axisY.z-1));
			gl.glVertex3f((float)axisZ.x-fsize,(float)axisZ.y-fsize,(float)(axisZ.z-1));
			gl.glVertex3f((float)axisZ.x+fsize,(float)axisZ.y+fsize,(float)(axisZ.z-1));
			gl.glVertex3f((float)axisZ.x-fsize,(float)axisZ.y-fsize,(float)(axisZ.z-1));
			gl.glVertex3f((float)axisZ.x+fsize,(float)axisZ.y-fsize,(float)(axisZ.z-1));
			gl.glVertex3f((float)axisZ.x-fsize,(float)axisZ.y+fsize,(float)(axisZ.z-1));
			gl.glVertex3f((float)axisZ.x+fsize,(float)axisZ.y+fsize,(float)(axisZ.z-1));
			gl.glEnd();
			
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glPopMatrix();
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			}



		public void dispose(GLAutoDrawable arg0)
			{
			}
		
		};
	

		
		
	
	/**
	 * Place camera at a distance, position and angle that makes the whole model fit
	 */
	public void autoCenter()
		{
		Vector<Vector3d> center=new Vector<Vector3d>();

		//Find centers of everything
		for(ModelWindowHook h:window.modelWindowHooks)
			for(Vector3d newcenter:h.autoCenterMid())
				center.add(newcenter);

		//Default center
		if(center.isEmpty())
			center.add(new Vector3d(0,0,0));
		
		Vector3d mid=new Vector3d(0,0,0);
		for(Vector3d v:center)
			mid.add(v);
		mid.scale(1.0/center.size());

		//Figure out required distance
		
//		return Collections.singleton((Double)maxr/Math.sin(FOV));

		
		double dist=0;
		for(ModelWindowHook h:window.modelWindowHooks)
			{
			double nrad=h.autoCenterRadius(mid);
			if(nrad>dist)
				dist=nrad;
			}
		//Avoid divison by zero at least
		if(dist==0)
			dist=1;
		dist/=Math.sin(FOV);

		//Set camera
		camera.center.x=mid.x;
		camera.center.y=mid.y;
		camera.center.z=mid.z;
		camera.center(dist);

		if(EV.debugMode)
			System.out.println("center: xyz "+camera.center.x+" "+camera.center.y+" "+camera.center.z+" dist "+dist);
		repaint();
		}
	
	
	public static boolean checkerr(GL gl)
		{
		return checkerr(gl,null);
		}
	public static boolean checkerr(GL gl, Object data)
		{
		int errcode=gl.glGetError();
		if(errcode!=GL2.GL_NO_ERROR)
			{
			try
				{
				throw new Exception("GL error: "+new GLU().gluErrorString(errcode));
				}
			catch (Exception e)
				{
				if(data!=null)
					System.out.println("## "+data);
				e.printStackTrace();
				}
			return true;
			}
		else
			return false;
		}
	
	/**
	 * Pan by a vector, world coordinates. 
	 * This vector is scaled depending on the size of the model.
	 */
	public void pan(double dx, double dy, double dz, boolean moveCenter)
		{
		if(moveCenter)
			{
			Vector3d v=camera.rotateVector(dx*panspeed, dy*panspeed, dz*panspeed);
			camera.pos.add(v);
			camera.center.add(v);
			}
		else
			camera.moveCamera(dx*panspeed, dy*panspeed, dz*panspeed);
		}
	
	public double getRepresentativeScale()
		{
		return representativeScale;
		}

	
	/**
	 * Render text in 3D
	 */
	public void renderString(GL2 gl, List<TransparentRender> transparentRenderers, final float textScaleFactor, final String text)
		{
		final float[] matarray=new float[16]; //[col][row]
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, matarray, 0);
		final TextRenderer thisRenderer=renderer;
		TransparentRender rend=new TransparentRender(){
		public void render(GL glin)
			{
			GL2 gl=glin.getGL2();

			//Save current state. Restore model view matrix as when this function was called.
			gl.glPushAttrib(GL2.GL_ENABLE_BIT);
			gl.glPushMatrix();
			gl.glLoadMatrixf(matarray, 0);
			
			thisRenderer.begin3DRendering();

			//make global I guess?
			//TODO when rendering sorted objects, can disable this all the time as default
			gl.glDisable(GL2.GL_CULL_FACE);
			
			//Note that the defaults for glCullFace and glFrontFace are GL_BACK and GL_CCW, which
			//match the TextRenderer's definition of front-facing text.
			Rectangle2D bounds = thisRenderer.getBounds(text);
			float w = (float) bounds.getWidth();
			float h = (float) bounds.getHeight();
			renderer.draw3D(text, w / -2.0f * textScaleFactor, h / -2.0f * textScaleFactor, 0, textScaleFactor);

			//Clean up
			renderer.end3DRendering();
			gl.glPopMatrix();
			gl.glPopAttrib();
			
			checkerr(gl,h);
			}
		};
		//Calculate z. Current coordinate: 0 0 0 1
		rend.z=matarray[14];
		transparentRenderers.add(rend);
		}


	
	/**
	 * TODO Overload repaint; can force some redraws to set a minimum FPS
	 */
	public void repaint()
		{
		super.repaint();
		}
	
	
	public BufferedImage getScreenshot()
		{
		GLContext c=getContext();
		c.makeCurrent();
		BufferedImage image=getFrameData(getGL());
		c.release();
		return image;
		}
	
	/**
	 * Get framebuffer data. Has to hold GL context
	 */
	private ByteBuffer getFrameData( GL glin, ByteBuffer pixelsRGB ) 
		{
		GL2 gl=glin.getGL2();
		gl.glReadBuffer( GL2.GL_BACK );
		gl.glPixelStorei( GL2.GL_PACK_ALIGNMENT, 1 );
		gl.glReadPixels( 0, 0, getWidth(), getHeight(), GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, pixelsRGB );
		return pixelsRGB;
		}

	/**
	 * Get framebuffer data. Has to hold GL context
	 */
	private BufferedImage getFrameData( GL gl ) 
		{  
		// Create a ByteBuffer to hold the frame data.
		java.nio.ByteBuffer pixelsRGB = ByteBuffer.allocateDirect	( getWidth() * getHeight() * 3 ); 

		// Get date from frame as ByteBuffer.
		getFrameData( gl, pixelsRGB );

		return transformPixelsRGBBuffer2ARGB_ByHand( pixelsRGB );
		}

	/**
	 * Convert framebuffer to AWT format
	 */
	private BufferedImage transformPixelsRGBBuffer2ARGB_ByHand(ByteBuffer pixelsRGB)
		{
		// Transform the ByteBuffer and get it as pixeldata.

		int w=getWidth();
		int h=getHeight();

		int[] pixelInts = new int[w*h];

		// Convert RGB bytes to ARGB ints with no transparency. 
		// Flip image vertically by reading the
		// rows of pixels in the byte buffer in reverse 
		// - (0,0) is at bottom left in OpenGL2.
		//
		// Points to first byte (red) in each row.
		int p = w*h*3; 
		int i = 0; // Index into target int[]
		int w3 = w*3; // Number of bytes in each row
		for(int row = 0; row < h; row++) 
			{
			p -= w3;
			int q = p;
			for(int col = 0; col < w; col++)
				{
				int iR = pixelsRGB.get(q++);
				int iG = pixelsRGB.get(q++);
				int iB = pixelsRGB.get(q++);
				pixelInts[i++] = 0xFF000000 | ((iR & 0x000000FF) << 16) | ((iG & 0x000000FF) << 8) | (iB & 0x000000FF);
				}
			}

		// Create a new BufferedImage from the pixeldata.
		BufferedImage bufferedImage = new BufferedImage( getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		bufferedImage.setRGB( 0, 0, w, h, pixelInts, 0, w );

		return bufferedImage;
		}
	
	
	
	public double getArrowLength()
		{
		return representativeScale*0.04*2;
		}
	
	/**
	 * Draw the head of an arrow, with specified tip point and direction. Uses the currently selected color.
	 * Will turn on normalize. lighting should be enabled
	 * 
	 * TODO use vertex arrays, precalc arrow polygons. use model matrix to transform. use phong shading.
	 */
	public void renderArrowHead(GL glin, Vector3d tip, Vector3d direction, float colR, float colG, float colB)
		{
		GL2 gl=glin.getGL2();

		CoordinateSystem cs=new CoordinateSystem();
		CoordinateSystem csRot=new CoordinateSystem();
	
		
		//System.out.println("render arrow");
		
		//Need to find a perpendicular vector
		Vector3d up=new Vector3d(0,0,1);
		Vector3d right=new Vector3d(1,0,0);
		if(direction.equals(up))
			{
			cs.setFromTwoVectors(direction, right, 1, 1, 1, tip);
			csRot.setFromTwoVectors(direction, right, 1, 1, 1, new Vector3d());
			}
		else
			{
			cs.setFromTwoVectors(direction, up, 1, 1, 1, tip);
			csRot.setFromTwoVectors(direction, up, 1, 1, 1, new Vector3d());
			}
		
		//gl.glShadeModel(GL2.GL_SMOOTH); //temp
		
		int numAngle=10;
//		double r=representativeScale*0.04;
//		double length=r*2;
	
//		double length=representativeScale*0.04*2;
		double length=getArrowLength();
		double r=length/2;
		
		
		Vector3d[] points=new Vector3d[numAngle];
		Vector3d[] normals=new Vector3d[numAngle];
		
//		gl.glEnable(GL2.GL_AUTO_NORMAL);
		gl.glEnable(GL2.GL_NORMALIZE); //used for normal at tip
		
  	gl.glColor3d(1,1,1);
  	gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, new float[]{colR,colG,colB}, 0);

		
		for(int i=0;i<numAngle;i++)
			{
			double angle=2*Math.PI*i/numAngle;
			double cos=Math.cos(angle);
			double sin=Math.sin(angle);
			points[i]=cs.transformToSystem(new Vector3d(-length,r*cos,r*sin));
			normals[i]=csRot.transformToSystem(new Vector3d(r,length*cos,length*sin)); //Assume later normalization
			//normals[i].normalize(); //temp
			//System.out.println(normals[i]);
			}
		
		Vector3d normalBack=csRot.transformToSystem(new Vector3d(-1,0,0));
		
		/*
		gl.glBegin(GL2.GL_TRIANGLE_FAN);
		gl.glNormal3d(normals[0].x, normals[0].y, normals[0].z);
		gl.glVertex3d(tip.x, tip.y, tip.z);
		for(int i=0;i<numAngle;i++)
			{
			gl.glNormal3d(normals[i].x, normals[i].y, normals[i].z);
			gl.glVertex3d(points[i].x, points[i].y, points[i].z);
			}
		gl.glNormal3d(normals[0].x, normals[0].y, normals[0].z);
		gl.glVertex3d(points[0].x, points[0].y, points[0].z);
		gl.glEnd();*/
		
		
//		for(int i=0;i<1;i++)
		/*
		gl.glBegin(GL2.GL_TRIANGLES);
		for(int i=0;i<numAngle;i++)
			{
//			gl.glBegin(GL2.GL_LINE_LOOP);
			int next=(i+1)%numAngle;
			gl.glNormal3d(normals[i].x+normals[next].x, normals[i].y+normals[next].y, normals[i].z+normals[next].z);
			gl.glVertex3d(tip.x, tip.y, tip.z);
			gl.glNormal3d(normals[i].x, normals[i].y, normals[i].z);
			gl.glVertex3d(points[i].x, points[i].y, points[i].z);
			
			gl.glNormal3d(normals[next].x, normals[next].y, normals[next].z);
			gl.glVertex3d(points[next].x, points[next].y, points[next].z);
			}
		gl.glEnd();
		*/
		
		gl.glBegin(GL2.GL_QUADS);
		for(int i=0;i<numAngle;i++)
			{
			int next=(i+1)%numAngle;
			
			gl.glNormal3d(normals[i].x, normals[i].y, normals[i].z);
			gl.glVertex3d(tip.x, tip.y, tip.z);

			
			gl.glNormal3d(normals[next].x, normals[next].y, normals[next].z);
			gl.glVertex3d(tip.x, tip.y, tip.z);
			
						
			gl.glNormal3d(normals[i].x, normals[i].y, normals[i].z);
			gl.glVertex3d(points[i].x, points[i].y, points[i].z);
			
			gl.glNormal3d(normals[next].x, normals[next].y, normals[next].z);
			gl.glVertex3d(points[next].x, points[next].y, points[next].z);
			}
		gl.glEnd();
		
		
		gl.glBegin(GL2.GL_POLYGON);
		gl.glNormal3d(normalBack.x, normalBack.y, normalBack.z);
		for(int i=numAngle-1;i>=0;i--)
			gl.glVertex3d(points[i].x, points[i].y, points[i].z);
		int lasta=numAngle-1;
		gl.glVertex3d(points[lasta].x, points[lasta].y, points[lasta].z);
		gl.glEnd();
		
		
		/*
		 * This means a lot of trig. An option is to store down the tip once in a list, then write a shader
		 * that applies the local transform. 
		 */
		
		}
	

	//http://www.felixgers.de/teaching/jogl/imagingProg.html


	public Vector3d getMouseMoveVector(int dx, int dy, Vector3d point)
		{
		Vector3d nucPosCamera=camera.transformPoint(point);
		return getMouseMoveVector(dx,dy,nucPosCamera.z);
		}
	/**
	 * Get the vector associated with the mouse moving along the screen
	 */
	public Vector3d getMouseMoveVector(int dx, int dy, double z)
		{
		Matrix4d mProj=getProjectionMatrix();
		double a=mProj.m00;
		double b=mProj.m11;
		Vector3d moveVecCamera=new Vector3d(
				-dx*a/z,
				dy*b/z,
				0);
		return camera.rotateVector(moveVecCamera);
		}
	}



