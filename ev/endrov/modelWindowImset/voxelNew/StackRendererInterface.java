/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindowImset.voxelNew;

import java.awt.Color;
import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Vector3d;

import endrov.imageset.EvChannel;
import endrov.imageset.Imageset;
import endrov.modelWindow.ModelWindow;
import endrov.modelWindow.TransparentRender;
import endrov.util.EvDecimal;
import endrov.util.ProgressHandle;
import endrov.modelWindow.ModelWindow.ProgressMeter;
import endrov.modelWindow.gl.GLCamera;

/**
 * General interface to any stack renderer
 * @author Johan Henriksson
 */
public abstract class StackRendererInterface
	{
	/**
	 * Settings for one channel in an imageset
	 */
	public static class ChannelSelection
		{
		public Imageset im;
		public EvChannel ch;
		public ChanProp prop;
		
		public String toString()
			{
			return "prop: "+prop;
			}
		}

	public static class ChanProp
		{
		public Color color;//=new Color(0,0,0); //TODO should be possible to change afterwards. also contrast & brightness
		public double contrast;
		public double brightness;
		
		public String toString()
			{
			return "col: "+color+"   cb: "+contrast+"/"+brightness;
			}
		}



	public EvDecimal newlastFrame;
	
	public abstract Collection<Double> adjustScale(ModelWindow w);
	public abstract Collection<Vector3d> autoCenterMid();
	public abstract double autoCenterRadius(Vector3d mid);
	
	public boolean newisReady=false;
	public boolean outOfDate=false;

	//The new 3-step design
	//Stacks are created once, rendered a few times, disposed
	
	//TODO now loading all stacks - but not always needed!
	
	//TODO better to use stack than frame?
	//TODO this does not work! might select a channel multiple times!!!!
	public abstract boolean newCreate(ProgressHandle progh, ProgressMeter pm, EvDecimal frame, List<StackRendererInterface.ChannelSelection> chsel,ModelWindow w);
	public abstract void loadGL(GL gl);
	public abstract void render(GL gl,List<TransparentRender> transparentRenderers, GLCamera cam,
			boolean solidColor, boolean drawEdges, boolean mixColors);
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
