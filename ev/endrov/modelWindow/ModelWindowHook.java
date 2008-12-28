package endrov.modelWindow;

import java.util.Collection;
import java.util.List;

import javax.media.opengl.GL;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.EvObject;
import endrov.util.EvDecimal;


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

	
	public void fillModelWindowMenus();
	
	public Collection<Double> adjustScale();
	public Collection<Vector3d> autoCenterMid();
	public Collection<Double> autoCenterRadius(Vector3d mid, double FOV);
	
	//These two could be put in a special class that EvContainer can implement.
	//it can then be shared with imagewindow
	public EvDecimal getFirstFrame();
	public EvDecimal getLastFrame();
	
	
	public void datachangedEvent();
	
	}
