package evplugin.imagesetImserv.service;

import java.util.Map;

import org.jdom.Element;


/**
 * 
 * @author Johan Henriksson
 */
public interface Auth
	{

	public void readConfig(Element e); 
	public void writeConfig(Element root);
	
	public void canRead(Daemon daemon, String user, Map<String,DataIF> map);
	public void canWrite(Daemon daemon, String user, Map<String,DataIF> map);

	public boolean canLogin(String user, String password);
	}
