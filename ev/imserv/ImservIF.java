package imserv;

import java.rmi.Remote;
import java.util.Map;
import java.util.Set;

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
  
  
  //To be able to present a list of tags etc, need a list to choose from
  public String[] getTags() throws Exception;
  public String[] getChannels() throws Exception;
  public String[] getObjects() throws Exception;
  
	}
