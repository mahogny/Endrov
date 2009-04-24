package endrov.histEqualizer;

import java.util.SortedMap;
import java.util.TreeMap;

public class HistEqualizer
	{

	/**
	 * From color, to color.
	 * Map is never inverted
	 */
	
	/**
	 * Never allowed to be empty
	 */
	public SortedMap<Double, Double> points=new TreeMap<Double, Double>();
	
	
	public HistEqualizer()
		{
		//Maybe separate mapping from the widget? can do later
		
		points.put(0.0,0.0);
		points.put(256.0, 256.0);
		
		
		}
	}
