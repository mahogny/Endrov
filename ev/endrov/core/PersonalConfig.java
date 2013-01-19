/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.core;

//import java.util.Vector;
import org.jdom.*;

/**
 * Component that can load/save personalized configuration
 * 
 * @author Johan Henriksson
 *
 */
public interface PersonalConfig
	{
	/**
	 * Load config. Elements with the given name will be passed 
	 * to this function
	 */
	public void loadPersonalConfig(Element root);
	
	/**
	 * Save config - create 0-inf elements and add them to the root.
	 * They should all have the registered name.
	 */
	public void savePersonalConfig(Element root);
	}
