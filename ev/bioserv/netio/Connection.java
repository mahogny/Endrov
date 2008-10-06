package bioserv.netio;

import java.io.IOException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Connection
	{

	
	
	
	public Connection(String host, int port) throws IOException
		{
	
		SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
		//Can disable encryption if wanted. both sides need to agree in that case
		
	//user/pass auth possible here
		
		
		
		
		
		}
	}
