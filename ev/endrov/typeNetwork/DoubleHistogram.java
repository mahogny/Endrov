package endrov.typeNetwork;

import java.util.HashMap;
import java.util.Map;

public class DoubleHistogram
	{

	public Map<Double,Integer> hist=new HashMap<Double, Integer>();
	
	
	public void count(double val)
		{
		count(val, 1);
		}
	public void count(double val, int c)
		{
		Integer count=hist.get(val);
		if(count==null)
			count=c;
		else
			count+=c;
		hist.put(val, count);
		}
	
	
	public void createEmptyIntegerBins(int from, int to)
		{
		for(int i=from;i<to;i++)
			count(i,0);
		}
	
	}
