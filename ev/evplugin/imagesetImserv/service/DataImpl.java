package evplugin.imagesetImserv.service;

import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import evplugin.ev.EV;
import evplugin.ev.EvXMLutils;
import evplugin.imagesetOST.OstImageset;



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
public class DataImpl extends UnicastRemoteObject implements DataIF//, Comparable<DataImpl>
	{
	public static final long serialVersionUID=0;
	private String name;
	public File file;
	public Set<String> channels=new HashSet<String>();
	public Set<String> tags=new HashSet<String>();
	public Set<String> objs=new HashSet<String>();
	public Map<String,String> attrs=new HashMap<String, String>();
	private final Daemon daemon;
	
	public DataImpl(Daemon daemon, String name, File file) throws Exception 
		{
		super(Daemon.PORT,	new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
		this.name=name;
		this.file=file;
		this.daemon=daemon;

		//Collect information about dataset
		File rmd=null;
		if(isOST3())
			{
			//Channels
			for(File child:file.listFiles())
				if(!child.getName().startsWith(".") && child.isDirectory() && !child.getName().equals("data"))
					channels.add(child.getName());
			rmd=new File(file,"rmd.ostxml");
			}
		else if(isOSTXML())
			rmd=file;

		if(rmd!=null)
			{
			XMLReader xr = XMLReaderFactory.createXMLReader();
			PartialRMDreader handler = new PartialRMDreader();
			xr.setContentHandler(handler);

			if(rmd.exists())
				xr.parse(new InputSource(new FileReader(rmd)));
			}
		
		readImservFile();
		}
	
	
	//One could make several implementations, depending on type
	private boolean isOST3(){return file.isDirectory();}
	private boolean isOSTXML(){return file.isFile() && file.getName().endsWith(".ostxml");}
	
	

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
		return tm.getThumb(this,file);
		}
	
	
	
	
	/**
	 * Get image listing. This is the same as the image cache.
	 */
	public CompressibleDataTransfer getImageList() throws Exception
		{
		if(!isOST3())
			return null;
		else
			{			
			//Load OST and generate listing if needed
			File imcacheFile=new File(file,"imagecache.txt");
			if(!imcacheFile.exists())
				{
				//Create
				OstImageset rec=new OstImageset(file.getPath());
				rec.loadDatabaseCache(); //This is an ugly hack. Constructor creates cachefile, but java might not wait for completion
				}
			if(imcacheFile.exists())
				return getUncompressed(imcacheFile);
			else
				return null;
			}
		}
	
	/**
	 * Get File for imagecache
	 */
	private File getImageCacheFile()
		{
		if(isOST3())
			{
			File imcacheFile=new File(file,"imagecache.txt");
			if(imcacheFile.exists())
				return imcacheFile;
			}
		return null;
		}

	/**
	 * Get metadata
	 */
	public synchronized CompressibleDataTransfer getRMD() throws Exception
		{
		File rmdFile;
		if(isOST3())
			rmdFile=new File(file,"rmd.ostxml");
		else if(isOSTXML())
			rmdFile=file;
		else
			return null;
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
	public synchronized ImageTransfer getImage(String channel, int frame, int z) throws Exception
		{
		File thefile=getImageFile(channel, frame, z);
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
	private File constructImageFile(String channel, int frame, int z, String end)
		{
		//dangerous format!
		File chandir=new File(file,channel);
		File framedir=new File(chandir,EV.pad(frame,8));
		return new File(framedir,EV.pad(z,8)+end);
		}
	
	/**
	 * Get file for image.
	 * Can it be made faster by guessing name?
	 */
	private File getImageFile(String channel, int frame, int z)
		{
		if(isOST3())
			{
			//Note: dangerous channel
			File chandir=new File(file,channel);
			File framedir=new File(chandir,EV.pad(frame,8));
			final String sz=EV.pad(z,8)+".";
			System.out.println("frame "+framedir+" "+sz);
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
	public synchronized void setRMD(CompressibleDataTransfer data) throws Exception
		{
		File rmdFile;
		if(isOST3())
			rmdFile=new File(file,"rmd.ostxml");
		else if(isOSTXML())
			rmdFile=file;
		else
			return;

//		if(data.compression==CompressibleDataTransfer.NONE)
		FileOutputStream fo=new FileOutputStream(rmdFile);
		fo.write(data.data);
		fo.close();
		}

	/**
	 * Set image
	 */
	public synchronized void putImage(String channel, int frame, int z, ImageTransfer data) throws Exception
		{
		if(isOST3())
			{
			File oldFile=getImageFile(channel, frame, z);
			if(oldFile.exists())
				oldFile.delete();
			File newFile=constructImageFile(channel, frame, z, data.format);
			newFile.getParentFile().mkdirs();
			FileOutputStream fo=new FileOutputStream(newFile);
			fo.write(data.data);
			fo.close();
			File imagecache=getImageCacheFile();
			if(imagecache!=null)
				imagecache.delete();
			}
		}
	
	public synchronized void setTag(String tag, boolean enable) throws Exception
		{
		if(enable)
			{
			daemon.getMapCreate(tag, daemon.tags).add(this);
			tags.add(tag);
			}
		else
			{
			daemon.getMapCreate(tag, daemon.tags).remove(this);
			tags.remove(tag);
			}
		writeImservFile();
		daemon.setLastUpdate();
		}

	
	
	private File getImservFile()
		{
		return new File(file.getParent(),file.getName()+".imserv");
		}
	private void readImservFile()
		{
		try
			{
			XMLReader xr = XMLReaderFactory.createXMLReader();
			ImservReader handler = new ImservReader();
			xr.setContentHandler(handler);
			File f=getImservFile();
			if(f.exists())
				xr.parse(new InputSource(new FileReader(f)));
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
			for(String tag:tags)
				{
				Element e=new Element("tag");
				e.setAttribute("name", tag);
				root.addContent(e);
				}
			EvXMLutils.writeXmlData(doc, getImservFile());
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
				tags.add(atts.getValue(atts.getIndex("name")));
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
			level++;
			//if(inImserv && name.equals("tag"))
			//	tags.add(atts.getValue(atts.getIndex("name")));
			if(level==2)
				{
				System.out.println(name);
				//if(name.equals("imserv"))
				//	inImserv=true;
				objs.add(name);
				}
			}

		public void endElement (String uri, String name, String qName)
			{
			//if(level==2)
			//	inImserv=false;
			level--;
			}
		}
	 
	}
