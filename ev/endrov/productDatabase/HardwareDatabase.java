package endrov.productDatabase;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

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
	public static LinkedList<Entry> entries=new LinkedList<Entry>();
	
	
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

			Entry dbe=new Entry();
			entries.add(dbe);
			
			for(Object m:oe.getAttributes())
				{
				Attribute ma=(Attribute)m;
				dbe.property.put(ma.getName(), ma.getValue());
				}
			
			}
		}
	
	
	
	
	/**
	 * One entry in the database
	 * @author Johan Henriksson
	 *
	 */
	public static class Entry
		{
		/**
		 * Properties, Name -> Value
		 */
		public HashMap<String, String> property=new HashMap<String, String>();
		
		public String getManufacturer()
			{
			return property.get("Manufacturer");
			}
		
		public String getModel()
			{
			return property.get("Model");
			}
		
		public String getModelNumber()
			{
			return property.get("ModelNumber");
			}
		
		/**
		 * Wavelength -> fraction transmitted
		 */
		public Map<Double,Double> getTransmittance()
			{
			return new TreeMap<Double, Double>();
			//TODO
			}
		
		}
	
	
	
	
	
	}
