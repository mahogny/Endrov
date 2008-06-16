package imserv;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


//javac *.java
//rmic HelloImpl
//java -Djava.security.policy=policy HelloImpl
//java HelloClient (run in another window)

//-Djavax.net.ssl.keyStore=testkeys -Djavax.net.ssl.keyStorePassword=passphrase


public class Client 
	{
	
	private static final int PORT = 2020;
	
	public static void main(String args[]) 
		{
		try 
			{
			// Make reference to SSL-based registry
			Registry registry = LocateRegistry.getRegistry(InetAddress.getLocalHost().getHostName(), PORT,	new RMISSLClientSocketFactory());
	
			ImservIF obj = (ImservIF) registry.lookup("imserv");
	
			String message = "blank";
			message = obj.sayHello();
			System.out.println(message+"\n");
			
			DataIF data=obj.getData("foo");
			data.print();
			
			} 
		catch (Exception e) 
			{
			System.out.println("HelloClient exception: " + e.getMessage());
			e.printStackTrace();
			}
		}
	}
