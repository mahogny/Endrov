package evplugin.line;

import org.jdom.*;

import java.util.*;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import evplugin.imageWindow.ImageWindow;
import evplugin.imageWindow.ImageWindowExtension;
import evplugin.modelWindow.ModelWindow;
//import evplugin.modelWindow.ModelWindow;
import evplugin.data.*;

/**
 * Meta object: Line 
 * @author Johan Henriksson
 */
public class EvLine extends EvObject implements Cloneable
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="line";

	
	public static void initPlugin() {}
	static
		{

		ModelWindow.modelWindowExtensions.add(new EvLineModelExtension());
		
		EvData.extensions.put(metaType,new EvObjectType()
			{
			public EvObject extractObjects(Element e)
				{
				EvLine meta=new EvLine();
				try
					{
					for(Object ee:e.getChildren())
						{
						Element el=(Element)ee;
						Vector3d pos=new Vector3d();
						pos.x=el.getAttribute("x").getDoubleValue();
						pos.y=el.getAttribute("y").getDoubleValue();
						pos.z=el.getAttribute("z").getDoubleValue();
						meta.pos.add(pos);
						}
					}
				catch (DataConversionException e1){e1.printStackTrace();}
				return meta;
				}
			});
		
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				EvLineRenderer r=new EvLineRenderer(w);
				w.imageWindowTools.add(new ToolMakeLine(w,r));
				w.imageWindowRenderers.add(r);
				}
			});
			
		}

	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu)
		{
		}

	
	
	public static Collection<EvLine> getObjects(EvData meta)
		{
		if(meta==null)
			return new Vector<EvLine>();
		else
			return meta.getObjects(EvLine.class);
		}
	
	

	
	/******************************************************************************************************
	 *                               Instance NucLineage                                                  *
	 *****************************************************************************************************/
	
	public Vector<Vector3d> pos=new Vector<Vector3d>();
	
	
	
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

		for(Vector3d p:pos)
			{
			Element ee=new Element("pos");
			ee.setAttribute("x", ""+p.x);
			ee.setAttribute("y", ""+p.y);
			ee.setAttribute("z", ""+p.z);
			e.addContent(ee);
			}
		}
	
	
	
	
	
	}
