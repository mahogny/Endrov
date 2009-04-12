package endrov.unsortedImageFilters;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.imageset.EvPixels;

/**
 * Calculate histograms
 * @author Johan Henriksson
 *
 */
public class Histogram
	{
	private static <A> void inc(Map<A, Integer> m, A a)
		{
		Integer i=m.get(a);
		if(i==null)
			i=1;
		else
			i=i+1;
		m.put(a, i);
		}
	
	public static <A> SortedMap<A,Integer> makeHistCumulative(Map<A,Integer> in)
		{
		TreeMap<A, Integer> cumsum=new TreeMap<A, Integer>();
		Integer lastCount=0;
		for(Map.Entry<A, Integer> e:new TreeMap<A, Integer>(in).entrySet())
			{
			lastCount+=e.getValue();
			cumsum.put(e.getKey(), lastCount);
			}
		return cumsum;
		}
	
	/**
	 * Use the original type of the pixels later? simplifies extraction, less conversion, only one function,
	 * but the invoker will have less information
	 */
	public Map<Integer,Integer> intHistogram(EvPixels in)
		{
		if(in.getType()==EvPixels.TYPE_INT)
			{
			int[] inPixels=in.getArrayInt();
			Map<Integer,Integer> hist=new HashMap<Integer, Integer>(); 
			for(int p:inPixels)
				inc(hist,(Integer)p);
			return hist;
			}
		else
			{
			in=in.convertTo(EvPixels.TYPE_INT, true);
			int[] inPixels=in.getArrayInt();
			Map<Integer,Integer> hist=new HashMap<Integer, Integer>(); 
			for(int p:inPixels)
				inc(hist,(Integer)p);
			return hist;
			}
		}
	
	
	
	}
