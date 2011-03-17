/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

import javax.swing.*;
import org.jdom.*;

/**
 * Meta object of unknown/custom type. Either real custom data or corresponding plugin is missing
 * @author Johan Henriksson
 */
public class CustomObject extends EvObject
	{
	public Element xml=new Element("custom");
	//privateString metaType;
	
	public String getMetaType()
		{
		return xml.getName(); 
		}
	
	public String getMetaTypeDesc()
		{
		return "unknown("+getMetaType()+")";
		}

	public String saveMetadata(Element e)
		{
		for(Object o:xml.getChildren())
			e.addContent((Element)((Element)o).clone()); //potential need for clone
		return getMetaType();
		}
	
	public void loadMetadata(Element e)
		{
		xml=e;
		}
	
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		}
	
	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}
	}
