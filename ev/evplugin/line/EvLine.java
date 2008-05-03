package evplugin.line;

import org.jdom.*;

import java.util.*;

import javax.swing.JMenu;
import javax.vecmath.Vector4d;

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
						Vector4d pos=new Vector4d();
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
	
	/** Positions, in space and time (w) */
	public Vector<Vector4d> pos=new Vector<Vector4d>();
	
	/**
	 * Calculate length of each segment
	 */
	public List<Double> getSegmentDistances()
		{
		List<Double> distances=new LinkedList<Double>();
		for(int i=1;i<pos.size();i++)
			{
			Vector4d p=new Vector4d(pos.get(i-1));
			p.sub(pos.get(i));
			double len=p.length();
			distances.add(len);
			}
		return distances;
		}

	/**
	 * Calculate total length
	 */
	public double getTotalDistance()
		{
		double totalDist=0;
		for(int i=1;i<pos.size();i++)
			{
			Vector4d p=new Vector4d(pos.get(i-1));
			p.sub(pos.get(i));
			double dx=p.x*p.x;
			double dy=p.y*p.y;
			double dz=p.z*p.z;
			double len=Math.sqrt(dx+dy+dz);
			totalDist+=len;
			}
		return totalDist;
		}
	
	
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

		for(Vector4d p:pos)
			{
			Element ee=new Element("pos");
			ee.setAttribute("x", ""+p.x);
			ee.setAttribute("y", ""+p.y);
			ee.setAttribute("z", ""+p.z);
			e.addContent(ee);
			}
		}
	
	
	
	
	
	}
