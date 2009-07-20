package endrov.data;

import javax.swing.*;
import org.jdom.*;

/**
 * A grouping of sub-objects
 * @author Johan Henriksson
 */
public class GroupObject extends EvObject
	{
	private static final String metaType="group";
	

	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,GroupObject.class);
		}
	
	public String getMetaType()
		{
		return metaType;
		}
	
	public String getMetaTypeDesc()
		{
		return "Group";
		}

	public void saveMetadata(Element e)
		{
		e.setName(getMetaType());
		}
	
	public void loadMetadata(Element e)
		{
		}
	
	public void buildMetamenu(JMenu menu)
		{
		}
	}
