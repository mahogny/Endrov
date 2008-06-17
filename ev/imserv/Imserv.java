package imserv;

import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Imserv
	{

	public static final int PORT = 2020;

	public static void main(String args[]) 
		{
		// Create and install a security manager
		if (System.getSecurityManager() == null) 
			System.setSecurityManager(new RMISecurityManager());
	
		try 
			{
			// Create SSL-based registry
			Registry registry = LocateRegistry.createRegistry(PORT, new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
	
			// Bind this object instance to the name "HelloServer"
			registry.bind("imserv", new ImservImpl());
	
			System.out.println("HelloServer bound in registry");
			} 
		catch (Exception e) 
			{
			System.out.println("HelloImpl err: " + e.getMessage());
			e.printStackTrace();
			}
		}
	}
