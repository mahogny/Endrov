package imserv;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;



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
	
	public DataImpl(String name, File file) throws Exception 
		{
		super(Daemon.PORT,	new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
		this.name=name;
		this.file=file;
		
		
		
		}
	
	
	
	public String getName()
		{
		return name;
		}


	
	
	private byte[] generateOSTThumb()
		{
		List<String> channels=new LinkedList<String>();
		for(File child:file.listFiles())
			if(!child.getName().startsWith(".") && child.isDirectory())
				channels.add(child.getName());
		
		
		
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
	
	}
