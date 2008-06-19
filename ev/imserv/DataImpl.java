package imserv;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
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
	private File file;
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
				if(!child.getName().startsWith(".") && child.isDirectory())
					channels.add(child.getName());
			
			rmd=new File(file,"rmd.ostxml");
			
			
			}
		else if(isOSTXML())
			rmd=file;

		if(rmd!=null)
			{
			
			XMLReader xr = XMLReaderFactory.createXMLReader();
			MySAXApp handler = new MySAXApp();
			xr.setContentHandler(handler);

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


	
	
	private byte[] generateOSTThumb()
		{
		
		
		
		BufferedImage im=new BufferedImage(80,80,BufferedImage.TYPE_3BYTE_BGR);
		Graphics g=im.getGraphics();
		g.setColor(Color.BLUE);
		g.fillRect(10, 10, 10, 10);
		g.drawString(name, 20, 40);
		return SendFile.getBytesFromImage(im);
		}
	
	public byte[] getThumb() throws Exception
		{
		//OST3
		if(file.isDirectory())
			{
			File thumbfile=new File(new File(file,"data"),"imserv.png");
			if(thumbfile.exists())
				return SendFile.getBytesFromFile(thumbfile);
			
			//Generate thumb and save down
			
			
			}
		
		//Not supported, return NULL. really.
		
		
		
		return generateOSTThumb();
		//or from file
		}
	
	
	
	
	
	
	
	
	
	/**
	 * Simplified XML reader.
	 * The point is that only a small subset of the data is needed and we want reading to be lightning fast.
	 * Hence a SAX reader is used to skip the tree building step, and filter out everything uninteresting.
	 * 
	 */
	public class MySAXApp extends DefaultHandler
		{
		int level=0;
		boolean inImserv=false;
		
		public MySAXApp ()
			{
			super();


			}


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
