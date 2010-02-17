/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardware;

import java.util.*;
import org.jdom.Element;


/**
 * A provider of other devices
 * @author Johan Henriksson
 *
 */
public abstract class EvDeviceProvider
	{
	/** Return null if operation not supported 
	 * TODO maybe not return but add it right away?
	 * */
	public abstract Set<EvDevice> autodetect();
	
	public Map<String, EvDevice> hw=new HashMap<String, EvDevice>();
	
	
	
	public abstract void getConfig(Element root);
	public abstract void setConfig(Element root);

	public abstract List<String> provides();
	public abstract EvDevice newProvided(String s); 
	}
