package evplugin.modelWindow.voxel;

import java.util.Collection;
import java.util.HashMap;

import javax.media.opengl.GL;

import evplugin.ev.Vector3D;
import evplugin.imageset.Imageset.ChannelImages;
import evplugin.modelWindow.Camera;
import evplugin.modelWindow.ModelWindow;

/**
 * General interface to any stack renderer
 * @author Johan Henriksson
 */
public abstract class StackInterface
	{
	public abstract boolean needSettings(double frame);
	public abstract void setLastFrame(double frame);
	public abstract void clean(GL gl);
	public abstract void loadGL(GL gl);
	public abstract void render(GL gl, Camera cam, boolean solidColor, boolean drawEdges, boolean mixColors);
	public abstract Collection<Double> adjustScale(ModelWindow w);
	public abstract Collection<Vector3D> autoCenterMid();
	public abstract Double autoCenterRadius(Vector3D mid, double FOV);
	public abstract void startBuildThread(double frame, HashMap<ChannelImages, VoxelExtension.ChannelSelection> chsel,ModelWindow w);
	public abstract void stopBuildThread();

	
	/**
	 * Render the edges of the volume, assuming it is cube-like
	 */
	public void renderEdge(GL gl, double w, double h, double d)
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
