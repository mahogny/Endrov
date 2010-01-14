/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ev;

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
	public void loadPersonalConfig(Element root);
	public void savePersonalConfig(Element root);
	}
