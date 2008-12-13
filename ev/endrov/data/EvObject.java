package endrov.data;

import javax.swing.*;
import org.jdom.*;

/**
 * An endrov object
 * @author Johan Henriksson
 *
 */
public abstract class EvObject extends EvContainer
	{
	
	//TODO only use container flag
	/** Has this data been modified? */
	public boolean metaObjectModified=false;
	
	/** Serialize object */
	public abstract void saveMetadata(Element e);
	
	/** Human readable name */
	public abstract String getMetaTypeDesc();
	
	/** Attach menu entries specific for this type of object */
	public abstract void buildMetamenu(JMenu menu);
	}
