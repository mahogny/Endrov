package endrov.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import org.jdom.Element;


import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.util.EvDecimal;

/**
 * Vascular and neuronal networks
 * 
 * @author Johan Henriksson
 *
 */
public class Network extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="nuclineage";

	
	public static Set<String> suggestTypes=new TreeSet<String>(Arrays.asList(
      "undefined",
      "soma",
      "axon",
      "basal dendrite",
      "apical dendrite"
			));
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public TreeMap<EvDecimal, NetworkFrame> frame=new TreeMap<EvDecimal, NetworkFrame>(); 
	
	public static class Point
		{
		public double x,y,z;
		public Double r;
		
		public Point(double x, double y, double z, Double r)
			{
			this.x = x;
			this.y = y;
			this.z = z;
			this.r = r;
			}
		
		public Point(Vector3d v, Double r)
			{
			this.x = v.x;
			this.y = v.y;
			this.z = v.z;
			this.r = r;
			}

		public Vector3d toVector3d()
			{
			return new Vector3d(x,y,z);
			}
		
		@Override
		public int hashCode()
			{
			return (int)(1000*(x+y+z));
			}
		}
	
	public static class TimeRelation
		{
		//Might need a faster index. Index from,to
		int fromTime, fromPid;
		int toTime, toPid;
		}
	
	public static class Segment
		{
		public int[] points;
		
		public String type;
		//TODO 
		}
	
	public static class NetworkFrame
		{

		public HashMap<Integer, Point> points=new HashMap<Integer, Point>();
		
		public List<Segment> segments=new ArrayList<Segment>();
		
		private int lastPointID=0;
		
		public int putNewPoint(Point p)
			{
			int id=lastPointID;
			while(points.containsKey(id))
				id++;
			points.put(id, p);
			lastPointID=id;
			return id;
			}
		
		public int reusePoint(Point p)
			{
			for(Map.Entry<Integer, Point> e:points.entrySet())
				{
				Point op=e.getValue();
				if(op.x==p.x && op.y==p.y && op.z==p.z)
					return e.getKey();
				}
			return putNewPoint(p);
			}
		
		}

	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,Network.class);
		/*
		try
			{
			cellGroups.importXML(EvFileUtil.getFileFromURL(NucLineage.class.getResource("cellgroups.cgrp")));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
			*/
		}

	@Override
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		}

	@Override
	public EvObject cloneEvObject()
		{
		// TODO Auto-generated method stub
		return null;
		}

	@Override
	public String getMetaTypeDesc()
		{
		return "Network";
		}

	@Override
	public void loadMetadata(Element e)
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	public String saveMetadata(Element e)
		{
		// TODO Auto-generated method stub
		return null;
		}
	
	}
