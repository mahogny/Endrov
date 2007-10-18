package evplugin.filter;

import org.jdom.*;
import java.util.*;

import javax.swing.JMenu;

import evplugin.data.*;
import evplugin.imageWindow.*;


//Filter = not such a good name? ImageOperators?


/**
 * 
 * @author Johan Henriksson
 */
public abstract class FilterMeta extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	//private static final String metaType="filter";
	
	public static TreeMap<String,FilterInfo> filterInfo=new TreeMap<String,FilterInfo>();
	
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new FilterImageExtension());
//		Metadata.extensions.put(metaType,new ImagesetMetaObjectExtension());
		
		
		}
	
	public String getMetaTypeDesc()
		{
		//Can/Should be overriden?
		return "Filter";
		}
	public void saveMetadata(Element e)
		{
		//This is just awful. let every filter implement?
		e.setName("filter");
		}

	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu)
		{
		}

	
	public static void addFilter(FilterInfo fi)
		{
		FilterMeta.filterInfo.put(fi.getName(),fi);
		}
	
	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/
	
	/*
	public static class ImagesetMetaObjectExtension implements MetaObjectExtension
		{
		public MetaObject extractObjects(Element e)
			{
			FilterMeta meta=new FilterMeta();
	
			
			return meta;
			}
	
		
		}
		*/

	
	/******************************************************************************************************
	 *                               Filter                                                               *
	 *****************************************************************************************************/

	}
