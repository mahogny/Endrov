/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.icon.BasicIcon;

/**
 * An endrov object. Very little is required, the class exists to great
 * deal for type safety. Serialization and keeping track of modifications
 * is the main thing.
 * 
 * It should be possible to create objects with an empty constructor ie
 * it should be a Java Bean. This keeps several future possibilities open.
 * 
 * 
 * @author Johan Henriksson
 *
 */
public abstract class EvObject extends EvContainer
	{
	//TODO new serializer
	//TODO new serializer
	// EvData.extensions.put(metaType,new ImagesetMetaObjectExtension());
  //if the class is a bean then Extension is not needed, rather give
	//the .class and create a new method here.
	
	
	
	/** Serialize object */
	public abstract String saveMetadata(Element e);
	
	/** Unserialize object */
	public abstract void loadMetadata(Element e);
	
	/** Human readable name */
	public abstract String getMetaTypeDesc();
	
	/** Attach menu entries specific for this type of object */
	public abstract void buildMetamenu(JMenu menu);
	
	
	/**
	 * Generic icon. Objects should override with a more specific icon.
	 */
	public Icon getContainerIcon()
		{
		return BasicIcon.iconData;
		}

	
	}
