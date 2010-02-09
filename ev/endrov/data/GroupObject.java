/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
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
	

	
	public String getMetaType()
		{
		return metaType;
		}
	
	public String getMetaTypeDesc()
		{
		return "Group";
		}

	public String saveMetadata(Element e)
		{
		return getMetaType();
		}
	
	public void loadMetadata(Element e)
		{
		}
	
	public void buildMetamenu(JMenu menu)
		{
		}
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,GroupObject.class);
		}

	
	}
