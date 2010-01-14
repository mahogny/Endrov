/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.examplePlugin;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;


/**
 * Example object, custom metadata.
 * Doesn't do anything, meant as a minimalistic starter for new objects
 * 
 * @author Johan Henriksson
 *
 */
public class ExampleObject extends EvObject
	{
	//No spaces or funny characters
	private static final String metaType="exampleObject";

	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,ExampleObject.class);
		}

	//////////////////
	// The meat
	
	public String myValue="";


	public int valueLength()
		{
		return myValue.length();
		}
	
	
	////////////////
	// Extending data menu
	
	@Override
	public void buildMetamenu(JMenu menu)
		{
		}

	////////////////
	// Name for the user
	
	@Override
	public String getMetaTypeDesc()
		{
		return "Example object";
		}

	////////////////
	// How to store the object on disk
	
	@Override
	public void loadMetadata(Element e)
		{
		myValue=e.getChild("extendedData").getAttributeValue("foo");
		}

	@Override
	public String saveMetadata(Element e)
		{
		Element someData=new Element("extendedData");
		someData.setAttribute("foo",myValue);
		e.addContent(someData);
		
		return metaType; //Minimum requirement
		}

	}
