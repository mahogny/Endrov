package endrov.unsortedImageFilters.imageMath;

import endrov.imageset.EvPixels;

/**
 * Math ops on images. with EvImage, use convenience functions to make a common size and position
 * 
 * Assumes same pixel and position
 * 
 * @author Johan Henriksson
 *
 */
public class EvImageMath
	{

	
	
	/**
	 * Sum up the signal in an image
	 */
	//could always be double if we wanted
	public static double sum(EvPixels a)
		{
		//support all types
		a=a.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		double[] aPixels=a.getArrayDouble();
		double sum=0;
		for(int i=0;i<aPixels.length;i++)
			sum+=aPixels[i];
		return sum;
		}
	
	
	
	
	}
