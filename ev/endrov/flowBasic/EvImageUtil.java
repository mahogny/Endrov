/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic;

import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;

/**
 * Math ops on images. with EvImage, use convenience functions to make a common size and position
 * 
 * Assumes same pixel and position
 * 
 * @author Johan Henriksson
 *
 */
public class EvImageUtil
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

	/**
	 * Minimum intensity in image
	 */
	public static double minValue(EvPixels a)
		{
		a=a.getReadOnly(EvPixelsType.DOUBLE);
		
		double[] aPixels=a.getArrayDouble();
		double ret=Double.MAX_VALUE;
		for(double d:aPixels)
			if(d<ret)
				ret=d;
		return ret;
		}

	/**
	 * Maximum intensity in image
	 */
	public static double maxValue(EvPixels a)
		{
		a=a.getReadOnly(EvPixelsType.DOUBLE);
		
		double[] aPixels=a.getArrayDouble();
		double ret=-Double.MAX_VALUE;
		for(double d:aPixels)
			if(d>ret)
				ret=d;
		return ret;
		}

	
	}
