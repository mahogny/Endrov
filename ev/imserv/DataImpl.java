package imserv;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.rmi.server.UnicastRemoteObject;



/**
 * Server "data" object: implementation
 * 
 * @author Johan Henriksson
 */
public class DataImpl extends UnicastRemoteObject implements DataIF//, Comparable<DataImpl>
	{
	public static final long serialVersionUID=0;
	private String name;
	
	public DataImpl(String name) throws Exception 
		{
		super(Daemon.PORT,	new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
		this.name=name;
		}
	
	public void print()
		{
		System.out.println("server hello"+ name);
		}
	
	
	public String getName()
		{
		return name;
		}


	
	public byte[] getThumb() throws Exception
		{
		BufferedImage im=new BufferedImage(80,80,BufferedImage.TYPE_3BYTE_BGR);
		Graphics g=im.getGraphics();
		g.setColor(Color.BLUE);
		g.fillRect(10, 10, 10, 10);
		g.drawString(name, 20, 40);
		return SendFile.getBytesFromImage(im);
		}
	
	}
