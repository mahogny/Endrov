/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowNoise;

import java.util.Random;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * Add exponential noise
 * O(n)
 *
 * @author Johan Henriksson
 * 
 */
public class EvOpImageNoiseExponential extends EvOpSlice1
	{
	private final Number lambda;
	
	public EvOpImageNoiseExponential(Number lambda)
		{
		this.lambda=lambda;
		}

	@Override
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return apply(p[0], lambda);
		}

	

	private static double nextExponential(Random r, double b) 
		{
		double randx;
		double result;
		randx = r.nextDouble();
		result = -1*b*Math.log(randx);
		return result;
		}

	public static EvPixels apply(EvPixels image, Number lambda)
		{
		image=image.getReadOnly(EvPixelsType.DOUBLE);
		double[] arr=image.getArrayDouble();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,image.getWidth(),image.getHeight());
		double[] outarr=out.getArrayDouble();
		
		Random rand=new Random();
		double vlambda=lambda.doubleValue();
		for(int i=0;i<arr.length;i++)
			outarr[i]=arr[i]+nextExponential(rand, vlambda);
		
		return out;
		}
	}
