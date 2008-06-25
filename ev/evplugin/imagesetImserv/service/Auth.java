package evplugin.imagesetImserv.service;

import org.jdom.Element;


/**
 * 
 * @author Johan Henriksson
 */
public interface Auth
	{

	public void readConfig(Element e); 
	public Element writeConfig();
	
	
	
	}
