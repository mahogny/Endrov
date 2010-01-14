/**
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.biceps;

import java.io.*;
import java.util.LinkedList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;


/**
 * Listening connection. There is no login or anything in the protocol itself.
 * @author Johan Henriksson
 *
 */
public class Listener extends Thread
	{
	private SSLServerSocket socket;
	private IncomingManager mgr;
	
	/**
	 * Event generated whenever there is a new incoming connection
	 */
	public static interface IncomingManager
		{
		public void newConnection(RMImanager conn);
		}
		
	/**
	 * Listen on port, report new connections to mgr
	 */
	public Listener(int port, IncomingManager mgr) throws IOException
		{
		SSLServerSocketFactory factory=(SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		socket = (SSLServerSocket)factory.createServerSocket(port);
		socket.setReuseAddress(true);
		
		//Allow anonymous handshake ie no certificate needed
		LinkedList<String> okCipher=new LinkedList<String>();
		for(String s:socket.getSupportedCipherSuites())
			if(s.contains("_anon_"))
				okCipher.add(s);
		socket.setEnabledCipherSuites(okCipher.toArray(new String[]{}));
	
		this.mgr=mgr;
		}
	
	/**
	 * Continuous thread
	 */
	public void run()
		{
		for(;;)
			try
				{
				SSLSocket client = (SSLSocket) socket.accept();
				mgr.newConnection(new RMImanager(client));
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
		}
	}
