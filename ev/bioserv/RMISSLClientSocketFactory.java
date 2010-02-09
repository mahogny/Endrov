/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv;

//can be replaced by
//javax.rmi.ssl.SslRMIClientSocketFactory and javax.rmi.ssl.SslRMIServerSocketFactory


import java.io.*;
import java.net.*;
import java.rmi.server.*;
import javax.net.ssl.*;

public class RMISSLClientSocketFactory implements RMIClientSocketFactory, Serializable 
	{
	static final long serialVersionUID=0;
	
	public Socket createSocket(String host, int port) throws IOException
		{
		SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
		
		//user/pass auth possible here
		
		return socket;
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
	
