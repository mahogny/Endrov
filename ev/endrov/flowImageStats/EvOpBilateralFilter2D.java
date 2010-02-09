/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowImageStats;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Local average, but only average using pixels within threshold of current pixel value. This improves edge conservation
 * <br/>
 * O(w*h*pw*ph)
 * <br/>
 * http://www.roborealm.com/help/Bilateral.php
 */
public class EvOpBilateralFilter2D extends EvOpSlice1
	{
	private final Number pw, ph, threshold;
	
	public EvOpBilateralFilter2D(Number pw, Number ph, Number threshold)
		{
		this.pw = pw;
		this.ph = ph;
		this.threshold=threshold;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0],pw.intValue(), ph.intValue(), threshold.doubleValue());
		}
	
	public static EvPixels apply(EvPixels in, int pw, int ph, double threshold)
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] inPixels=in.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				
				double sum=0;
				int num=0;

				double curp=inPixels[in.getPixelIndex(ax, ay)];
				double lower=curp-threshold;
				double upper=curp+threshold;
				for(int y=fromy;y<toy;y++)
					for(int x=fromx;x<tox;x++)
						{
						
						double p=inPixels[in.getPixelIndex(x, y)];
//						double dp=p-curp;
						if(p>=lower && p<=upper)
							{
							sum+=p;
							num++;
							}
						}
				outPixels[out.getPixelIndex(ax, ay)]=sum/num;
				}
			}
		return out;
		}
	
	}