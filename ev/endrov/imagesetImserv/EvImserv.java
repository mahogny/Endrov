package endrov.imagesetImserv;


import java.io.IOException;
import java.util.*;

import bioserv.BioservDaemon;
import bioserv.imserv.ImservConnection;
import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;
import endrov.data.EvDataSupport;
import endrov.data.EvIOData;
import endrov.util.Tuple;


/**
 * Handler of active sessions to ImServ
 * 
 * @author Johan Henriksson
 *
 */
public class EvImserv
	{
	public static List<EvImservSession> sessions=Collections.synchronizedList(new LinkedList<EvImservSession>());
	
	public static class ImservURL
		{
		String host;
		String user;
		String name;
		String password;
		int port=bioserv.BioservDaemon.PORT;
		public ImservURL(String url)
			{
			//TODO Parser should be rewritten from scratch; error checking and escaping, port
			
			url=url.substring("imserv://".length());
			
			int nextat=url.indexOf('@');
			if(nextat!=-1)
				{
				user=url.substring(0,nextat);
				url=url.substring(nextat+1);
				
				int nextcolon=user.indexOf(":");
				if(nextcolon!=-1)
					{
					String temp=user.substring(0,nextcolon);
					password=user.substring(nextcolon+1);
					user=temp;
					}
				
				}
			
			int nextslash=url.indexOf('/');
			host=url.substring(0,nextslash);
			
			name=url.substring(nextslash+1);
			}
		}

	
	public static EvImservSession getSession(String url) throws Exception
		{
		return getSession(new ImservURL(url));
		}
		
	public static EvImservSession getSession(ImservURL url) throws Exception
		{
		//Search through existing sessions
		for(EvImservSession session:sessions)
			if(session.conn.host.equals(url.host))
				return session;
		
		//Connect to new session
		if(url.password!=null)
			{
			EvImservSession session=new EvImservSession(url.host, url.user, url.password, url.port);
			sessions.add(session);
			BasicWindow.updateWindows();
			return session;
			}
		
		
		DialogOpenDatabase dlg=new DialogOpenDatabase(null);
		dlg.dbUser=url.user;
		dlg.dbUrl=url.host;
		EvImservSession session=dlg.run();
		if(session!=null)
			{
			sessions.add(session);
			BasicWindow.updateWindows();
			}
		return session;
		}

	public static EvData getImageset(String url) throws Exception
		{
		return getDataIO(new ImservURL(url));
		}

	public static EvData getDataIO(ImservURL url) throws Exception
		{
		EvImservSession session=getSession(url);
		if(session!=null)
			return session.conn.getImservImageset(url.name);
		else
			return null;
		}
	
	
	
	
	public static void initPlugin() {}
	static
		{
		System.setProperty("javax.net.ssl.keyStore",BioservDaemon.class.getResource("imservkeys").getFile());
		System.setProperty("javax.net.ssl.keyStorePassword","passphrase");
		System.setProperty("javax.net.ssl.trustStore",BioservDaemon.class.getResource("cacerts").getFile());
		System.setProperty("javax.net.ssl.trustStorePassword","changeit");
		
		BasicWindow.addBasicWindowExtension(new ImservBasic());
		
		
		//New file type
		EvData.supportFileFormats.add(new EvDataSupport(){
		public Integer loadSupports(String file)
			{
			System.out.println("try "+file);
			if(file.startsWith("imserv://"))
				return 10;
			else
				return null;
			}
		public EvData load(String file, EvData.FileIOStatusCallback cb) throws Exception
			{
			System.out.println("loading "+file);
			return getImageset(file);
			}
		public List<Tuple<String, String[]>> getLoadFormats()
			{
			return new LinkedList<Tuple<String,String[]>>();
			}
		public List<Tuple<String, String[]>> getSaveFormats()
			{
			return new LinkedList<Tuple<String,String[]>>();
			}
		public EvIOData getSaver(EvData d, String file) throws IOException
			{
			// TODO Auto-generated method stub
			return null;
			}
		public Integer saveSupports(String file)
			{
			//TODO probably a good thing to implement
			return null;
			}
		
		});
		
		}
	
	
	public static class EvImservSession
		{
		public ImservConnection conn;
		public EvImservSession(String host, String user, String pass, int port) throws Exception
			{
			//InetAddress.getLocalHost().getHostName(), Daemon.PORT
			conn=ImservConnection.connect(host,user,pass,port);
			if(conn==null)
				throw new Exception("Wrong user/pass?");
			}
		

		
		
		
		
		}
	
	
	
	
	}
