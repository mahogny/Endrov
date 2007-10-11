package evplugin.roi;

import org.jdom.*;

import evplugin.imageWindow.*;
import evplugin.metadata.*;
import java.util.*;

public class ROIMeta extends MetaObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static final String metaType="ROI";
	
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new ROIImageExtension());
//		Metadata.extensions.put(metaType,new ImagesetMetaObjectExtension());
		}

	
	public String getMetaTypeDesc()
		{
		return "ROI";
		}





	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		}





	/******************************************************************************************************
	 *            Class: XML Reader and writer of this type of meta object                                *
	 *****************************************************************************************************/
	
	public static class ImagesetMetaObjectExtension implements MetaObjectExtension
		{
		public MetaObject extractObjects(Element e)
			{
			ROIMeta meta=new ROIMeta();
			
		
			
			return meta;
			}
	
	

		}
	

	
		
		
	/******************************************************************************************************
	 *                               ROIs                                                                 *
	 *****************************************************************************************************/

	TreeMap<String,ROI> roiList=new TreeMap<String,ROI>();
	
	
	}
