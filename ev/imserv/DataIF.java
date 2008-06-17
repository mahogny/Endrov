package imserv;

import java.rmi.Remote;

/**
 * Server "data" object: implementation
 * 
 * @author Johan Henriksson
 */
public interface DataIF extends Remote
	{
	static final long serialVersionUID=0;
	
	public void print() throws Exception;
	
	public String getName() throws Exception;
	
	
	
	
	
	}
