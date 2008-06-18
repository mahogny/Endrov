package imserv;

//http://java.sun.com/j2se/1.5.0/docs/api/javax/rmi/ssl/package-summary.html
//http://java.sun.com/j2se/1.5.0/docs/guide/rmi/socketfactory/SSLInfo.html
//http://java.sun.com/j2se/1.5.0/docs/guide/security/jsse/samples/index.html

import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

/**
 * Server "imserv" object: implementation
 * 
 * @author Johan Henriksson
 */
public class ImservImpl extends UnicastRemoteObject implements ImservIF 
	{
	public static final long serialVersionUID=0;

	private Daemon daemon;
	
	public ImservImpl(Daemon daemon) throws Exception 
		{
		super(Daemon.PORT, new RMISSLClientSocketFactory(), new RMISSLServerSocketFactory());
		this.daemon=daemon;
		}
	
	public DataIF getData(String name) throws Exception 
		{
		return daemon.getData(name);
		
		
//		return new DataImpl(name);
		}
	
	public ClientSessionIF auth(String user, String pass) throws Exception
		{
		ClientSessionImpl sess=new ClientSessionImpl(user);
		daemon.addSession(sess);
		return sess;
		}
	
	
	public String[] getDataKeys() throws Exception
		{
		//Map<String,DataIF>	
	//	Map<String,DataIF> map=new HashMap<String, DataIF>();
		return daemon.getAllDataMap().keySet().toArray(new String[]{});
		}
	
	public Map<String,DataIF> getDataMap()
		{
		return daemon.getAllDataMap();
		}
	
	
	}
	
