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

/**
 * Add poisson noise
 * O(n)
 *
 * @author Johan Henriksson
 * 
 */
public class EvOpImageNoisePoisson extends EvOpSlice1
	{
	private final Number lambda;
	
	public EvOpImageNoisePoisson(Number lambda)
		{
		this.lambda=lambda;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0], lambda);
		}


	private static int nextPoisson(Random r, double lambda) 
		{
		double elambda = Math.exp(-1*lambda);
		double product = 1;
		int count =  0;
		int result=0;
		while (product >= elambda)
			{
			product *= r.nextDouble();
			result = count;
			count++; // keep result one behind
			}
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
			outarr[i]=arr[i]+nextPoisson(rand, vlambda);
		
		return out;
		}
	}
