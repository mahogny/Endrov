package endrov.util;

import java.util.Collection;
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
	
	
	
	
	/**
	 * Find percentile. q<-[0,1].
	 * O(log n)
	 */
	public static Integer findPercentileInt(Collection<Integer> set, double perc)
		{
		Integer[] list=set.toArray(new Integer[]{});
		int size=(int)(perc*(list.length-1));
		return findRankInt(list,list.length, size);
		}

	
	/**
	 * Find value of given rank q (=value at position q in sorted list). O(log n)
	 */
	public static Integer findRankInt(Collection<Integer> set, int q)
		{
		Integer[] list=set.toArray(new Integer[]{});
		return findRankInt(list,list.length, q);
		}
	
	/**
	 * Helper function to find value of rank
	 */
	private static Integer findRankInt(Integer[] set, int length, int q)
		{
		int pivotPos=(int)(Math.random()*length);
		Integer pivot=set[pivotPos];
		
		Integer[] part1=new Integer[length]; 
		Integer[] part2=new Integer[length]; 

		/*System.out.println("--- "+pivotPos+"("+pivot+") of "+length);
		for(int i=0;i<length;i++)
			System.out.print(set[i]+" ");
		System.out.println();*/
		
		//Split the list
		int c1=0,c2=0;
		for(int i=0;i<length;i++)
			{
			Integer t=set[i];
			if(t<pivot)
				{
				part1[c1]=t;
				c1++;
				}
			else if(t>pivot)
				{
				part2[c2]=t;
				c2++;
				}
			}
		int cmid=length-c1-c2;
			
		//Where in the list to search next?
		if(q<c1)
			//Recurse left list
			return findRankInt(part1, c1, q);
		else if(q<c1+cmid)
			//It is one of the elements in the middle (all the same)
			return pivot;
		else
			//Recurse right list
			return findRankInt(part2, c2, q-c1-cmid);
		}
	
	
	public static void main(String[] args)
		{
		LinkedList<Integer> list=new LinkedList<Integer>();
		for(int i=0;i<50;i++)
			list.add(i);
		for(int i=0;i<50;i++)
			list.add(i);
		/*
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);
		list.add(7);
		*/
		System.out.println(findRankInt(list, 10));
		}
	
	}
