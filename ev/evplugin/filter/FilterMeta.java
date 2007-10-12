package evplugin.filter;

import org.jdom.*;
//import java.util.*;
import evplugin.imageWindow.*;
import evplugin.metadata.*;


//Filter = not such a good name? ImageOperators?


/**
 * 
 * @author Johan Henriksson
 */
public abstract class FilterMeta extends MetaObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	//private static final String metaType="filter";
	
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
