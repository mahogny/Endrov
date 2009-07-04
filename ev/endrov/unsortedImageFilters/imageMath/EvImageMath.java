package endrov.unsortedImageFilters.imageMath;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

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
		a=a.getReadOnly(EvPixelsType.DOUBLE);
		
		double[] aPixels=a.getArrayDouble();
		double sum=0;
		for(int i=0;i<aPixels.length;i++)
			sum+=aPixels[i];
		return sum;
		}
	
	
	
	
	}
