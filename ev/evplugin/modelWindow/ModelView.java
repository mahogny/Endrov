package evplugin.modelWindow;

import java.util.*;
import java.awt.geom.*;
import java.awt.Font;
import java.nio.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.vecmath.Vector3d;


//import com.sun.opengl.util.Screenshot;
import com.sun.opengl.util.j2d.*;

import evplugin.data.*;
import evplugin.ev.*;


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
	
	/** Number of clipping planes supported by this context */
	public int numClipPlanesSupported;
	
	/** Common data */
	private ModelWindow window;

	/** Camera coordinates */
	public Camera camera=new Camera();
	private final double FOV=45.0/180.0*Math.PI;	
	/** Current frame */
	public double frame=0;
	

	/** Scaling factor for panning */
	public double panspeed=1; //private TODO

	
	/** Current mouse coordinate */
	public int mouseX=-1, mouseY=-1;	
	public TextRenderer renderer;

	
	
	
	/** Get metadata. Never returns null */
	EvData meta=new EvDataEmpty();
	public EvData getMetadata()
		{
		return meta;
		}
	
	
	/**
	 * Construct new component with access to common program data
	 */
	public ModelView(ModelWindow window)
		{
		this.window=window;

		addGLEventListener(glEventListener);
		
		
		
		}
	
	
	public interface GLSelectListener
		{
		public void hoverInit(int id);
		public void hover(int id);
		}
	
	//selectColorExtensionMap only used here. use something else than hook, new listener
	//events: hover, click, drag etc

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
				Log.printLog("INIT GL IS: " + gl.getClass().getName());
				Log.printLog("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
				Log.printLog("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
				Log.printLog("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
				Log.printLog("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
				}
			
			//Get GL context
			GL gl = drawable.getGL();

			//Switch off vertical synchronization. Might speed up
			gl.setSwapInterval(1);
		
			//GL states that won't change
			gl.glEnable(GL.GL_CULL_FACE);
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glEnable(GL.GL_NORMALIZE);
			gl.glShadeModel(GL.GL_SMOOTH); //GL_FLAT
			
	    renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 72));
	    
	    int[] planesArr=new int[1];
	    gl.glGetIntegerv(GL.GL_MAX_CLIP_PLANES, planesArr, 0);
	    numClipPlanesSupported=planesArr[0];
	    System.out.println("clipping planes supported: "+numClipPlanesSupported);
	    //6 planes on NVidia macpro
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
			//Store away unaffected matrix
			GL gl = drawable.getGL();
			gl.glPushMatrix();
			
			 //Set light to follow camera
			float light_position[] = { 1.0f, 1.0f, 1.0f, 0.0f };
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light_position,0); //have no idea what 0 does
			
			//Get camera into position
			camera.transformGL(gl);
			
			
			//Prepare render extensions
			for(ModelWindowHook h:window.modelWindowHooks)
				h.displayInit(gl);
			
			/////////////////////////////////
			// Render for selection
			/////////////////////////////////
			
			window.resetCrossList();
			
			//Skip this step if mouse isn't even within the window
			if(mouseX>=0 && mouseY>=0)
				{
				for(Map.Entry<Integer,GLSelectListener> sel:selectColorExtensionMap.entrySet())
					sel.getValue().hoverInit(sel.getKey());
					
				//This could later be replaced by line-sphere intersection. it would be
				//a bit more cpu-intensive but cheap gfx-wise
				
				//Clear buffers
				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				resetSelectColor();
				
				//Render extensions
				for(ModelWindowHook h:window.modelWindowHooks)
					h.displaySelect(gl);

				//Render cross. could be an extension, but order need be right
				window.displayCrossSelect(gl);
				
				
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
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

			
			//Render extensions
			for(ModelWindowHook h:window.modelWindowHooks) //todo: order of rendering
				h.displayFinal(gl);
			
			//Render cross. could be an extension
			window.displayCrossFinal(gl);

			
			//adjust scale for next time
			//TODO: should take all into account somehow. average?
			for(ModelWindowHook h:window.modelWindowHooks)
				{
				for(double dist:h.adjustScale())
					{
					//Select pan speed
					panspeed=dist/1000.0;
					
					//Select grid size
					double g=Math.pow(10, (int)Math.log10(dist));
					if(g<1) g=1;
					ModelWindowGrid.setGridSize(window,g);
					}
				}
			
			//Restore unaffected matrix
			gl.glPopMatrix();
			}

		
		};
	

	
	
	
	/**
	 * Place camera at a distance, position and angle that makes the whole model fit
	 */
	public void autoCenter()
		{
		Vector<Vector3D> center=new Vector<Vector3D>();

		//Find centers of everything
		for(ModelWindowHook h:window.modelWindowHooks)
			for(Vector3D newcenter:h.autoCenterMid())
				center.add(newcenter);

		//If centers were available, continue
		if(!center.isEmpty())
			{
			Vector3D mid=new Vector3D(0,0,0);
			for(Vector3D v:center)
				mid=mid.add(v);
			mid=mid.mul(1.0/center.size());
			
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
			
			//TODO: if NaN, restore and warn
			
			camera.center(dist);
			
			if(EV.debugMode)
				System.out.println("center: xyz "+camera.center.x+" "+camera.center.y+" "+camera.center.z+" dist "+dist);
			}
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
	
	
	
	/**
	 * Render text in 3D
	 * @param gl OpenGL context
	 * @param renderer Font renderer
	 * @param textScaleFactor Size of font
	 * @param text String to render
	 */
	public void renderString(GL gl, TextRenderer renderer,
			float textScaleFactor, 
      String text)
		{
		renderer.begin3DRendering();
		
		//make global I guess?
		gl.glDisable(GL.GL_CULL_FACE);
		
		//		Note that the defaults for glCullFace and glFrontFace are
		//		GL_BACK and GL_CCW, which match the TextRenderer's definition
		//		of front-facing text.
		Rectangle2D bounds = renderer.getBounds(text);
		float w = (float) bounds.getWidth();
		float h = (float) bounds.getHeight();
		renderer.draw3D(text, w / -2.0f * textScaleFactor, h / -2.0f * textScaleFactor, 0, textScaleFactor);
		
		//// OPTIMIZATION: would it be faster if I fixed scalefactor and scaled in GL?
		
		renderer.end3DRendering();
		gl.glEnable(GL.GL_CULL_FACE);
		}
	
	
	
	
	
	
	
	
	}



