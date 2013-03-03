/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ioOST;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Document;

import endrov.data.EvData;
import endrov.data.EvIODataReaderWriterDeclaration;
import endrov.data.EvIOData;
import endrov.data.RecentReference;
import endrov.util.collection.Tuple;
import endrov.util.io.EvXmlUtil;


/**
 * Metadata stored in an ordinary XML-file
 * @author Johan Henriksson
 */
public class EvIODataXML implements EvIOData
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	
	
	public File filename=null;
	
	public String getMetadataName()
		{
		if(filename==null)
			return "(Unnamed XML)";
		else
			return filename.getName();
		}
	public String toString()
		{
		return getMetadataName();
		}

	
	/*public EvIODataXML()
		{
		}*/
	
	public EvIODataXML(EvData d, String filename)
		{
		this.filename=new File(filename);
		}
	

	
	/**
	 * Save data
	 */
	public void saveData(EvData d, EvData.FileIOStatusCallback cb)
		{
		Document document=d.saveXmlMetadata();
		try
			{
			EvXmlUtil.writeXmlData(document, filename);
			d.setMetadataNotModified();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	
	public void buildDatabase(EvData d)
		{
		d.metaObject.clear();
		d.loadXmlMetadata(filename);
		}
	
	
	
	public File datadir()
		{
		return filename.getParentFile();
		}
	
	public RecentReference getRecentEntry()
		{
		return new RecentReference(getMetadataName(), filename.getAbsolutePath());
		}

	
	public void close() throws IOException
		{
		}



	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{		
		//OST XML-support
		EvData.supportedFileFormats.add(new EvIODataReaderWriterDeclaration(){
			public Integer loadSupports(String fileS)
				{
				File file=new File(fileS);
				return file.isFile() && (/*file.getName().endsWith(".xml") ||*/
						file.getName().endsWith(".ostxml")) ? 10 : null;
				}
			public List<Tuple<String,String[]>> getLoadFormats()
				{
				LinkedList<Tuple<String,String[]>> formats=new LinkedList<Tuple<String,String[]>>(); 
				formats.add(new Tuple<String, String[]>("OST XML",new String[]{".ostxml"}));
				return formats;
				}
			public EvData load(String file, EvData.FileIOStatusCallback cb) throws Exception
				{
				EvData d=new EvData();
				EvIODataXML io=new EvIODataXML(d,file);
				d.io=io;
				io.buildDatabase(d);
				return d;
				}
			public Integer saveSupports(String file){return loadSupports(file);}
			public List<Tuple<String,String[]>> getSaveFormats(){return getLoadFormats();}
			public EvIOData getSaver(EvData d, String file) throws IOException
				{
				return new EvIODataXML(d,file);
				}

			
		});
		}
	
	}
