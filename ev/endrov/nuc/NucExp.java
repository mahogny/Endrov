/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.nuc;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.util.EvDecimal;


public class NucExp implements Cloneable
	{
	public SortedMap<EvDecimal,Double> level=new TreeMap<EvDecimal, Double>();
	public java.awt.Color expColor=java.awt.Color.RED; //Not stored to disk, but kept here so the color is the same in all windows
	public String unit;
	
	/**
	 * Make a deep copy 
	 */
	public Object clone()
		{
		NucExp exp=new NucExp();
		for(Map.Entry<EvDecimal, Double> e:level.entrySet())
			exp.level.put(e.getKey(),e.getValue());
		exp.expColor=expColor;
		return exp;
		}
	
	/**
	 * Get highest level for any frame
	 */
	public Double getMaxLevel()
		{
		Double max=null;
		for(Double d:level.values())
			if(max==null || max>d)
				max=d;
		return max;
		}
	
	/**
	 * Interpolate level for a certain frame
	 */
	public Double interpolateLevel(EvDecimal frame)
		{
		if(frame.lessEqual(level.firstKey()))
			return level.get(level.firstKey());
		else if(frame.greaterEqual(level.lastKey()))
			return level.get(level.lastKey());
		else
			{
			//There must be more than two distinct points
			SortedMap<EvDecimal,Double> hlevel=level.headMap(frame);
			SortedMap<EvDecimal,Double> tlevel=level.tailMap(frame);
			EvDecimal frameBefore=hlevel.lastKey();
			EvDecimal frameAfter=tlevel.firstKey();
			double levelBefore=hlevel.get(frameBefore);
			double levelAfter=tlevel.get(frameAfter);
			double s=frame.subtract(frameBefore).divide((frameAfter.subtract(frameBefore))).doubleValue();
			return levelAfter*s+levelBefore*(1-s);
			}
		}
	
	
	}
