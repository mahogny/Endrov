package evplugin.data;

import javax.swing.*;
import org.jdom.*;

/**
 * Meta object of unknown/custom type. Either real custom data or corresponding plugin is missing
 * @author Johan Henriksson
 */
public class CustomObject extends EvObject
	{
	public final Element xml;
	public final String metaType;
	
	public CustomObject(Element xml)
		{
		this.xml=xml;
		metaType=xml.getName();
		}
	
	public String getMetaTypeDesc()
		{
		return "unknown("+metaType+")";
		}

	public void saveMetadata(Element e)
		{
		e.setName(xml.getName());
		for(Object o:xml.getChildren())
			e.addContent((Element)((Element)o).clone()); //potential need for clone
		//added one later. xml to e. 
		}
	
	
	public void buildMetamenu(JMenu menu)
		{
		}
	}
