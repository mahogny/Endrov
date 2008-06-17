package imserv;

import java.rmi.server.UnicastRemoteObject;

public class DataImpl extends UnicastRemoteObject implements DataIF
	{
	public static final long serialVersionUID=0;
	private String name;
	
	public DataImpl(String name) throws Exception 
		{
		super(Imserv.PORT,	new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
		this.name=name;
		}
	
	public void print()
		{
		System.out.println("server hello"+ name);
		}
	
	}
