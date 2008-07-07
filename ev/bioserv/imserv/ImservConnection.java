package bioserv.imserv;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import bioserv.ClientSessionIF;
import bioserv.BioservDaemon;
import bioserv.RMISSLClientSocketFactory;

public class ImservConnection
	{
	public Registry registry;
	public ImservIF imserv;
	public ClientSessionIF session;
	public int port;
	public String host;
	public String user;
	
	public static ImservConnection connect(String host, String user, String passwd, int port) throws Exception
		{
		ImservConnection conn=new ImservConnection();
		conn.port=port;
		conn.host=host;
		conn.user=user;

		conn.registry = LocateRegistry.getRegistry(InetAddress.getLocalHost().getHostName(), BioservDaemon.PORT,	new RMISSLClientSocketFactory());
		conn.imserv = (ImservIF) conn.registry.lookup("imserv");
		conn.session=conn.imserv.auth(conn.user, passwd);
		if(conn.session!=null)
			return conn;
		return null;
		}
	}
