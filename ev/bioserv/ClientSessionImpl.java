/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv;

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
		super(BioservDaemon.PORT,	new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
		this.user=user;
		}
	
	
	
	
	}
