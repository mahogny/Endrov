package endrov.unsortedImageFilters;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Piecewise continuous histogram transform 
 * @author Johan Henriksson
 *
 */
public class HistogramTransform
	{

	/**
	 * From color, to color.
	 * Map is never inverted
	 */
	
	/**
	 * Never allowed to be empty
	 */
	public SortedMap<Double, Double> points=new TreeMap<Double, Double>();
	
	
	public HistogramTransform()
		{
		//Maybe separate mapping from the widget? can do later
		
		points.put(0.0,0.0);
		points.put(256.0, 256.0);
		
		
		}
	}
