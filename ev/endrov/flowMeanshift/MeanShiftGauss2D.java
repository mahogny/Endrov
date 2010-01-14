/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeanshift;

import javax.vecmath.Vector2d;

import endrov.flowBasic.math.EvOpImageMulImage;
import endrov.flowGenerateImage.GenerateSpecialImage;
import endrov.flowMultiscale.Multiscale;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Find pixels or classify areas using the Mean-shift algorithm 
 * @author Johan Henriksson
 *
 */
public class MeanShiftGauss2D
	{

	
	/**
	 * Preprocessed image
	 * @author Johan Henriksson
	 *
	 */
	public static class MeanShiftPreProcess
		{
		/**
		 * Cumulative sums
		 */
		EvPixels momentX;
		EvPixels momentY;
		EvPixels moment0;
		
		public MeanShiftPreProcess(EvPixels s)
			{
			int w=s.getWidth();
			int h=s.getHeight();
			EvPixels incX=EvOpImageMulImage.apply(GenerateSpecialImage.genIncX(w, h),s);
			EvPixels incY=EvOpImageMulImage.apply(GenerateSpecialImage.genIncY(w, h),s);
			moment0=s.getReadOnly(EvPixelsType.DOUBLE);
			momentX=incX.getReadOnly(EvPixelsType.DOUBLE);
			momentY=incY.getReadOnly(EvPixelsType.DOUBLE);
			}
		
		
		

		private Vector2d next(Vector2d pos, double sigmaX, double sigmaY)
			{
			//Calculate mean at this position
			double sumX=0;
			double sumY=0;
			double sum0=0;

			sumX+=Multiscale.convolveGaussPoint2D(momentX, sigmaX, sigmaY, pos.x, pos.y);
			sumY+=Multiscale.convolveGaussPoint2D(momentY, sigmaX, sigmaY, pos.x, pos.y);
			sum0+=Multiscale.convolveGaussPoint2D(moment0, sigmaX, sigmaY, pos.x, pos.y);
				
			//Turn into local moment
			sumX/=sum0;
			sumY/=sum0;
			
			return new Vector2d(sumX,sumY);
			}
		
		/**
		 * Iterate toward convergence for a position
		 */
		public Vector2d iterate(Vector2d pos, double sigmaX, double sigmaY)
			{
			//For our purpose, not enough z-resolution to build up a table. have to track position with fraction plane precision.
			
			Vector2d lastPos;
			for(;;)
				{
				lastPos=pos;
				pos=next(pos,sigmaX,sigmaY);
				Vector2d diff=new Vector2d(pos);
				diff.sub(lastPos);
				double okdiff=0.002; //Has huge influence on number of found candidates. This is a hand-tuned value.
				if(diff.lengthSquared()<okdiff*okdiff)
					return pos;
				}
			}

		}
	
	
	}
