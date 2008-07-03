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
