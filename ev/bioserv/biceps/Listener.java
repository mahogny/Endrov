package bioserv.biceps;

import java.io.*;
import java.util.LinkedList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

//name: BIdirective Paranoid Message EXchange 
//BIPMEX RMI

//BICEPS
//bidirective command exchange, paranoid security


/**
 * Listening connection. There is no login or anything in the protocol itself.
 * @author Johan Henriksson
 *
 */
public class Listener extends Thread
	{
	private SSLServerSocket socket;
	private IncomingManager mgr;
	
	public static interface IncomingManager
		{
		public void newConnection(RMImanager conn);
		}
	
	
	
	
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
