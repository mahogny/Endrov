package evplugin.imagesetImserv;


import java.util.*;

import org.jdom.Element;

import evplugin.basicWindow.BasicWindow;
import evplugin.ev.EV;
import evplugin.ev.PersonalConfig;
import evplugin.imagesetImserv.service.ImservClientPane;
import evplugin.imagesetImserv.service.ImservConnection;


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
		System.setProperty("javax.net.ssl.keyStore",ImservClientPane.class.getResource("imservkeys").getFile());
		System.setProperty("javax.net.ssl.keyStorePassword","passphrase");
		System.setProperty("javax.net.ssl.trustStore",ImservClientPane.class.getResource("cacerts").getFile());
		System.setProperty("javax.net.ssl.trustStorePassword","changeit");
		
		BasicWindow.addBasicWindowExtension(new ImservBasic());
		
		EV.personalConfigLoaders.put("imservwindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
//				try	{new FrameTimeWindow(BasicWindow.getXMLbounds(e));}
	//			catch (Exception e1) {e1.printStackTrace();}
				}
			public void savePersonalConfig(Element e){}
			});
		
		//TODO: add new file type
		}
	
	
	public static class EvImservSession
		{
		ImservConnection conn;
		public EvImservSession(String host, int port) throws Exception
			{
			//InetAddress.getLocalHost().getHostName(), Daemon.PORT
			conn=ImservConnection.connect(host,port);
			}
		

		
		
		
		
		}
	
	
	
	
	}
