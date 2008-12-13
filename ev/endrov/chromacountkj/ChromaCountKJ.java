package endrov.chromacountkj;

import org.jdom.*;

import java.util.*;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import endrov.data.*;
import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowExtension;
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


	
	public static void initPlugin() {}
	static
		{

		ModelWindow.modelWindowExtensions.add(new ChromaCountKJModelExtension());
		
		EvData.extensions.put(metaType,new EvObjectType()
			{
			public EvObject extractObjects(Element e)
				{
				ChromaCountKJ meta=new ChromaCountKJ();
				try
					{
					meta.pos.x=e.getAttribute("x").getDoubleValue();
					meta.pos.y=e.getAttribute("y").getDoubleValue();
					meta.pos.z=e.getAttribute("z").getDoubleValue();
					meta.frame=e.getAttribute("frame").getIntValue();
					meta.group=e.getAttribute("group").getIntValue();
					}
				catch (DataConversionException e1){e1.printStackTrace();}
				return meta;
				}
			});
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				ChromaCountKJImageRenderer r=new ChromaCountKJImageRenderer(w);
				w.imageWindowTools.add(new ChromaCountKJImageTool(w,r));
				w.imageWindowRenderers.add(r);
				}
			});
		}

	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu)
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
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		
		e.setAttribute("x", ""+pos.x);
		e.setAttribute("y", ""+pos.y);
		e.setAttribute("z", ""+pos.z);
		e.setAttribute("frame", ""+frame);
		e.setAttribute("group", ""+group);
		}
	
	
	
	
	
	}
