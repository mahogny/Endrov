package evplugin.modelWindow;

import java.util.Collection;

import javax.media.opengl.*;
import org.jdom.*;

import evplugin.data.EvObject;
import evplugin.ev.*;


/**
 * 
 * @author Johan Henriksson
 */
public interface ModelWindowHook
	{
	public void readPersonalConfig(Element e);
	public void savePersonalConfig(Element e);
	
	public boolean canRender(EvObject ob);
	
	public void displayInit(GL gl);
	public void displaySelect(GL gl);
	public void select(int id);
	public void displayFinal(GL gl);

	
	public void fillModelWindomMenus();
	
	public Collection<Double> adjustScale();
	public Collection<Vector3D> autoCenterMid();
	public Collection<Double> autoCenterRadius(Vector3D mid, double FOV);
	
	public void datachangedEvent();
	}
