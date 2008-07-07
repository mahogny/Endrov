package endrov.imagesetImserv;


import java.util.*;

import bioserv.BioservDaemon;
import bioserv.imserv.ImservConnection;
import endrov.basicWindow.BasicWindow;


//recent: store host,port,user,pass

/**
 * Handler of active sessions to ImServ
 * 
 * @author Johan Henriksson
 *
 */
public class EvImserv
	{
	public static List<EvImservSession> sessions=Collections.synchronizedList(new LinkedList<EvImservSession>());
	
	public static void initPlugin() {}
	static
		{
		System.setProperty("javax.net.ssl.keyStore",BioservDaemon.class.getResource("imservkeys").getFile());
		System.setProperty("javax.net.ssl.keyStorePassword","passphrase");
		System.setProperty("javax.net.ssl.trustStore",BioservDaemon.class.getResource("cacerts").getFile());
		System.setProperty("javax.net.ssl.trustStorePassword","changeit");
		
		BasicWindow.addBasicWindowExtension(new ImservBasic());
		
		
		
		//TODO: add new file type
		}
	
	
	public static class EvImservSession
		{
		ImservConnection conn;
		public EvImservSession(String host, String user, String pass, int port) throws Exception
			{
			//InetAddress.getLocalHost().getHostName(), Daemon.PORT
			conn=ImservConnection.connect(host,user,pass,port);
			if(conn==null)
				throw new Exception("Wrong user/pass?");
			}
		

		
		
		
		
		}
	
	
	
	
	}
