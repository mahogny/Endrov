/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMisc;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.ProgressHandle;



/**
 * Convolve: in (*) kernel
 * 
 * kernel and in assumed 0 outside area
 * 
 * Complexity O(w*h*kw*kh)
 * 
 * TODO test
 * 
 * LEARNED need to specialize over multiple input formats
 * LEARNED when calling functions to be inlined, these need specialized versions too with tests already done by caller
 * 
 * @author Johan Henriksson
 * 
 */
public class EvOpConvolve2D extends EvOpSlice1
	{
	private Number kcx;
	private Number kcy;
	private EvPixels kernel;
	public EvOpConvolve2D(EvPixels kernel, Number kcx, Number kcy)
		{
		this.kcx = kcx;
		this.kcy = kcy;
		this.kernel = kernel;
		}
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return convolve(ph, p[0], kernel, kcx.intValue(), kcy.intValue());
		}

	public static EvPixels convolve(ProgressHandle ph, EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		in=in.getReadOnly(EvPixelsType.INT);
		kernel=kernel.getReadOnly(EvPixelsType.INT);
		int w=in.getWidth();
		int h=in.getHeight();
		int kw=kernel.getWidth();
		int kh=kernel.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] kernelPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		

		//TODO probably need to support int x double, double x double
		
		//TODO specialize border calculations to avoid branching?
		
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				{
				//Find bounds for integration
				int fromx=Math.max(-kcx,-ax);
				int tox=Math.min(kw-kcx,w-(kw-kcx));
				int fromy=Math.max(-kcy,-ay);
				int toy=Math.min(kh-kcy,h-(kh-kcy));

				//Convolve
				int sum=0;
				for(int kx=fromx;kx<tox;kx++)
					for(int ky=fromy;ky<toy;ky++)
						sum+=inPixels[in.getPixelIndex(ax+kx, ay+ky)]*kernelPixels[kernel.getPixelIndex(kcx+kx, kcy+ky)];
				
				outPixels[out.getPixelIndex(ax, ay)]=sum;
				}
		
		return out;
		}
	
	
	
	

	/**
	 * Optimization:
	 * keep track of kernels, if they are separable or not. assume two separable kernels are applied:
	 * x*y*X*Y*g = x*X*y*Y*g 
	 * 
	 * x*X is extremely cheap, this almost optimizes away both additional convolutions.
	 * 
	 * also, (a*b)*c=a*(b*c), it is very cheap to do a size analysis and figure out the optimal order
	 * 
	 * 
	 * 
	 * 
	 */
	}