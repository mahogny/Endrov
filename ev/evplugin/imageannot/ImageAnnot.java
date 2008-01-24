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
			{
			Vector<ImageAnnot> set=new Vector<ImageAnnot>();
			for(EvObject ob:meta.metaObject.values())
				if(ob instanceof ImageAnnot)
					set.add((ImageAnnot)ob);
			return set;
			}
		}
	
	

	
	/******************************************************************************************************
	 *                               Instance NucLineage                                                  *
	 *****************************************************************************************************/
	
	public String text=""; //never null
	public Vector3d pos=new Vector3d(); //never null
	
	
	
	
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
		
		}
	
	
	
	
	
	}
