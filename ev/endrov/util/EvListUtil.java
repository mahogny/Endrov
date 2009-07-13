package endrov.util;

import java.util.Collection;
import java.util.Iterator;
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
	 * O(n)
	 */
	public static Integer findPercentileInt(int[] list, double perc)
		{
		int size=(int)(perc*(list.length-1));
		return findRankInt(list,list.length, size);
		}

	
	/**
	 * Find value of given rank q (=value at position q in sorted list). O(n)
	 */
	public static Integer findRankInt(int[] list, int q)
		{
		return findRankInt(list,list.length, q);
		}
	
	/**
	 * Helper function to find value of rank
	 */
	private static Integer findRankInt(int[] set, int length, int q)
		{
		int pivotPos=(int)(Math.random()*length);
		int pivot=set[pivotPos];
		
		int[] part1=new int[length]; 
		int[] part2=new int[length]; 

		//Split the list
		int c1=0,c2=0;
		for(int i=0;i<length;i++)
			{
			int t=set[i];
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
	
	
	
	
	
	
	
	/**
	 * Convert to scalar array
	 */
	public static double[] toDoubleArray(Collection<Double> set)
		{
		double[] out=new double[set.size()];
		int i=0;
		for(Double d:set)
			out[i++]=d;
		return out;
		}
	
	/**
	 * Convert to scalar array
	 */
	public static int[] toDoubleArray(Collection<Integer> set)
		{
		int[] out=new int[set.size()];
		int i=0;
		for(Integer d:set)
			out[i++]=d;
		return out;
		}
	
	/**
	 * Find percentile. q<-[0,1].
	 * O(n)
	 */
	public static Double findPercentileDouble(double[] list, double perc)
		{
		int size=(int)(perc*(list.length-1));
		return findRankDouble(list,list.length, size);
		}

	
	/**
	 * Find value of given rank q (=value at position q in sorted list). O(n)
	 */
	public static Double findRankDouble(double[] list, int q)
		{
		return findRankDouble(list,list.length, q);
		}
	
	/**
	 * Helper function to find value of rank
	 */
	private static Double findRankDouble(double[] set, int length, int q)
		{
		int pivotPos=(int)(Math.random()*length);
		double pivot=set[pivotPos];
		
		double[] part1=new double[length]; 
		double[] part2=new double[length]; 

		//Split the list
		int c1=0,c2=0;
		for(int i=0;i<length;i++)
			{
			double t=set[i];
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
			return findRankDouble(part1, c1, q);
		else if(q<c1+cmid)
			//It is one of the elements in the middle (all the same)
			return pivot;
		else
			//Recurse right list
			return findRankDouble(part2, c2, q-c1-cmid);
		}
	
	/**
	 * Get index of element with largest value
	 */
	public static int getIndexOfMax(double[] t) 
		{
		double maximum = t[0];
		int mi=0;
		for (int i=1; i<t.length; i++)
      if (t[i] > maximum)
      	{
        maximum = t[i];
        mi=i;
      	}
		return mi;
		}

	

	/**
	 * Get key of element with largest value
	 */
	public static <E,F extends Comparable<F>> E getKeyOfMax(Map<E,F> t) 
		{
		Iterator<Map.Entry<E,F>> it=t.entrySet().iterator();
		Map.Entry<E, F> fst=it.next();
		E index=fst.getKey();
		F max=fst.getValue();
		while(it.hasNext())
			{
			Map.Entry<E, F> next=it.next();
			if(max.compareTo(next.getValue())<1)
				{
				index=next.getKey();
				max=next.getValue();
				}
			}
		return index;
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
		//System.out.println(findRankInt(list, 10));
		}
	
	}
