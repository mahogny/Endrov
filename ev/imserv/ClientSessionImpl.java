package imserv;

import java.rmi.server.UnicastRemoteObject;

/**
 * Server "client session" object: implementation
 * 
 * @author Johan Henriksson
 */
public class ClientSessionImpl extends UnicastRemoteObject implements ClientSessionIF
	{
	public static final long serialVersionUID=0;
	
	public ClientSessionImpl() throws Exception 
		{
		super(Daemon.PORT,	new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
		}
	
	}
