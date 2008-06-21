package evplugin.imagesetImserv.service;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ImservConnection
	{
	public Registry registry;
	public ImservIF imserv;
	public ClientSessionIF session;
	
	
	
	public static ImservConnection connect(String host, int port)
		{
		try 
			{
			ImservConnection conn=new ImservConnection();
			
			
			conn.registry = LocateRegistry.getRegistry(InetAddress.getLocalHost().getHostName(), Daemon.PORT,	new RMISSLClientSocketFactory());
			conn.imserv = (ImservIF) conn.registry.lookup("imserv");
			conn.session=conn.imserv.auth("user", "pass");
			if(conn.session!=null)
				return conn;
			} 
		catch (Exception e) 
			{
			System.out.println("HelloClient exception: " + e.getMessage());
			e.printStackTrace();
			}
		return null;
		}
	}
