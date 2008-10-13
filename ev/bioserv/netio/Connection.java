package bioserv.netio;

import java.io.IOException;
import java.util.LinkedList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Connection
	{

	
	
	
	public Connection(String host, int port) throws IOException
		{
	
		SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
		
		
		
//		socket.startHandshake();
	//	java.security.cert.Certificate[] serverCerts = socket.getSession().getPeerCertificates();

		
		//Can disable encryption if wanted. both sides need to agree in that case
		
	//user/pass auth possible here
		//can be done with RMI
		
		
		
		
		}
	}
