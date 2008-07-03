package bioserv;

import java.util.Map;

import org.jdom.Element;

import endrov.imagesetImserv.service.DataIF;


/**
 * 
 * @author Johan Henriksson
 */
public interface Auth
	{

	public void readConfig(Element e); 
	public void writeConfig(Element root);
	
	public void canRead(BioservDaemon daemon, String user, Map<String,DataIF> map);
	public void canWrite(BioservDaemon daemon, String user, Map<String,DataIF> map);

	public boolean canLogin(String user, String password);
	}
