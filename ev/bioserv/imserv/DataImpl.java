/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.imserv;

import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import org.jdom.Document;
import org.jdom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import bioserv.BioservDaemon;
import bioserv.RMISSLClientSocketFactory;
import bioserv.RMISSLServerSocketFactory;
import bioserv.SendFile;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.imagesetOST.EvIODataOST;
import endrov.util.EvDecimal;
import endrov.util.EvXmlUtil;



//Tag:foo
//Channel:RFP
//Type:OST
//Obj:NucLineage

//TODO possible race conditions. need to synchronize

/**
 * Server "data" object: implementation
 * 
 * @author Johan Henriksson
 */
public class DataImpl extends UnicastRemoteObject implements DataIF
	{
	public static final long serialVersionUID=0;
	private String name;
	public File fileRoot;
	public Map<String,Tag> tags=new TreeMap<String,Tag>();
	private final ImservImpl imserv;
	
	public DataImpl(ImservImpl imserv, String name, File file) throws Exception 
		{
		super(BioservDaemon.PORT,	new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
		this.name=name;
		this.fileRoot=file;
		this.imserv=imserv;

		//Collect information about dataset
		File rmd=null;
		if(isOST3())
			{
			//Channels
			for(File child:file.listFiles())
				if(!child.getName().startsWith(".") && child.isDirectory() && !child.getName().equals("data"))
					{
					String tagname="chan:"+child.getName();
					tags.put(tagname,new Tag(true,tagname));
					}
			rmd=new File(file,"rmd.ostxml");
			}
//		else if(isOSTXML())
	//		rmd=file;

		if(rmd!=null)
			{
			XMLReader xr = XMLReaderFactory.createXMLReader();
			PartialRMDreader handler = new PartialRMDreader();
			xr.setContentHandler(handler);

			if(rmd.exists())
				xr.parse(new InputSource(new FileInputStream(rmd)));
			}
		
		readImservFile();
		}
	
	
	//One could make several implementations, depending on type
	private boolean isOST3(){return fileRoot.isDirectory() && fileRoot.getName().endsWith(".ost");}
	

	/**
	 * Get name of dataset
	 */
	public String getName()
		{
		return name;
		}
	
	/**
	 * Get thumbnail of dataset
	 */
	public synchronized byte[] getThumb() throws Exception
		{
		ThumbMaker tm=new ThumbMaker();
		return tm.getThumb(this,fileRoot);
		}
	
	
	
	
	/**
	 * Get image listing. This is the same as the image cache.
	 */
	public CompressibleDataTransfer getImageCache(String blobid) throws Exception
		{
		File imcacheFile=getImageCacheFile(blobid);
		if(!imcacheFile.exists())
			{
			//Create
			System.out.println("No cache file, creating " + imcacheFile);
			EvIODataOST io=new EvIODataOST(fileRoot);
			io.initialLoad(new EvData(), EvData.deafFileIOCB);
			}

		if(!imcacheFile.exists())
			{
			System.out.println("Error: failed to create cache " + imcacheFile);
			return null;
			}
		else
			return getUncompressed(imcacheFile);
		}
	
	private boolean blobidIsSafe(String blobid)
		{
		boolean safe=blobid.indexOf("/")==-1 && blobid.indexOf('\\')==-1;
		if(!safe)
			System.out.println("unsafe! "+blobid);
		return safe;
		}
	
	/**
	 * Get File for imagecache
	 */
	private File getImageCacheFile(String blobid)
		{
		if(blobidIsSafe(blobid))
			{
			File imcacheFile=new File(new File(fileRoot,blobid), "imagecache.txt");
			return imcacheFile;
			}
		return null;
		}

	

	/**
	 * Get metadata
	 */
	public synchronized CompressibleDataTransfer getMetadata() throws Exception
		{
		File rmdFile=new File(fileRoot,"rmd.ostxml");
		if(rmdFile.exists())
			return getUncompressed(rmdFile);
		else
			return null;
		}

	/**
	 * Pack file as uncompressed into data structure
	 */
	private synchronized CompressibleDataTransfer getUncompressed(File file) throws Exception
		{
		CompressibleDataTransfer imlist=new CompressibleDataTransfer();
		imlist.compression=CompressibleDataTransfer.NONE;
		imlist.data=SendFile.getBytesFromFile(file);
		return imlist;
		}
	
	
	/**
	 * Get image data
	 */
	public synchronized ImageTransfer getImage(String blobid, String channel, EvDecimal frame, EvDecimal z) throws Exception
		{
		File thefile=getImageFile(blobid, channel, frame, z);
		if(thefile!=null)
			{
			ImageTransfer transfer=new ImageTransfer();
			transfer.format=fileTypeFromFile(thefile);
			transfer.data=SendFile.getBytesFromFile(thefile);
			return transfer;
			}
		else
			return null;
		}
	
	/**
	 * Get filename ending from file handle
	 */
	private static String fileTypeFromFile(File file)
		{
		String name=file.getName();
		int i=name.indexOf('.');
		if(i==-1)
			return "";
		else
			return name.substring(i);
		}
	
	/**
	 * Construct proper name of image file
	 */
	private File constructImageFile(String channel, EvDecimal frame, EvDecimal z, String end)
		{
		//dangerous format!
		File chandir=new File(fileRoot,"ch-"+channel);
		File framedir=new File(chandir,EV.pad(frame,8));
		return new File(framedir,EV.pad(z,8)+end);
		}
	
	/**
	 * Get file for image.
	 * Can it be made faster by guessing name?
	 */
	private File getImageFile(String blobid, String channel, EvDecimal frame, EvDecimal z)
		{
		if(blobidIsSafe(blobid) && blobidIsSafe(channel))
			{
			//Note: dangerous channel
			File blobdir=new File(fileRoot,blobid);
			File chandir=new File(blobdir,"ch-"+channel);
			File framedir=new File(chandir,EV.pad(frame,8));
			final String sz=EV.pad(z,8)+".";
//			System.out.println("frame "+framedir+" "+sz);
			System.out.println("looking for images in "+framedir);
			File zcand[]=framedir.listFiles(new FileFilter(){
				public boolean accept(File pathname){return pathname.getName().startsWith(sz);}});
			
			if(zcand.length>0)
				{
				File thefile=zcand[0];
				return thefile;
				}
			}
		return null;
		}
	
	/**
	 * Set metadata file
	 */
	public synchronized void setMetadata(CompressibleDataTransfer data) throws Exception
		{
		File rmdFile=new File(fileRoot,"rmd.ostxml");
//		if(data.compression==CompressibleDataTransfer.NONE)
		FileOutputStream fo=new FileOutputStream(rmdFile);
		fo.write(data.data);
		fo.close();
		}

	/**
	 * Set image data
	 */
	public synchronized void putImage(String blobid, String channel, EvDecimal frame, EvDecimal z, ImageTransfer data) throws Exception
		{
		File oldFile=getImageFile(blobid, channel, frame, z);
		if(oldFile.exists())
			oldFile.delete();
		File newFile=constructImageFile(channel, frame, z, data.format);
		newFile.getParentFile().mkdirs();
		FileOutputStream fo=new FileOutputStream(newFile);
		fo.write(data.data);
		fo.close();
		File imagecache=getImageCacheFile(blobid);
		if(imagecache!=null)
			imagecache.delete();
		}
	
	public synchronized void setTag(String tagname, String value, boolean enable) throws Exception
		{
		//Cannot overwrite virtual tags. Ignore this totally.
		Tag extag=tags.get(tagname);
		if(extag!=null && extag.virtual)
			return;
		
		//Change this record
		if(enable)
			{
			Tag tag=new Tag(false,tagname,value);
			tags.put(tagname,tag);
			imserv.internalAddTag(tagname, this);
			}
		else
			{
			tags.remove(tagname);
			imserv.internalRemoveTag(tagname, this);
			}
		//TODO: reload this data
		System.out.println(tagname+" "+enable);
		writeImservFile();
		imserv.setLastUpdate();
		}


	public Tag[] getTags() throws Exception
		{
		return tags.values().toArray(new Tag[]{});
		}

	
	
	
	
	
	private File getOldImservFile()
		{
		return new File(fileRoot.getParent(),fileRoot.getName()+".imserv");
		}
	private File getImservFile()
		{
		return new File(new File(new File(fileRoot.getParent(),fileRoot.getName()),"data"),"imserv.txt");
		}
	private void readImservFile()
		{
		try
			{
			XMLReader xr = XMLReaderFactory.createXMLReader();
			ImservReader handler = new ImservReader();
			xr.setContentHandler(handler);
			File f=getOldImservFile();
			if(f.exists())
				{
				getImservFile().getParentFile().mkdirs();
				f.renameTo(getImservFile());
				}
			f=getImservFile();
			if(f.exists())
				xr.parse(new InputSource(new FileInputStream(f)));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	private void writeImservFile()
		{
		try
			{
			Element root=new Element("imserv");
			Document doc=new Document(root);
			for(Map.Entry<String, Tag> tag:tags.entrySet())
				if(!tag.getValue().virtual)
					{
					Element e=new Element("tag");
					e.setAttribute("name", tag.getKey());
					if(tag.getValue().value!=null)
						e.setAttribute("name", tag.getValue().value);
					root.addContent(e);
					}
			EvXmlUtil.writeXmlData(doc, getImservFile());
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Read imserv XML file
	 */
	public class ImservReader extends DefaultHandler
		{
		public ImservReader(){super();}
		public void startElement (String uri, String name, String qName, Attributes atts)
			{
			if(name.equals("tag"))
				{
				String tagname=atts.getValue(atts.getIndex("name"));
				tags.put(tagname, new Tag(false,tagname,atts.getValue(atts.getIndex("value"))));
				}
			}
		}

	
	
	
	/**
	 * Simplified XML reader.
	 * The point is that only a small subset of the data is needed and we want reading to be lightning fast.
	 * Hence a SAX reader is used to skip the tree building step, and filter out everything uninteresting.
	 */
	public class PartialRMDreader extends DefaultHandler
		{
		int level=0;
		//boolean inImserv=false;
		
		public PartialRMDreader(){super();}

		public void startElement (String uri, String name,
				String qName, Attributes atts)
			{
			if(!name.equals("ostblobid"))
				level++;
			//if(inImserv && name.equals("tag"))
			//	tags.add(atts.getValue(atts.getIndex("name")));
			if(level==2)
				{
//				System.out.println(name);
//				int i=atts.getIndex("value");
				String tagname="obj:"+name;
	//			String value=i==-1 ? null : atts.getValue(i);
				//if(name.equals("imserv"))
				//	inImserv=true;
				tags.put(tagname,new Tag(true,tagname));
				}
			}

		public void endElement (String uri, String name, String qName)
			{
			//if(level==2)
			//	inImserv=false;
			if(!name.equals("ostblobid"))
				level--;
			}
		}
	 
	}
