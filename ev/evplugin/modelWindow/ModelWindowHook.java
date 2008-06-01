package evplugin.modelWindow;

import java.util.Collection;
import java.util.List;

import javax.media.opengl.GL;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import evplugin.data.EvObject;
//import evplugin.ev.Vector3D;


/**
 * Hook, inserted by model window extensions into each model window instance
 * @author Johan Henriksson
 */
public interface ModelWindowHook
	{
	public void readPersonalConfig(Element e);
	public void savePersonalConfig(Element e);
	
	public boolean canRender(EvObject ob);
	
	public void displayInit(GL gl);
	public void displaySelect(GL gl);
	public void displayFinal(GL gl,List<TransparentRender> transparentRenderers);

	
	public void fillModelWindomMenus();
	
	public Collection<Double> adjustScale();
	public Collection<Vector3d> autoCenterMid();
	public Collection<Double> autoCenterRadius(Vector3d mid, double FOV);
	
	public void datachangedEvent();
	
	}
