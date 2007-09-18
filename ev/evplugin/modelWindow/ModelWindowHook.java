package evplugin.modelWindow;

import javax.media.opengl.*;
import evplugin.ev.*;

/**
 * 
 * @author Johan Henriksson
 */
public interface ModelWindowHook
	{
	public void displayInit(GL gl);
	public void displaySelect(GL gl);
	public void select(int id);
	public void displayFinal(GL gl);

	public void adjustScale();
	
	public Vector3D autoCenterMid();
	public Double autoCenterRadius(Vector3D mid, double FOV);
	}
