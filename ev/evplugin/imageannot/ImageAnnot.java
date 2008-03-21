package evplugin.imageannot;

import org.jdom.*;

import java.util.*;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import evplugin.imageWindow.ImageWindow;
import evplugin.imageWindow.ImageWindowExtension;
import evplugin.modelWindow.ModelWindow;
import evplugin.data.*;

/**
 * Meta object: Image annotation
 * @author Johan Henriksson
 */
public class ImageAnnot extends EvObject implements Cloneable
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="imageannot";


	
	public static void initPlugin() {}
	static
		{

		ModelWindow.modelWindowExtensions.add(new ImageAnnotModelExtension());
		
		EvData.extensions.put(metaType,new EvObjectType()
			{
			public EvObject extractObjects(Element e)
				{
				ImageAnnot meta=new ImageAnnot();
				try
					{
					meta.pos.x=e.getAttribute("x").getDoubleValue();
					meta.pos.y=e.getAttribute("y").getDoubleValue();
					meta.pos.z=e.getAttribute("z").getDoubleValue();
					meta.frame=e.getAttribute("frame").getIntValue();
					meta.text=e.getAttributeValue("text");
					}
				catch (DataConversionException e1){e1.printStackTrace();}
				return meta;
				}
			});
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				ImageAnnotRenderer r=new ImageAnnotRenderer(w);
				w.imageWindowTools.add(new ToolMakeImageAnnot(w,r));
				w.imageWindowRenderers.add(r);
				}
			});
		}

	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu)
		{
		}

	
	
	public static Collection<ImageAnnot> getObjects(EvData meta)
		{
		if(meta==null)
			return new Vector<ImageAnnot>();
		else
			return meta.getObjects(ImageAnnot.class);
		}
	
	

	
	/******************************************************************************************************
	 *                               Instance NucLineage                                                  *
	 *****************************************************************************************************/
	
	public String text="";
	public Vector3d pos=new Vector3d(); //never null
	public int frame;
	
	
	
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
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		
		e.setAttribute("x", ""+pos.x);
		e.setAttribute("y", ""+pos.y);
		e.setAttribute("z", ""+pos.z);
		e.setAttribute("frame", ""+frame);
		e.setAttribute("text", ""+text);
		}
	
	
	
	
	
	}
