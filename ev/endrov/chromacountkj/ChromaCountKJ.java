/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.chromacountkj;

import org.jdom.*;

import java.util.*;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import endrov.data.*;
import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowExtension;
import endrov.imageWindow.ImageWindowInterface;
import endrov.imageWindow.ImageWindowRendererExtension;
import endrov.modelWindow.ModelWindow;

/**
 * Meta object: Chromatin counting for kristian jeppsson
 * @author Johan Henriksson
 */
public class ChromaCountKJ extends EvObject implements Cloneable
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="chromacountkj";



	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		}

	
	
	public static Collection<ChromaCountKJ> getObjects(EvContainer meta)
		{
		if(meta==null)
			return new Vector<ChromaCountKJ>();
		else
			return meta.getObjects(ChromaCountKJ.class);
		}
	
	

	
	/******************************************************************************************************
	 *                               Instance NucLineage                                                  *
	 *****************************************************************************************************/
	
	public Vector3d pos=new Vector3d(); //never null
	public int frame;
	public int group;
	
	
	
	/**
	 * Description of this metatype 
	 */
	public String getMetaTypeDesc()
		{
		return metaType;
		}
	
	/**
	 * Save down data
	 */
	public String saveMetadata(Element e)
		{
		e.setAttribute("x", ""+pos.x);
		e.setAttribute("y", ""+pos.y);
		e.setAttribute("z", ""+pos.z);
		e.setAttribute("frame", ""+frame);
		e.setAttribute("group", ""+group);
		
		return metaType;
		}
	
	
	public void loadMetadata(Element e)
		{
		try
			{
			pos.x=e.getAttribute("x").getDoubleValue();
			pos.y=e.getAttribute("y").getDoubleValue();
			pos.z=e.getAttribute("z").getDoubleValue();
			frame=e.getAttribute("frame").getIntValue();
			group=e.getAttribute("group").getIntValue();
			}
		catch (DataConversionException e1){e1.printStackTrace();}
		}


	
	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{

		ModelWindow.modelWindowExtensions.add(new ChromaCountKJModelExtension());
		
		EvData.supportedMetadataFormats.put(metaType,ChromaCountKJ.class);
		
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				//ChromaCountKJImageRenderer r=new ChromaCountKJImageRenderer(w);
				w.addImageWindowTool(new ChromaCountKJImageTool(w));
				//w.addImageWindowRenderer(r);
				}
			});
		
		ImageWindow.addImageWindowRendererExtension(new ImageWindowRendererExtension()
			{
			public void newImageWindow(ImageWindowInterface w)
				{
				ChromaCountKJImageRenderer r=new ChromaCountKJImageRenderer(w);
				w.addImageWindowRenderer(r);
				}
			});
		
		}
	
	
	}
