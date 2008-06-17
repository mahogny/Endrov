package imserv;

import java.rmi.Remote;

public interface DataIF extends Remote
	{
	static final long serialVersionUID=0;
	
	public void print() throws Exception;
	
	}
