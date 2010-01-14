/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.productDatabase;

import java.io.File;
import java.util.LinkedList;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import endrov.util.EvFileUtil;
import endrov.util.EvXmlUtil;

/**
 * Information about existing hardware
 * 
 * @author Johan Henriksson
 *
 */
public class HardwareDatabase
	{
	/**
	 * List of all known hardware
	 */
	public static LinkedList<HardwareMetadata> entries=new LinkedList<HardwareMetadata>();
	
	
	public static void initPlugin() {}
	static
		{
		try
			{
			readDatabase(EvFileUtil.getFileFromURL(HardwareDatabase.class.getResource("hardwareDatabase.xml")));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	/**
	 * Read file with hardware entries
	 */
	public static void readDatabase(File f) throws Exception
		{
		Document doc=EvXmlUtil.readXML(f);
		
		
		for(Object o:doc.getRootElement().getChildren())
			{
			Element oe=(Element)o;

			HardwareMetadata dbe=new HardwareMetadata();
			entries.add(dbe);
			
			for(Object m:oe.getAttributes())
				{
				Attribute ma=(Attribute)m;
				dbe.property.put(ma.getName(), ma.getValue());
				}
			
			}
		}
	
	
	
	
	
	}
