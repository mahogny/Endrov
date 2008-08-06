package endrov.nuc;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


public class NucExp implements Cloneable
	{
	public SortedMap<Integer,Double> level=new TreeMap<Integer, Double>();
	public java.awt.Color expColor=java.awt.Color.RED; //Not stored to disk, but kept here so the color is the same in all windows
	public String unit;
	
	/**
	 * Make a deep copy 
	 */
	public Object clone()
		{
		NucExp exp=new NucExp();
		for(Map.Entry<Integer, Double> e:level.entrySet())
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
	public Double interpolateLevel(double frame)
		{
		if(frame<level.firstKey())
			return level.get(level.firstKey());
		else if(frame>level.lastKey())
			return level.get(level.lastKey());
		else
			{
			SortedMap<Integer,Double> hlevel=level.headMap((int)frame);
			SortedMap<Integer,Double> tlevel=level.tailMap((int)frame);
			int frameBefore=hlevel.lastKey();
			int frameAfter=tlevel.firstKey();
			double levelBefore=hlevel.get(frameBefore);
			double levelAfter=tlevel.get(frameAfter);
			double s=(frame-frameBefore)/(frameAfter-frameBefore);
			return levelAfter*s+levelBefore*(1-s);
			}
		}
	
	
	}
