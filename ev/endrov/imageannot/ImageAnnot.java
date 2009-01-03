package endrov.imageannot;

import org.jdom.*;

import java.util.*;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import endrov.data.*;
import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowExtension;
import endrov.modelWindow.ModelWindow;

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
		
		EvData.extensions.put(metaType,ImageAnnot.class);
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				ImageAnnotImageRenderer r=new ImageAnnotImageRenderer(w);
				w.imageWindowTools.add(new ImageAnnotImageTool(w,r));
				w.imageWindowRenderers.add(r);
				}
			});
		}

	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu)
		{
		}

	
	
	public static Collection<ImageAnnot> getObjects(EvContainer meta)
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
	
	
	
	
	}
