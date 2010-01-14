/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

import org.jdom.*;

/**
 * One type of Object. Implements loader function from XML.
 * 
 * @author Johan Henriksson
 */
public interface EvObjectType
	{
	public EvObject extractObjects(Element e);
	}
