package evplugin.data;

import java.util.*;
import java.io.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import evplugin.basicWindow.*;
import evplugin.data.cmd.*;
import evplugin.ev.*;
import evplugin.script.*;

public abstract class EvData
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	public static void initPlugin() {}
	static
		{
		Script.addCommand("loadostxml", new CmdLoadOSTXML());
		Script.addCommand("dls",  new CmdDLS());
		Script.addCommand("dsel", new CmdDSEL());
		Script.addCommand("dunl", new CmdDUNL());
		Script.addCommand("msel", new CmdMSEL());
		Script.addCommand("mls",  new CmdMLS());
		BasicWindow.addBasicWindowExtension(new EvDataBasic());
		}
	
	public static TreeMap<String,EvObjectType> extensions=new TreeMap<String,EvObjectType>();
	public static Vector<EvData> metadata=new Vector<EvData>();

	public static void addMetadata(EvData m)
		{
		metadata.add(m);
		}

	
	
	public static int selectedMetadataId=-1;

	/**
	 * Get currently selected metadata or null
	 */
	public static EvData getSelectedMetadata()
		{
		if(selectedMetadataId>=0 && selectedMetadataId<metadata.size())
			return metadata.get(selectedMetadataId);
		else
			return null;
		}
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	/** All meta objects */
	public HashMap<Integer,EvObject> metaObject=new HashMap<Integer,EvObject>();

	/** Flag if the metadata container itself has been modified */
	private boolean coreMetadataModified=false;

	
	public int selectedMetaobjectId=-1;
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	/** Remember last path used to load an imageset */
	public static String lastDataPath="/Volumes/TBU_xeon01_data/johan_x1/daemonoutput/";

	/**
	 * Get currently selected metadata or null
	 */
	public EvObject getSelectedMetaobject()
		{
		return metaObject.get(selectedMetaobjectId);
		}

	
	
	
	/**
	 * Get a name description of the metadata
	 */
	public abstract String getMetadataName();
	
	/**
	 * Save metadata
	 */
	public abstract void saveMeta();
	
	
	/**
	 * Set if metadata has been modified
	 */
	public void setMetadataModified(boolean flag)
		{
		if(flag)
			coreMetadataModified=true;
		else
			{
			coreMetadataModified=false;
			for(EvObject ob:metaObject.values())
				ob.metaObjectModified=false;
			}
		}
	
	/**
	 * Check if the metadata or any object has been modified
	 */
	public boolean isMetadataModified()
		{
		boolean modified=coreMetadataModified;
		for(EvObject ob:metaObject.values())
			modified|=ob.metaObjectModified;
		return modified;
		}
	
	/**
	 * Get a meta object by ID
	 */
	public EvObject getMetaObject(int i)
		{
		return metaObject.get(i);
		}
	
	/**
	 * Put a meta object into the collection
	 */
	public void addMetaObject(EvObject o)
		{
		int i=1;
		while(metaObject.get(i)!=null)
			i++;
		metaObject.put(i, o);
		}
	
	/**
	 * Load metadata from XML-file
	 * @param filename Name of file
	 */
	public void loadXmlMetadata(String filename)
		{
		metaObject.clear();
    Document document = null;
    try 
    	{
  		FileInputStream fileInputStream = new FileInputStream(filename);
  		SAXBuilder saxBuilder = new SAXBuilder();
  		document = saxBuilder.build(fileInputStream);
  		Element element = document.getRootElement();

  		//Extract objects
  		List children=element.getChildren(); //TODO parameterize
  		for(Object ochild:children)
  			{
  			Element child=(Element)ochild;
  			EvObjectType ext=extensions.get(child.getName());
  			EvObject o;
  			if(ext==null)
  				{
  				o=new CustomObject(child);
  				Log.printLog("Found unknown meta object of type "+child.getName());
  				}
  			else
  				{
  				o=ext.extractObjects(child);
  				Log.printLog("Found meta object of type "+child.getName());
					}
  			String sid=child.getAttributeValue("id");
  			int id;
  			if(sid==null) //This is only needed for imagesets without the EV extended attributes
  				id=-1;
  			else
  				id=Integer.parseInt(sid);
 				metaObject.put(id, o);
  			}
    	} 
    catch (Exception e) 
    	{
    	e.printStackTrace();
    	} 
		}

	
	/**
	 * Put all meta objects into an XML document
	 */
	public Document saveXmlMetadata() //root name
		{
		Element ostElement=new Element("ost");
		Document doc = new Document(ostElement);
		for(int id:metaObject.keySet())
			{
			EvObject o=metaObject.get(id);
			Element el=new Element("TEMPNAME");
			el.setAttribute("id",""+id);
			o.saveMetadata(el);
			ostElement.addContent(el);
			}
		return doc;
		}
	
	
	/**
	 * Write XML-document to disk
	 */
	public static void writeXmlData(Document doc, File file)
		{
		try 
			{
			Format format=Format.getPrettyFormat();
			XMLOutputter outputter = new XMLOutputter(format);
			FileWriter writer = new FileWriter(file);
			FileOutputStream writer2=new FileOutputStream(file);
			outputter.output(doc, writer2);
			writer.close();
			
			//This is for the backup utility; "touch" all directories below
			touchRecursive(file, System.currentTimeMillis());
			} 
		catch (java.io.IOException e) 
			{
			e.printStackTrace();
			}
		}
	
	public static void touchRecursive(File f, long timestamp)
		{
		f.setLastModified(timestamp);
		File parent=f.getParentFile();
		if(parent!=null)
			touchRecursive(parent,timestamp);
		}
	
	}
