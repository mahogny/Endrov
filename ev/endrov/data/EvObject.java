/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;


import javax.swing.Icon;
import javax.swing.JMenu;

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
	/** Serialize object */
	public abstract String saveMetadata(Element e);
	
	/** Unserialize object */
	public abstract void loadMetadata(Element e);
	
	/** Human readable name */
	public abstract String getMetaTypeDesc();
	
	/** Attach menu entries specific for this type of object 
	 * @param parentObject TODO*/
	public abstract void buildMetamenu(JMenu menu, EvContainer parentObject);
	
	/** Get a deep copy of the object, not including children */
	public abstract EvObject cloneEvObject();

	
	
	
	/**
	 * This is the simplest way of cloning an object. It works if the object need not maintain any special pointers which cannot be serialized.
	 * Use it as the standard method of serializing, unless performance is a problem.
	 */
	protected EvObject cloneUsingSerialize()
		{
		try
			{
			Element root=new Element("ost");
			saveMetadata(root);
			EvObject newObject=this.getClass().newInstance();
			newObject.loadMetadata(root);
			return newObject;
			}
		catch (InstantiationException e)
			{
			e.printStackTrace();
			}
		catch (IllegalAccessException e)
			{
			e.printStackTrace();
			}
		System.out.println("This should be impossible");
		return null; //Should never happen
		}

	
	/**
	 * Generic icon. Objects should override with a more specific icon.
	 */
	public Icon getContainerIcon()
		{
		return BasicIcon.iconData;
		}

	/**
	 * Make a deep copy of the object - uses the serialization mechanism to do so
	 */
	public EvObject cloneEvObjectRecursive()
		{
		EvObject copyRoot=cloneEvObject();
		for(String name:metaObject.keySet())
			copyRoot.metaObject.put(name, metaObject.get(name).cloneEvObject());		
		return copyRoot;
		}
	

	}
