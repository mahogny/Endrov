package endrov.neighmap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.util.EvDecimal;

/**
 * Map of neigbours
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class NeighMap extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/

	private static final String metaType="neighmap";

	public static void initPlugin() {}
	static
		{
		EvData.extensions.put(metaType,NeighMap.class);
		}

	
	public static class Interval
		{
		public Interval(EvDecimal start, EvDecimal end)
			{
			this.start = start;
			this.end = end;
			}
		public EvDecimal start,end;
		
		public Interval cut(Interval i)
			{
			Interval ni=new Interval(start,end);
			if(ni.start.less(i.start))
				ni.start=i.start;
			if(ni.end==null || (i.end!=null && ni.end.greater(i.end)))
				ni.end=i.end;
			return ni;
			}
		
		public EvDecimal length()
			{
			EvDecimal len=end.subtract(start);
			if(len.less(EvDecimal.ZERO))
				return EvDecimal.ZERO;
			else
				return len;
			}
		
		}
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	public Map<String, Interval> lifetime=new HashMap<String, Interval>();
	
	/**
	 * List of intervals during which A and B are neighbours. The list is entirely symmetric: (a,b) and (b,a)
	 * point to the same list (in memory). 
	 */
	public Map<String, Map<String, List<Interval>>> neighmap=new HashMap<String, Map<String,List<Interval>>>();
	
	
	public Interval validity=null;

	
	
	public Map<String, List<Interval>> getCreateNeighMap(String a)
		{
		Map<String, List<Interval>> m=neighmap.get(a);
		if(m==null)
			neighmap.put(a, m=new TreeMap<String, List<Interval>>());
		return m;
		}
	
	public List<Interval> getCreateListFor(String a, String b)
		{
		Map<String, List<Interval>> ma=getCreateNeighMap(a);
		List<Interval> list=ma.get(b);
		if(list==null)
			{
			ma.put(b,list=new LinkedList<Interval>());
			Map<String, List<Interval>> mb=getCreateNeighMap(b);
			mb.put(a,list);
			}
		return list;
		}
	
	public void buildMetamenu(JMenu menu)
		{
		}
	
	public String getMetaTypeDesc()
		{
		return "Temporal Neighbour Map";
		}
	
	public void loadMetadata(Element e)
		{
		for(Object oframetime:e.getChildren())
			{
			Element e2=(Element)oframetime;
			String aname=e2.getAttributeValue("name");
			
			EvDecimal livefrom=new EvDecimal(e2.getAttributeValue("ls"));
			EvDecimal liveto=new EvDecimal(e2.getAttributeValue("le"));
			
			lifetime.put(aname, new Interval(livefrom,liveto));
			for(Object oneigh:e.getChildren())
				{
				Element eneigh=(Element)oneigh;
				//name: neigh
				String bname=eneigh.getAttributeValue("name");
				List<Interval> ints=getCreateListFor(aname, bname);
				
				for(Object oint:e.getChildren())
					{
					//name: int
					Element eint=(Element)oint;
					EvDecimal neighfrom=new EvDecimal(eint.getAttributeValue("s"));
					EvDecimal neighto=new EvDecimal(eint.getAttributeValue("e"));
					ints.add(new Interval(neighfrom,neighto));
					}
				
				}
			}
		}
	
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		for(String aname:lifetime.keySet())
			{
			Element ael=new Element("cell");
			e.addContent(ael);
			ael.setAttribute("name", aname);
			ael.setAttribute("ls", lifetime.get(aname).start.toString());
			ael.setAttribute("le", lifetime.get(aname).end.toString());
			
			Map<String, List<Interval>> nm=neighmap.get(aname);
			for(String bname:nm.keySet())
				if(aname.compareTo(bname)<=0)
					{
					//Only store half. This is a requirement for this format
					Element bel=new Element("neigh");
					ael.addContent(bel);
					bel.setAttribute("name",bname);
					
					for(Interval interval:nm.get(bname))
						{
						Element eint=new Element("int");
						eint.setAttribute("s",interval.start.toString());
						eint.setAttribute("e",interval.end.toString());
						bel.addContent(eint);
						}
					}
			}
		}
	
	
	
	
	//HTML table generation should go here instead
	//huge. separate file, and it needs multiple neighmaps
	
	
	
	//Code to build a map is so icky it could be a separate file
	
	
	
	
	
	
	}
