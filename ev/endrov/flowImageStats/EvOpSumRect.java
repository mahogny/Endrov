/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowImageStats;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.CumSumArea;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * Moving sum. Sum is taken over an area of size (2pw+1)x(2ph+1). r=0 hence corresponds
 * to the identity operation.
 * 
 * Complexity O(w*h)
 */
public class EvOpSumRect extends EvOpSlice1
	{
	private final Number pw, ph;
	
	public EvOpSumRect(Number pw, Number ph)
		{
		this.pw = pw;
		this.ph = ph;
		}

	public EvPixels exec1(ProgressHandle progh, EvPixels... p)
		{
		return movingSumRect(progh, p[0], pw.intValue(), ph.intValue());
		}
	
	
	/**
	 * TODO +
	 * 
	 *  !
	 * #!#
	 *  !
	 *  
	 * r radius
	 * b width of strip
	 * 
	 * b = (pi/2-1) r ~=0.57 r
	 * 
	 */
	
	
	/**
	 * Moving sum. Sum is taken over an area of size (2pw+1)x(2ph+1). pw=ph=0 hence corresponds
	 * to the identity operation. Pixels outside assumed 0.
	 * 
	 * Complexity O(w*h)
	 */
	public static EvPixels movingSumRect(ProgressHandle progh, EvPixels in, int pw, int ph)
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] outPixels=out.getArrayDouble();
		
		CumSumArea cumsum=new CumSumArea(in);
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				outPixels[out.getPixelIndex(ax, ay)]=cumsum.integralFromCumSumDouble(fromx, tox, fromy, toy);
				}
			}
		return out;
		}
	
	
	}