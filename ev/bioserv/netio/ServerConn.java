package bioserv.netio;

import java.io.IOException;
import java.util.LinkedList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class ServerConn
	{

	
	public static void main(String[] arg) 
		{
		try
			{
			int portnum=4321;
			
			SSLServerSocketFactory factory=(SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
			
			SSLServerSocket server = (SSLServerSocket)factory.createServerSocket(portnum);
			
			System.out.println(""+server.getNeedClientAuth()+" "+server.getWantClientAuth());
			
			//Allow anonymous handshake ie no certificate needed
			LinkedList<String> okCipher=new LinkedList<String>();
			for(String s:server.getSupportedCipherSuites())
				if(s.contains("_anon_"))
					okCipher.add(s);
			server.setEnabledCipherSuites(okCipher.toArray(new String[]{}));
		
			
			SSLSocket client = (SSLSocket) server.accept();
			
			
			
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}

		
		System.exit(0);
		}
	}
