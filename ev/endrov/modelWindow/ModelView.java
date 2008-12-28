package endrov.modelWindow;

import java.util.*;
import java.awt.geom.*;
import java.awt.Color;
import java.awt.Font;
import java.nio.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.vecmath.Vector3d;

//import com.sun.opengl.util.Screenshot;
import com.sun.opengl.util.j2d.*;

import endrov.ev.*;
import endrov.modelWindow.TransparentRender.RenderState;
import endrov.util.EvDecimal;


//NEED GLJPanel
//GLCanvas fast

//http://fivedots.coe.psu.ac.th/~ad/jg2/ch15/jogl1v4.pdf
//talks about -Dsun.java2d.opengl=true, uses gljpanel

/**
 * A panel for displaying the model
 */
public class ModelView extends GLCanvas
	{
	public static final long serialVersionUID=0;
	
	//method: display(). faster than repaint.
	
	
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
	public Camera camera=new Camera();
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

	
	public Color bgColor=Color.BLACK;
	
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
	final private HashMap<Integer,GLSelectListener> selectColorExtensionMap=new HashMap<Integer,GLSelectListener>();
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
		gl.glColor3ub(colR,colG,colB);
		}
	
	
	private boolean force=false;
	public void forceRepaint()
		{
		force=true;
		System.out.println("force enabled");
		}
	
	private GLEventListener glEventListener=new GLEventListener()
		{
		/**
		 * Called once when OpenGL is inititalized
		 */
		public void init(GLAutoDrawable drawable)
			{
			//Get debug info
			if(EV.debugMode)
				{
				drawable.setGL(new DebugGL(drawable.getGL()));
				GL gl = drawable.getGL();
				}
			
			//Get GL context
			GL gl = drawable.getGL();

			//Switch off vertical synchronization. Might speed up
			gl.setSwapInterval(1);
		
			//GL states that won't change
			gl.glEnable(GL.GL_CULL_FACE);
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glEnable(GL.GL_NORMALIZE);
			gl.glShadeModel(GL.GL_SMOOTH);
			
	    renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 72));

	    //Number of clipping planes
	    int[] queryArr=new int[1];
	    gl.glGetIntegerv(GL.GL_MAX_CLIP_PLANES, queryArr, 0);
	    numClipPlanesSupported=queryArr[0];
	    
	    //3D texture support
	    gl.glGetIntegerv(GL.GL_MAX_3D_TEXTURE_SIZE, queryArr, 0);
	    max3DTextureSize=queryArr[0];

	    //Texture units
	    gl.glGetIntegerv(GL.GL_MAX_TEXTURE_UNITS, queryArr, 0);
	    numTextureUnits=queryArr[0];
	    
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
				Log.printLog("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
				Log.printLog("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
				Log.printLog("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
				Log.printLog("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
				Log.printLog("clipping planes supported: "+numClipPlanesSupported);
				Log.printLog("max 3D texture size: "+max3DTextureSize);
				Log.printLog("num texture units: "+numTextureUnits);
				Log.printLog("VBO supported: "+VBOsupported);
	    	}
			}

		

		public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
			{
			}
		
		/**
		 * Called when component is resized. Adjust OpenGL.
		 */
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
			{
			GL gl = drawable.getGL();
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glLoadIdentity();
			GLU glu=new GLU();
			glu.gluPerspective(FOV*180.0/Math.PI,(float)width/(float)height,0.1,30000);
			
			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glLoadIdentity();
			}
		
		
		
		/**
		 * Called when it is time to render
		 */
		public void display(GLAutoDrawable drawable)
			{
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
			
			
			//Store away unaffected matrix
			GL gl = drawable.getGL();
			gl.glPushMatrix();
			
			 //Set light to follow camera
			float light_position[] = { 1.0f, 1.0f, 1.0f, 0.0f };
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light_position,0); //have no idea what 0 does
			
			//Get camera into position
			camera.transformGL(gl);
			
			window.crossHandler.resetCrossList();
			
			//Prepare render extensions
			for(ModelWindowHook h:window.modelWindowHooks)
				h.displayInit(gl);
			
			
			/////////////////////////////////
			// Render for selection
			/////////////////////////////////
			
			
			//Skip this step if mouse isn't even within the window
			if(mouseX>=0 && mouseY>=0)
				{
				for(Map.Entry<Integer,GLSelectListener> sel:selectColorExtensionMap.entrySet())
					sel.getValue().hoverInit(sel.getKey());
					
				//This could later be replaced by line-sphere intersection. it would be
				//a bit more cpu-intensive but cheap gfx-wise
				
				//Clear buffers
				gl.glClearColor(0f,0f,0f,0f);
				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				resetSelectColor();
				
				//Render extensions
				for(ModelWindowHook h:window.modelWindowHooks)
					h.displaySelect(gl);

				//Render cross. could be an extension, but order need be right
				window.crossHandler.displayCrossSelect(gl,window);
				
				
				//Figure out where the mouse is
				ByteBuffer rpix=ByteBuffer.allocate(3);
				gl.glReadPixels(mouseX,getHeight()-mouseY,1,1,GL.GL_RGB, GL.GL_UNSIGNED_BYTE, rpix);
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
			


			//Clear buffers
			gl.glClearColor((float)bgColor.getRed()/255.0f,(float)bgColor.getGreen()/255.0f,(float)bgColor.getBlue()/255.0f,0.0f);
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

			
			
			
			//Render cross. could be an extension
			window.crossHandler.displayCrossFinal(gl,window);
			
			//Render extensions
			List<TransparentRender> transparentRenderers=new LinkedList<TransparentRender>();
			for(ModelWindowHook h:window.modelWindowHooks)
				h.displayFinal(gl, transparentRenderers);
			
			
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
				}
			if(currentRenderState!=null)
				currentRenderState.deactivate(gl);
			
			
			
			//Restore unaffected matrix
			gl.glPopMatrix();
			}

		
		};
	

/*	private cameraMoved
	public void autoCenterIfNeeded()
		{
			TODO autocenter if needed not obvious how to implement
			
		}*/
	
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
		double dist=0;
		for(ModelWindowHook h:window.modelWindowHooks)
			{
			for(Double newDist:h.autoCenterRadius(mid,FOV))
				if(dist<newDist)
					dist=newDist;
			}
		//Avoid divison by zero at least
		if(dist==0)
			dist=1;

		//Set camera
		camera.center.x=mid.x;
		camera.center.y=mid.y;
		camera.center.z=mid.z;
		camera.center(dist);

		if(EV.debugMode)
			System.out.println("center: xyz "+camera.center.x+" "+camera.center.y+" "+camera.center.z+" dist "+dist);
		repaint();
		}
	
	
	
	/**
	 * Pan by a vector, world coordinates. 
	 * This vector is scaled depending on the size of the model.
	 */
	public void pan(double dx, double dy, double dz, boolean moveCenter)
		{
		if(moveCenter)
			{
			Vector3d v=camera.transformedVector(dx*panspeed, dy*panspeed, dz*panspeed);
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
	 * @param renderer Font renderer
	 * @param textScaleFactor Size of font
	 * @param text String to render
	 */
	public void renderString(GL gl, List<TransparentRender> transparentRenderers, final float textScaleFactor, final String text)
		{
		final float[] matarray=new float[16]; //[col][row]
		gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, matarray, 0);
		final TextRenderer thisRenderer=renderer;
		TransparentRender rend=new TransparentRender(){
		public void render(GL gl)
			{
			//Save current state. Restore model view matrix as when this function was called.
			gl.glPushAttrib(GL.GL_ENABLE_BIT);
			gl.glPushMatrix();
			gl.glLoadMatrixf(matarray, 0);
			
			thisRenderer.begin3DRendering();

			//make global I guess?
			//TODO when rendering sorted objects, can disable this all the time as default
			gl.glDisable(GL.GL_CULL_FACE);
			
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
			}
		};
		//Calculate z. Current coordinate: 0 0 0 1
		rend.z=matarray[14];
		transparentRenderers.add(rend);
		}

	
	/**
	 * TODO
	 * Overload repaint; can force some redraws to set a minimum FPS
	 */
	public void repaint()
		{
		super.repaint();
		}
	
	
	}



