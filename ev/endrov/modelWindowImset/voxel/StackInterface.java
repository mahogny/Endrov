/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindowImset.voxel;

import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Vector3d;

import endrov.imageset.EvChannel;
import endrov.modelWindow.Camera;
import endrov.modelWindow.ModelWindow;
import endrov.modelWindow.TransparentRender;
import endrov.util.EvDecimal;
import endrov.modelWindow.ModelWindow.ProgressMeter;

/**
 * General interface to any stack renderer
 * @author Johan Henriksson
 */
public abstract class StackInterface
	{
	//public abstract boolean needSettings(EvDecimal frame);
	//public abstract void setLastFrame(EvDecimal frame);

	public EvDecimal newlastFrame;
	
	public abstract Collection<Double> adjustScale(ModelWindow w);
	public abstract Collection<Vector3d> autoCenterMid();
	public abstract Double autoCenterRadius(Vector3d mid, double FOV);
	
//	public abstract void startBuildThread(EvDecimal frame, HashMap<EvChannel, VoxelExtension.ChannelSelection> chsel,ModelWindow w);

//	public EvDecimal lastframe=null;

	
	public boolean newisReady=false;
	public boolean outOfDate=false;

	//The new 3-step design
	//Stacks are created once, rendered a few times, disposed
	public abstract boolean newCreate(ProgressMeter pm, EvDecimal frame, HashMap<EvChannel, VoxelExtension.ChannelSelection> chsel,ModelWindow w);
	public abstract void loadGL(GL gl);
	public abstract void render(GL gl,List<TransparentRender> transparentRenderers, Camera cam, boolean solidColor, boolean drawEdges, boolean mixColors);
	public abstract void clean(GL gl);

	protected boolean stopBuildThread=false;
	public void stopCreate()
		{
		stopBuildThread=true;
		}

	
	
	
	/**
	 * Render the edges of the volume, assuming it is cube-like
	 */
	public void renderEdge(GL2 gl, double w, double h, double d)
		{
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glColor3f(0, 0, 1);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(w, 0, 0);
		gl.glVertex3d(w, h, 0);
		gl.glVertex3d(0, h, 0);
		gl.glEnd();
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(0, 0, d);
		gl.glVertex3d(w, 0, d);
		gl.glVertex3d(w, h, d);
		gl.glVertex3d(0, h, d);
		gl.glEnd();
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(0, 0, 0);	gl.glVertex3d(0, 0, d);
		gl.glVertex3d(w, 0, 0);	gl.glVertex3d(w, 0, d);
		gl.glVertex3d(w, h, 0);	gl.glVertex3d(w, h, d);
		gl.glVertex3d(0, h, 0);	gl.glVertex3d(0, h, d);
		gl.glEnd();
		}
	
	
	
	}
