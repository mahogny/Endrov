package endrov.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Utility functions for lists
 * @author Johan Henriksson
 */
public class EvListUtil
	{

	public static <A,B> List<Tuple<A, B>> map2tuples(Map<A, B> map)
		{
		LinkedList<Tuple<A, B>> list=new LinkedList<Tuple<A,B>>();
		for(Map.Entry<A, B> e:map.entrySet())
			list.add(new Tuple<A, B>(e.getKey(),e.getValue()));
		return list;
		}
	
	public static <A,B> SortedMap<A, B> tuples2map(List<Tuple<A, B>> list)
		{
		TreeMap<A, B> map=new TreeMap<A, B>();
		for(Tuple<A, B> t:list)
			map.put(t.fst(),t.snd());
		return map;
		}
	
	
	
	/**
	 * Find out the closest frame
	 */
	public static <B> EvDecimal closestFrame(SortedMap<EvDecimal,B> imageLoader, EvDecimal frame)
		{
		if(imageLoader.get(frame)!=null || imageLoader.size()==0)
			return frame;
		else
			{
			SortedMap<EvDecimal, B> before=imageLoader.headMap(frame);
			SortedMap<EvDecimal, B> after=imageLoader.tailMap(frame);
			if(before.size()==0)
				return imageLoader.firstKey();
			else if(after.size()==0)
				return imageLoader.lastKey();
			else
				{
				EvDecimal afterkey=after.firstKey();
				EvDecimal beforekey=before.lastKey();
				
				if(afterkey.subtract(frame).less(frame.subtract(beforekey)))
					return afterkey;
				else
					return beforekey;
				}
			}
		}

	/*
	public static <E> SortedSet<E> inclusiveHeadSet(SortedSet<E> set)
		{
		}*/
	
	}
