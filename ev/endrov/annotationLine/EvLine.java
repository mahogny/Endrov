/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.annotationLine;

import org.jdom.*;

import java.util.*;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import endrov.data.*;
import endrov.util.EvDecimal;
import endrov.windowViewer2D.Viewer2DWindow;
import endrov.windowViewer2D.Viewer2DExtension;
import endrov.windowViewer2D.Viewer2DInterface;
import endrov.windowViewer2D.Viewer2DRendererExtension;
import endrov.windowViewer3D.Viewer3DWindow;

/**
 * Meta object: Line 
 * @author Johan Henriksson
 */
public class EvLine extends EvObject 
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="line";

	

	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		}

	
	

	
	/******************************************************************************************************
	 *                               Instance Pos3dt                                                      *
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

		public Pos3dt clone()
			{
			Pos3dt p=new Pos3dt();
			p.v=new Vector3d(v);
			p.frame=frame;
			return p;
			}
		
		@Override
		public String toString()
			{
			return "("+v+","+frame+")";
			}
		}
	
	/** Positions, in space and time (w) */
	public Vector<Pos3dt> pos=new Vector<Pos3dt>();
	
	

	
	public EvLine clone()
		{
		EvLine l=new EvLine();
		for(Pos3dt p:pos)
			l.pos.add(p);
		return l;
		}
	
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

		Viewer3DWindow.modelWindowExtensions.add(new EvLineModelExtension());
		
		EvData.supportedMetadataFormats.put(metaType,EvLine.class);
		
		Viewer2DWindow.addImageWindowExtension(new Viewer2DExtension()
			{
			public void newImageWindow(Viewer2DWindow w)
				{
				EvLineImageRenderer r=new EvLineImageRenderer(w);
				w.addImageWindowTool(new ToolMakeLine(w,r));
				w.addImageWindowRenderer(r);
				//w.addImageWindowTool(new ToolMakeLine(w));
				}
			});

		Viewer2DWindow.addImageWindowRendererExtension(new Viewer2DRendererExtension()
			{
			public void newImageWindow(Viewer2DInterface w)
				{
				EvLineImageRenderer r=new EvLineImageRenderer(w);
				w.addImageWindowRenderer(r);
				}
			});

		}
	}
