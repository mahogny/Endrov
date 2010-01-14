/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowImageStats;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.CumSumLine;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Moving sum. Sum is taken over a circle with given radius.
 * 
 * Complexity O(w*h*r)
 */
public class EvOpSumCircle extends EvOpSlice1
	{
	private final Number pw;
	
	public EvOpSumCircle(Number pw)
		{
		this.pw = pw;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0], pw.intValue());
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
	public static EvPixels apply(EvPixels in, int iradius)
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] outPixels=out.getArrayDouble();
		
		CumSumLine cumsum=new CumSumLine(in);
		
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{

				double sum=0;
				for(int dy=-iradius;dy<iradius;dy++)
					{
					int toty=ay-dy;
					if(toty<0 || toty>=h)
						continue;
					int dx=(int)Math.sqrt(iradius*iradius-dy*dy);
					
					int fromx=Math.max(0,ax-dx);
					int tox=Math.min(w,ax+dx+1);
					
					sum+=cumsum.integralLineFromCumSumDouble(fromx, tox, toty);
					}
				
				outPixels[out.getPixelIndex(ax, ay)]=sum;
				}
			}
		return out;
		}
	
	
	}