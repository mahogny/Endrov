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
public interface StackInterface
	{
	public boolean needSettings(double frame);
	public void setLastFrame(double frame);
	public void clean(GL gl);
	public void loadGL(GL gl);
	public void render(GL gl, Camera cam);
	public Collection<Double> adjustScale(ModelWindow w);
	public Collection<Vector3D> autoCenterMid();
	public Double autoCenterRadius(Vector3D mid, double FOV);
	
	
	public void startBuildThread(double frame, HashMap<ChannelImages, VoxelExtension.ChannelSelection> chsel,ModelWindow w);
	public void stopBuildThread();

	}
