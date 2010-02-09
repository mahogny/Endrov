/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowImageStats;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.CumSumArea;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Moving average. Average is taken over an area of size (2pw+1)x(2ph+1). r=0 hence corresponds
 * to the identity operation.
 * 
 * Complexity O(w*h)
 */
public class EvOpAverageRect extends EvOpSlice1
	{
	private final Number pw, ph;
	
	public EvOpAverageRect(Number pw, Number ph)
		{
		this.pw = pw;
		this.ph = ph;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0],pw.intValue(), ph.intValue());
		}
	
	public static EvPixels apply(EvPixels in, int pw, int ph)
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] outPixels=out.getArrayDouble();
		
		//EvPixels cumsum=CumSumArea.cumsum(in);
		CumSumArea cumsum=new CumSumArea(in);
		
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				int area=(tox-fromx)*(toy-fromy);
				//outPixels[out.getPixelIndex(ax, ay)]=CumSumArea.integralFromCumSumInteger(cumsum, fromx, tox, fromy, toy)/(int)area;
				outPixels[out.getPixelIndex(ax, ay)]=cumsum.integralFromCumSumDouble(fromx, tox, fromy, toy)/(int)area;
				}
			}
		return out;
		}
	}