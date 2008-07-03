package bioserv;

import java.io.*;
import java.net.*;
import java.rmi.server.*;
import javax.net.ssl.*;

import java.security.KeyStore;

public class RMISSLServerSocketFactory implements RMIServerSocketFactory 
	{
	private SSLServerSocketFactory ssf = null;
	
	public RMISSLServerSocketFactory() throws Exception 
		{
		try 
			{
			char[] passphrase = "passphrase".toCharArray();
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(BioservDaemon.class.getResource("imservkeys").getFile()), passphrase);
			
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, passphrase);
		
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(kmf.getKeyManagers(), null, null);
		
			ssf = ctx.getServerSocketFactory();
			} 
		catch (Exception e) 
			{
			e.printStackTrace();
			throw e;
			}
		}
	
	public ServerSocket createServerSocket(int port) throws IOException 
		{
		return ssf.createServerSocket(port);
		}
	
	public int hashCode() 
		{
		return getClass().hashCode();
		}
	
	public boolean equals(Object obj) 
		{
		if (obj == this) 
			return true;
		else if (obj == null || getClass() != obj.getClass()) 
			return false;
		return true;
		}
	}
	
