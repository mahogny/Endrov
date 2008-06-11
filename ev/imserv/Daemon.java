package imserv;

import java.util.WeakHashMap;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Daemon extends Thread
	{
	WeakHashMap<ClientSession, Object> sessions=new WeakHashMap<ClientSession, Object>();
	
	int port=9999;
	
	boolean toStop=false;
	public void run()
		{


		try
			{
			SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(port);
//			sslserversocket.setReuseAddress(true);

			while(!toStop)
				{
				SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();

				ClientSession session=new ClientSession(sslsocket);
				session.start();
				sessions.put(session,null);
				}

			}
		catch (Exception exception) 
			{
			System.out.println("Failed to listen");
			exception.printStackTrace();
			}



		}
	
	
	
	}
