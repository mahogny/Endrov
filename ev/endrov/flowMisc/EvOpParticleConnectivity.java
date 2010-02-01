/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMisc;

import java.util.HashMap;
import java.util.Map;

import endrov.imageset.EvStack;

/**
 * Calculate connectivity graph. Assumes stack contains integer values, one for each region.
 * The map returned is symmetric.
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpParticleConnectivity
	{
	
	private static void register(HashMap<Integer,Integer> graph, int a, int b)
		{
		graph.put(a,b);
		}

	/**
	 * 
	 * @param stack
	 * @return
	 */
	public static HashMap<Integer,Integer> exec(EvStack stack)
		{
		HashMap<Integer, Integer> map=new HashMap<Integer, Integer>();
		
		//Would it be faster if it was not a hashmap?

		//Find relations, at least in one way
		int[][] arrs=stack.getReadOnlyArraysInt();
		int w=stack.getWidth();
		int h=stack.getHeight();
		for(int az=0;az<arrs.length;az++)
			{
			int[] thisarr=arrs[az];
			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w;ax++)
					{
					if(ay<h-1)
						register(map, thisarr[ay*w+ax], thisarr[(ay+1)*w+ax]);
					if(ax<w-1)
						register(map, thisarr[ay*w+ax], thisarr[ay*w+(ax+1)]);
					}
			}
		
		//Make map symmetric. Likely this map is much smaller than the entire image;
		//by doing it this way there need only be a single put operation in the
		//first step, making it roughly twice the speed
		HashMap<Integer, Integer> map2=new HashMap<Integer, Integer>();
		for(Map.Entry<Integer, Integer> e:map.entrySet())
			{
			map2.put(e.getKey(),e.getValue());
			map2.put(e.getValue(),e.getKey());
			}
		
		return map2;
		}
	
	
	
	}
