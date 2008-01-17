package evplugin.modelWindow;

import java.util.*;
import java.awt.geom.*;
import java.awt.Font;
import java.nio.*;

//import javax.media.j3d.Texture;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.vecmath.Vector3d;


//import com.sun.opengl.util.Screenshot;
import com.sun.opengl.util.j2d.*;

import evplugin.basicWindow.*;
import evplugin.data.*;
import evplugin.ev.*;
import evplugin.nuc.*;


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
	
	
	
	

	private int selectColorNum;
	final private HashMap<Integer,ModelWindowHook> selectColorExtensionMap=new HashMap<Integer,ModelWindowHook>();
	private void resetSelectColor()
		{
		selectColorNum=0;
		selectColorExtensionMap.clear();
		}
	public int reserveSelectColor(ModelWindowHook ext)
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
		byte colR=(byte)((selectColorNum    ) & 0xFF);
		byte colG=(byte)((selectColorNum>>8 ) & 0xFF);
		byte colB=(byte)((selectColorNum>>16));
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
			
			//Skip this step if mouse isn't even within the window
			if(mouseX>=0 && mouseY>=0)
				{
				//Update hover
				NucPair lastHover=NucLineage.currentHover;
				NucLineage.currentHover=new NucPair();

				//This could later be replaced by line-sphere intersection. it would be
				//a bit more cpu-intensive but cheap gfx-wise
				
				//Clear buffers
				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				resetSelectColor();
				
				//Render extensions
				for(ModelWindowHook h:window.modelWindowHooks)
					h.displaySelect(gl);

				//Figure out where the mouse is
				ByteBuffer rpix=ByteBuffer.allocate(3);
				gl.glReadPixels(mouseX,getHeight()-mouseY,1,1,GL.GL_RGB, GL.GL_UNSIGNED_BYTE, rpix);
				int colR=rpix.get(0);
				int colG=(((int)rpix.get(1))<<8);
				int colB=(((int)rpix.get(2))<<16);
				int pixelid=colR + colG + colB;
				//EV.printDebug("curhover "+colR+" "+colG+" "+colB+" %% "+mouseX+" "+mouseY+" && "+pixelid);

				//Update hover
				if(selectColorExtensionMap.containsKey(pixelid))
					selectColorExtensionMap.get(pixelid).select(pixelid);

				//Propagate hover. Avoid infinite recursion.
				if(!NucLineage.currentHover.equals(lastHover))
					BasicWindow.updateWindows(window);
				}

			/////////////////////////////////
			// Render for viewing
			/////////////////////////////////

			//Clear buffers
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

			
			//Render extensions
			for(ModelWindowHook h:window.modelWindowHooks) //todo: order of rendering
				h.displayFinal(gl);
			
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
		
		renderer.end3DRendering();
		gl.glEnable(GL.GL_CULL_FACE);
		}
	
	}





/*
float red[] = { 0.8f, 0.1f, 0.0f, 1.0f };
float green[] = { 0.0f, 0.8f, 0.2f, 1.0f };
float blue[] = { 0.2f, 0.2f, 1.0f, 1.0f };
gear1 = gl.glGenLists(1);
gl.glNewList(gear1, GL.GL_COMPILE);
gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, red, 0);
gear(gl, 1.0f, 4.0f, 1.0f, 20, 0.7f);
gl.glEndList();
 * void GLWidget::cbPresetFront()
 { rotX=   0;  rotY=  0;  updateGL(); }
void GLWidget::cbPresetBack()
 { rotX=   0;  rotY=180;  updateGL(); }
void GLWidget::cbPresetLeft()
 { rotX=  0;  rotY= 90;  updateGL(); }
void GLWidget::cbPresetRight()
 { rotX=  0;  rotY=-90;  updateGL(); }
void GLWidget::cbPresetTop()
 { rotX= 90;  rotY=  0;  updateGL(); }
void GLWidget::cbPresetBottom()
 { rotX=-90;  rotY=  0;  updateGL(); }

gl.glTranslatef(-3.0f, -2.0f, 0.0f);
gl.glRotatef(angle, 0.0f, 0.0f, 1.0f);
gl.glCallList(gear1);
	gl.glNormal3f(0.0f, 0.0f, 1.0f);
	gl.glBegin(GL.GL_QUADS);
		gl.glBegin(GL.GL_QUAD_STRIP);
		gl.glVertex3f(r0 * (float)Math.cos(angle), r0 * (float)Math.sin(angle), width * 0.5f);
		gl.glNormal3f(-(float)Math.cos(angle), -(float)Math.sin(angle), 0.0f);
	gl.glEnd();
	 */
