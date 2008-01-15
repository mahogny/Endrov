package evplugin.modelWindow;

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

	public void adjustScale();
	
	public void fillModelWindomMenus();
	
	public Vector3D autoCenterMid();
	public Double autoCenterRadius(Vector3D mid, double FOV);
	}
