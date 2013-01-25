/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeText;

import org.jdom.*;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import endrov.data.*;
import endrov.windowViewer2D.Viewer2DWindow;
import endrov.windowViewer2D.Viewer2DExtension;
import endrov.windowViewer3D.Viewer3DWindow;

/**
 * Meta object: Image annotation
 * @author Johan Henriksson
 */
public class TextAnnot extends EvObject implements Cloneable
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="imageannot";


	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		}

	

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public String text="";
	public Vector3d pos=new Vector3d(); //never null
	public int frame;
	
	
	
	public TextAnnot clone()
		{
		TextAnnot a=new TextAnnot();
		a.text=text;
		a.pos=new Vector3d(pos);
		a.frame=frame;
		return a;
		}
	
	
	@Override
	public boolean equals(Object obj)
		{
		if(obj instanceof TextAnnot)
			{
			TextAnnot a=(TextAnnot)obj;
			return a.text.equals(text) && a.pos.equals(pos) && a.frame==frame;
			}
		else
			return false;
		}
	
	/**
	 * Description of this metatype 
	 */
	public String getMetaTypeDesc()
		{
		return metaType;
		}
	
	/**
	 * Save data
	 */
	public String saveMetadata(Element e)
		{
		e.setAttribute("x", ""+pos.x);
		e.setAttribute("y", ""+pos.y);
		e.setAttribute("z", ""+pos.z);
		e.setAttribute("frame", ""+frame);
		e.setAttribute("text", ""+text);
		
		return metaType;
		}
	
	/**
	 * Load data
	 */
	public void loadMetadata(Element e)
		{
		try
			{
			pos.x=e.getAttribute("x").getDoubleValue();
			pos.y=e.getAttribute("y").getDoubleValue();
			pos.z=e.getAttribute("z").getDoubleValue();
			frame=e.getAttribute("frame").getIntValue();
			text=e.getAttributeValue("text");
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

		Viewer3DWindow.modelWindowExtensions.add(new TextAnnotModelExtension());
		
		EvData.supportedMetadataFormats.put(metaType,TextAnnot.class);
		Viewer2DWindow.addImageWindowExtension(new Viewer2DExtension()
			{
			public void newImageWindow(Viewer2DWindow w)
				{
				TextAnnotImageRenderer r=new TextAnnotImageRenderer(w);
				w.addImageWindowTool(new TextAnnotImageTool(w,r));
				w.addImageWindowRenderer(r);
				}
			});
		}
	
	}
