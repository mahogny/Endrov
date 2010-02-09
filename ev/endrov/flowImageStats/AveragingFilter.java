/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowImageStats;

/**
 * Different averaging filters
 * @author Johan Henriksson
 *
 */
public class AveragingFilter
	{

	//	http://www.ph.tn.tudelft.nl/Courses/FIP/noframes/fip.html


	
	/* out: int
	 * 
	 * 
	 * 	public static EvPixels movingSumQuad(EvPixels in, int pw, int ph)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] outPixels=out.getArrayInt();
		
		EvPixels cumsum=CumSumArea.cumsum(in);
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				outPixels[out.getPixelIndex(ax, ay)]=CumSumArea.integralFromCumSumInteger(cumsum, fromx, tox, fromy, toy);
				}
			}
		return out;
		}

	 */
	
	
	/*
	 * TODO
	 * page 77. other average filters are possible.
	 * u^-1(E[u(p)])  with u(g)=g arithmetic,  u(g)=1/g harmonic,  u(g)=log g geometric
	 */
	
	
	
	
	
	}
