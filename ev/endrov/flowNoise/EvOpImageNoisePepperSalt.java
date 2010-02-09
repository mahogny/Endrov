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
 * Apply pepper and salt noise
 * O(n)
 *
 * @author Johan Henriksson
 * 
 */
public class EvOpImageNoisePepperSalt extends EvOpSlice1
	{
	private final Number pPepper;
	private final Number pSalt;
	
	public EvOpImageNoisePepperSalt(Number pPepper, Number pSalt)
		{
		this.pPepper=pPepper;
		this.pSalt=pSalt;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0], pPepper, pSalt);
		}

	

	public static EvPixels apply(EvPixels image, Number pPepper, Number pSalt)
		{
		image=image.getReadOnly(EvPixelsType.DOUBLE);
		double[] arr=image.getArrayDouble();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,image.getWidth(),image.getHeight());
		double[] outarr=out.getArrayDouble();
		
		double vPepper=pPepper.doubleValue();
		double vSalt=pPepper.doubleValue()+vPepper;
		Random rand=new Random();
		for(int i=0;i<arr.length;i++)
			{
			double r=rand.nextDouble();
			if(r>vSalt)
				outarr[i]=arr[i];
			else if(r>vPepper)
				outarr[i]=1000000;
			else
				outarr[i]=0;
			}
		
		return out;
		}
	}
