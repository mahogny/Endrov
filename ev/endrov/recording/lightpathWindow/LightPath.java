/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.lightpathWindow;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.hardware.EvDevicePath;


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
	private static final String metaType="lightpath";
	
	public Map<EvDevicePath,UnitInfo> units=new HashMap<EvDevicePath, UnitInfo>();
	
	public static class UnitInfo
		{
		public double x,y;
		}
	
	
	public static class Light
		{
		public EvDevicePath fromUnit, toUnit;
		public String fromConn, toConn;
		}


	public void buildMetamenu(JMenu menu, EvContainer parentObject)
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


	public String saveMetadata(Element e)
		{
		return metaType;
		}
	
	
	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}
	
	
	
	
	
	}
