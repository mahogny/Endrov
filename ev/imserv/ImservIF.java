package imserv;

import java.rmi.Remote;
import java.util.Map;

/**
 * Server "imserv" object: interface
 * 
 * @author Johan Henriksson
 */
public interface ImservIF extends Remote
	{
	public ClientSessionIF auth(String user, String pass) throws Exception;
  
  public String[] getDataKeys() throws Exception;
  
  public DataIF getData(String name) throws Exception;
  //ImagesetIF getImageset(String name)
  
  public Map<String,DataIF> getDataMap() throws Exception;
  
	}
