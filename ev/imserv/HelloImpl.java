package imserv;

//http://java.sun.com/j2se/1.5.0/docs/api/javax/rmi/ssl/package-summary.html
//http://java.sun.com/j2se/1.5.0/docs/guide/rmi/socketfactory/SSLInfo.html
//http://java.sun.com/j2se/1.5.0/docs/guide/security/jsse/samples/index.html

import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class HelloImpl extends UnicastRemoteObject implements Hello 
	{
	static final long serialVersionUID=0;

	private static final int PORT = 2020;
	
	public HelloImpl() throws Exception 
		{
		super(PORT,
				new RMISSLClientSocketFactory(),
				new RMISSLServerSocketFactory());
		}
	
	public String sayHello() 
		{
		return "Hello World!";
		}
	
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
			registry.bind("HelloServer", new HelloImpl());
	
			System.out.println("HelloServer bound in registry");
			} 
		catch (Exception e) 
			{
			System.out.println("HelloImpl err: " + e.getMessage());
			e.printStackTrace();
			}
		}
	}
	
