package evplugin.imagesetImserv.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import evplugin.ev.EV;



//Tag:foo
//Channel:RFP
//Type:OST
//Obj:NucLineage
//Attr:foo=bar

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
	
	public DataImpl(String name, File file) throws Exception 
		{
		super(Daemon.PORT,	new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
		this.name=name;
		this.file=file;

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
		
		}
	
	
	private boolean isOST3()
		{
		return file.isDirectory();
		}
	
	private boolean isOSTXML()
		{
		return file.isFile() && file.getName().endsWith(".ostxml");
		}
	
	

	
	public String getName()
		{
		return name;
		}


	
	
	
	public byte[] getThumb() throws Exception
		{
		ThumbMaker tm=new ThumbMaker();
		return tm.getThumb(this,file);
		}
	
	
	
	
	
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
				
				}
			if(imcacheFile.exists())
				return getUncompressed(imcacheFile);
			else
				return null;
			}
		}
	public CompressibleDataTransfer getRMD() throws Exception
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

	private CompressibleDataTransfer getUncompressed(File file) throws Exception
		{
		CompressibleDataTransfer imlist=new CompressibleDataTransfer();
		imlist.compression=CompressibleDataTransfer.NONE;
		imlist.data=SendFile.getBytesFromFile(file);
		return imlist;
		}
	
	
	
	public ImageTransfer getImage(String channel, int frame, int z) throws Exception
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
			ImageTransfer transfer=new ImageTransfer();
			transfer.format="awt";
			transfer.data=SendFile.getBytesFromFile(thefile);
			return transfer;
			}
		else
			return null;
		}
	
	
	
	public void setRMD(CompressibleDataTransfer data) throws Exception
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
	
	public void putImage(String channel, int frame, int z, ImageTransfer data) throws Exception
		{
		
		}
	
	
	
	
	/**
	 * Simplified XML reader.
	 * The point is that only a small subset of the data is needed and we want reading to be lightning fast.
	 * Hence a SAX reader is used to skip the tree building step, and filter out everything uninteresting.
	 */
	public class PartialRMDreader extends DefaultHandler
		{
		int level=0;
		boolean inImserv=false;
		
		public PartialRMDreader(){super();}

		public void startElement (String uri, String name,
				String qName, Attributes atts)
			{
			level++;
			if(inImserv && name.equals("tag"))
				tags.add(atts.getValue(atts.getIndex("name")));
			if(level==2)
				{
				System.out.println(name);
				if(name.equals("imserv"))
					inImserv=true;
				objs.add(name);
				}
			}

		public void endElement (String uri, String name, String qName)
			{
			if(level==2)
				inImserv=false;
			level--;
			}
		}

	
	
	
	}
