/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeanshift;

import javax.vecmath.Vector2d;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Find pixels or classify areas using the Mean-shift algorithm 
 * @author Johan Henriksson
 *
 */
public class MeanShiftGauss1D
	{

	
	/**
	 * Preprocessed image
	 * @author Johan Henriksson
	 *
	 */
	public static class MeanShiftPreProcess
		{
		
		MeanShiftGauss2D.MeanShiftPreProcess ms;
		
		public MeanShiftPreProcess(double[] s)
			{
			EvPixels p=new EvPixels(EvPixelsType.DOUBLE,s.length,1);
			double[] arr=p.getArrayDouble();
			for(int i=0;i<s.length;i++)
				arr[i]=s[i];
			ms=new MeanShiftGauss2D.MeanShiftPreProcess(p);
			}
		
		
		

		/**
		 * Iterate toward convergence for a position
		 */
		public double iterate(double pos, double sigma)
			{
			return ms.iterate(new Vector2d(pos,0), sigma, sigma).x;
			}

		}
	
	
	}
