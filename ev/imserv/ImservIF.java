package imserv;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ImservIF extends Remote
	{
  public String sayHello() throws RemoteException;
  
  
  public DataIF getData(String name);
  //ImagesetIF getImageset(String name)
  
  
	}
