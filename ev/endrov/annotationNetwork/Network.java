package endrov.annotationNetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.vecmath.Vector3d;

import org.jdom.DataConversionException;
import org.jdom.Element;

import endrov.core.EndrovUtil;
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
	
	private static final String metaType="network";

	
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

		public Point(Point p)
			{
			this.x=p.x;
			this.y=p.y;
			this.z=p.z;
			this.r=p.r;
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
		
		public Point clone()
			{
			return new Point(this);
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
		
		public Segment clone()
			{
			Segment s=new Segment();
			s.type=type;
			s.points=new int[points.length];
			return s;
			}
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
			Integer id=getPointIDByPos(p.x, p.y, p.z);
			if(id!=null)
				return id;
			else
				return putNewPoint(p);
			}
		
		public Integer getPointIDByPos(double x, double y, double z)
			{
			for(Map.Entry<Integer, Point> e:points.entrySet())
				{
				Point op=e.getValue();
				if(op.x==x && op.y==y && op.z==z)
					return e.getKey();
				}
			return null;
			}
		
		
		public NetworkFrame clone()
			{
			NetworkFrame nf=new NetworkFrame();
			for(int id:points.keySet())
				nf.points.put(id, points.get(id).clone());
			for(Segment s:segments)
				nf.segments.add(s.clone());
			return nf;
			}
		}

	


	@Override
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		}

	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}

	@Override
	public String getMetaTypeDesc()
		{
		return "Network";
		}

	@Override
	public void loadMetadata(Element e)
		{
		try
			{
			for(Element nfe:EndrovUtil.castIterableElement(e.getChildren()))
				{
				EvDecimal f=new EvDecimal(nfe.getAttributeValue("frame"));
				NetworkFrame nf=new NetworkFrame();
				frame.put(f, nf);
				
				for(Element pose:EndrovUtil.castIterableElement(nfe.getChildren()))
					{
					if(pose.getName().equals("p"))
						{
						int pid=pose.getAttribute("id").getIntValue();
						double posx=pose.getAttribute("x").getDoubleValue();
						double posy=pose.getAttribute("y").getDoubleValue();
						double posz=pose.getAttribute("z").getDoubleValue();
						Double posr=null;
						if(pose.getAttribute("r")!=null)
							posr=pose.getAttribute("r").getDoubleValue();

						Point p=new Point(posx,posy,posz,posr);
						nf.points.put(pid, p);
						}
					else if(pose.getName().equals("seg"))
						{
						Segment seg=new Segment();
						nf.segments.add(seg);
						
						if(pose.getAttribute("type")!=null)
							seg.type=pose.getAttributeValue("type");
						
						//Parse points
						ArrayList<Integer> pids=new ArrayList<Integer>(); 
						StringTokenizer stok=new StringTokenizer(pose.getAttributeValue("points"),",");
						while(stok.hasMoreElements())
							pids.add(Integer.parseInt(stok.nextToken()));						
						seg.points=new int[pids.size()];
						for(int i=0;i<seg.points.length;i++)
							seg.points[i]=pids.get(i);
						}
					}

				}
			}
		catch (DataConversionException e1)
			{
			e1.printStackTrace();
			}
		}

	@Override
	public String saveMetadata(Element e)
		{
		for(EvDecimal f:frame.keySet())
			{
			NetworkFrame nf=frame.get(f);
			Element framee=new Element("frame");
			e.addContent(framee);
			framee.setAttribute("frame",""+f);
			
			//Points
			for(int pid:nf.points.keySet())
				{
				Point pos=nf.points.get(pid);
				Element pose=new Element("p");
				framee.addContent(pose);
				pose.setAttribute("id", ""+pid);
				pose.setAttribute("x", ""+pos.x);
				pose.setAttribute("y", ""+pos.y);
				pose.setAttribute("z", ""+pos.z);
				if(pos.r!=null)
					pose.setAttribute("r", ""+pos.r);
				}
			
			//Segments
			for(Segment segment:nf.segments)
				{
				Element segmente=new Element("seg");
				framee.addContent(segmente);

				if(segment.type!=null)
					segmente.setAttribute("type",segment.type);

				//Write segment pids
				StringBuffer sbp=new StringBuffer();
				boolean first=true;
				for(int pid:segment.points)
					{
					sbp.append(pid);
					if(first)
						sbp.append(",");
					first=false;
					}
				segmente.setAttribute("points", sbp.toString());
				
				}

			}
		return metaType;
		}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,Network.class);
		}
	}
