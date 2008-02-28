package evplugin.filter;

import org.jdom.*;
import java.util.*;

import javax.swing.*;

import evplugin.data.*;
import evplugin.ev.SimpleObserver;
import evplugin.imageWindow.*;


//Filter = not such a good name? ImageOperators?


/**
 * 
 * @author Johan Henriksson
 */
public abstract class Filter extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static final String metaType="filter";
	
	public static TreeMap<String,FilterInfo> filterInfo=new TreeMap<String,FilterInfo>();
	
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new FilterImageExtension());
		EvData.extensions.put(metaType,new FilterObjectType());
		}
	
	public String getMetaTypeDesc()
		{
		return "Filter ("+getFilterName()+")";
		}
	

	public static void setFilterXmlHead(Element e, String filterName)
		{
		e.setName(metaType);
		e.setAttribute("filtername", filterName);
		}
	
	
	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu)
		{
		}

	
	public static void addFilter(FilterInfo fi)
		{
		Filter.filterInfo.put(fi.getName(),fi);
		}
	
	
	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/
	
	public static class FilterObjectType implements EvObjectType
		{
		public EvObject extractObjects(Element e)
			{
			return extractFilterXML(e);
			}
		}
	public static EvObject extractFilterXML(Element e)
		{
		String filterName=e.getAttributeValue("filtername");
		return filterInfo.get(filterName).readXML(e);
		}
	
	
	//Save is to be defined for each filter itself
	
	/******************************************************************************************************
	 *                               Filter                                                               *
	 *****************************************************************************************************/

	public final SimpleObserver observer=new SimpleObserver();
	public final SimpleObserver observerGUI=new SimpleObserver(); //sender should be the widget needing update?
	
	
	public abstract JComponent getFilterWidget();
	public abstract String getFilterName();
	}
