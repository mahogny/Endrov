package endrov.line;

import org.jdom.*;

import java.util.*;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import endrov.data.*;
import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowExtension;
import endrov.modelWindow.ModelWindow;
import endrov.util.EvDecimal;

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
		
		EvData.supportedMetadataFormats.put(metaType,EvLine.class);
		
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

	
	

	
	/******************************************************************************************************
	 *                               Instance NucLineage                                                  *
	 *****************************************************************************************************/
	
	public static class Pos3dt
		{
		public Vector3d v;
		public EvDecimal frame;
		
		public Pos3dt()
			{
			v=new Vector3d();
			frame=EvDecimal.ZERO;
			}
		
		public Pos3dt(Pos3dt p)
			{
			v=new Vector3d(p.v);
			frame=p.frame;
			}

		public Pos3dt(Vector3d v, EvDecimal frame)
			{
			this.v=new Vector3d(v);
			this.frame=frame;
			}

		}
	
	/** Positions, in space and time (w) */
	public Vector<Pos3dt> pos=new Vector<Pos3dt>();
	
	/**
	 * Calculate length of each segment
	 */
	public List<Double> getSegmentDistances()
		{
		List<Double> distances=new LinkedList<Double>();
		for(int i=1;i<pos.size();i++)
			{
			Vector3d p=new Vector3d(pos.get(i-1).v);
			p.sub(pos.get(i).v);
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
			Vector3d p=new Vector3d(pos.get(i-1).v);
			p.sub(pos.get(i).v);
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
	public String saveMetadata(Element e)
		{
		for(Pos3dt p:pos)
			{
			Element ee=new Element("pos");
			ee.setAttribute("x", ""+p.v.x);
			ee.setAttribute("y", ""+p.v.y);
			ee.setAttribute("z", ""+p.v.z);
			ee.setAttribute("frame", ""+p.frame);
			e.addContent(ee);
			}
		
		return metaType;
		}
	
	public void loadMetadata(Element e)
		{
		try
			{
			for(Object ee:e.getChildren())
				{
				Element el=(Element)ee;
				Vector4d pos=new Vector4d();
				pos.x=el.getAttribute("x").getDoubleValue();
				pos.y=el.getAttribute("y").getDoubleValue();
				pos.z=el.getAttribute("z").getDoubleValue();
				if(el.getAttribute("frame")!=null)
					pos.w=el.getAttribute("frame").getDoubleValue();
				pos.add(pos);
				}
			}
		catch (DataConversionException e1){e1.printStackTrace();}
		}
	
	
	
	}
