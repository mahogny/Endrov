package evplugin.imagesetImserv.service;

import java.rmi.server.UnicastRemoteObject;

/**
 * Server "client session" object: implementation
 * 
 * @author Johan Henriksson
 */
public class ClientSessionImpl extends UnicastRemoteObject implements ClientSessionIF
	{
	public static final long serialVersionUID=0;
	
	public String user;
	//ip,port
	//login time
	
	
	public ClientSessionImpl(String user) throws Exception 
		{
		super(Daemon.PORT,	new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
		this.user=user;
		}
	
	
	
	
	}
