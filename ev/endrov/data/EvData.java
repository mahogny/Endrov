/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Root of container tree, handler of types
 * @author Johan Henriksson
 */
public class EvData extends EvContainer
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	/**
	 * Supported file formats and associated routines to load it
	 */
	public static Vector<EvIODataReaderWriterDeclaration> supportedFileFormats=new Vector<EvIODataReaderWriterDeclaration>();
	
	/**
	 * Registered types of metadata
	 */
	public static Map<String,Class<? extends EvObject>> supportedMetadataFormats=Collections.synchronizedMap(new TreeMap<String,Class<? extends EvObject>>());
	
	/**
	 * Callback for current status on I/O (saving, loading)
	 * @author Johan Henriksson
	 */
	public interface FileIOStatusCallback
		{
		/**
		 * Tell current status. Fraction is within 0-1
		 */
		public void fileIOStatus(double frac, String text);
		}

	/**
	 * File I/O status callback: Doesn't do anything, to be used when status does not have to be presented
	 */
	public static FileIOStatusCallback deafFileIOCB=new FileIOStatusCallback(){
		public void fileIOStatus(double frac, String text){}
	};
	
	/******************************************************************************************************
	 *                               Loading                                                              *
	 *****************************************************************************************************/

	/** Load file by path/URI */
	public static EvData loadFile(String file){return loadFile(file,deafFileIOCB);}
	
	/** Load file by path */
	public static EvData loadFile(File file){return loadFile(file.getPath(),deafFileIOCB);}
	
	/** Load file by path, receive feedback on process */
	public static EvData loadFile(File file, FileIOStatusCallback cb){return loadFile(file.getPath(),cb);}

	/**
	 * Load file by path, receive feedback on process
	 */
	public static EvData loadFile(String file, FileIOStatusCallback cb)
		{
		EvIODataReaderWriterDeclaration thes=null;
		int lowest=0;
		for(EvIODataReaderWriterDeclaration s:EvData.supportedFileFormats)
			{
			Integer sup=s.loadSupports(file);
			if(sup!=null && (thes==null || lowest>sup))
				{
				thes=s;
				lowest=sup;
				}
			}
		if(thes!=null)
			{
			try
				{
				EvData data=thes.load(file, cb);
				if(cb!=null)
					cb.fileIOStatus(0, "Loading "+file);
				if(data!=null)
					return data;
				}
			catch (Exception e)
				{
				e.printStackTrace();
				return null;
				}
			}
		return null;
		}

	
	
	
	
	

	/******************************************************************************************************
	 *                               Saving                                                               *
	 *****************************************************************************************************/

	/** Save by file or URI */
	public void saveDataAs(String file) throws IOException {saveDataAs(file,deafFileIOCB);}

	/** Save by path */
	public void saveDataAs(File file) throws IOException {saveDataAs(file.getPath(),deafFileIOCB);}

	/**
	 * Point I/O to a new file, prepare for saving. This does not affect currently loaded data 
	 */
	public void setSaver(String file) throws IOException
		{
		EvIODataReaderWriterDeclaration thes=null;
		int lowest=0;
		for(EvIODataReaderWriterDeclaration s:EvData.supportedFileFormats)
			{
			Integer sup=s.saveSupports(file);
			if(sup!=null && (thes==null || lowest>sup))
				{
				thes=s;
				lowest=sup;
				}
			}
		if(thes!=null)
			{
			EvIOData io=thes.getSaver(this, file);
			if(io!=null)
				this.io=io;
			else
				throw new IOException("Plugin does not support saving this file");
			}
		else
			throw new IOException("No suitable plugin to save file");
		}
	
	/**
	 * Save file by path, receive feedback on process. Return if ok.
	 */
	public void saveDataAs(String file, FileIOStatusCallback cb) throws IOException
		{
		setSaver(file);
		saveData(cb);
		}


	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/



	/**
	 * Connection with disk for partial I/O etc
	 */
	public EvIOData io=null;

	/** Version of metadata */
	public String metadataVersion="0";


	public void saveData() throws IOException
		{
		saveData(deafFileIOCB);
		}
	
	public void saveData(EvData.FileIOStatusCallback cb) throws IOException
		{
		io.saveData(this, cb);
		}
	
	

	
	/******************************************************************************************************
	 *                               Instance: XML tools                                                  *
	 *****************************************************************************************************/

	
	/**
	 * Load metadata from XML-file
	 */
	public void loadXmlMetadata(File filename)
		{
		try
			{
			FileInputStream fileInputStream = new FileInputStream(filename);
			loadXmlMetadata(fileInputStream);
			}
		catch (FileNotFoundException e)
			{
			e.printStackTrace();
			}
		}
	
	
	
	public void loadXmlMetadata(InputStream is)
		{
		metaObject.clear();
    Document document = null;
    try 
    	{
  		SAXBuilder saxBuilder = new SAXBuilder();
  		document = saxBuilder.build(is);
  		Element element = document.getRootElement();

  		if(element.getAttribute("version")!=null)
  			metadataVersion=element.getAttributeValue("version");
  		
  		recursiveLoadMetadata(element);
    	} 
    catch (Exception e) 
    	{
    	e.printStackTrace();
    	} 
		}

	
	
	/**
	 * Put all meta objects into an XML document
	 */
	public Document saveXmlMetadata() 
		{
		Element ostElement=new Element("ost");
		ostElement.setAttribute("version","3.3");
		Document doc = new Document(ostElement);
//		save
		recursiveSaveMetadata(ostElement);
//		saveSubObjectsXML(ostElement);
		return doc;
		}

	
	
	/**
	 * Get the name of this metadata to be displayed in menus
	 */
	public String getMetadataName()
		{
		if(io==null)
			return "<unnamed>";
		else
			return io.getMetadataName();
		}
	
	public String toString()
		{
		return getMetadataName();
		}

	
	/**
	 * Get entry for the "Load Recent"-menu or null if not possible
	 */
	public RecentReference getRecentEntry()
		{
		if(io==null)
			return null;
		else
			return io.getRecentEntry();
		}


	}
