package endrov.flowThreshold;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.EvListUtil;

/**
 * Threshold given as percentile
 * 
 * Complexity O(w*h)
 */
public class EvOpThresholdPercentile2D extends Threshold2D
	{
	private final double perc;
	public EvOpThresholdPercentile2D(double perc)
		{
		this.perc = perc;
		}
	
	public double getThreshold(EvPixels in)
		{
		return findThreshold(in, perc);
		}
	public static double findThreshold(EvPixels in, double perc)
		{
		double[] arr=in.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		return EvListUtil.findPercentileDouble(arr, perc);
		}
	}