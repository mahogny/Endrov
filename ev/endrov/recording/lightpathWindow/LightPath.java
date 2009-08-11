package endrov.recording.lightpathWindow;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvObject;
import endrov.hardware.DevicePath;


/**
 * Description of the microscope setup
 * 
 * 
 * Endrov persistent data object, as config?
 * product stuff could go here too. but I don't think so
 * 
 * @author Johan Henriksson
 *
 */
public class LightPath extends EvObject
	{

	
	public Map<DevicePath,UnitInfo> units=new HashMap<DevicePath, UnitInfo>();
	
	public static class UnitInfo
		{
		public double x,y;
		}
	
	
	public static class Light
		{
		public DevicePath fromUnit, toUnit;
		public String fromConn, toConn;
		}


	public void buildMetamenu(JMenu menu)
		{
		}


	public String getMetaTypeDesc()
		{
		return "LightPath";
		}


	public void loadMetadata(Element e)
		{
		// TODO Auto-generated method stub
		}


	public void saveMetadata(Element e)
		{
		// TODO Auto-generated method stub
		}
	
	
	
	
	
	
	
	}
