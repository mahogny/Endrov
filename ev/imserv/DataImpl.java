package imserv;

import java.rmi.server.UnicastRemoteObject;



/**
 * Server "data" object: implementation
 * 
 * @author Johan Henriksson
 */
public class DataImpl extends UnicastRemoteObject implements DataIF//, Comparable<DataImpl>
	{
	public static final long serialVersionUID=0;
	private String name;
	
	public DataImpl(String name) throws Exception 
		{
		super(Daemon.PORT,	new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
		this.name=name;
		}
	
	public void print()
		{
		System.out.println("server hello"+ name);
		}
	
	
	public String getName()
		{
		return name;
		}

	/*
	public int compareTo(DataImpl o)
		{
		return getName().compareTo(o.getName());
		}
	
	public boolean equals(Object o) 
		{
		if (!(o instanceof DataImpl))
			return false;
		return name.equals(((DataImpl)o).name);
		}*/
	
	
	}
