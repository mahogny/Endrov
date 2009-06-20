package endrov.flowAveraging;

import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.CumSumArea;

/**
 * Different averaging filters
 * @author Johan Henriksson
 *
 */
public class AveragingFilter
	{

	//	http://www.ph.tn.tudelft.nl/Courses/FIP/noframes/fip.html
	
	/**
	 * Moving sum. Sum is taken over an area of size (2pw+1)x(2ph+1). pw=ph=0 hence corresponds
	 * to the identity operation. Pixels outside assumed 0.
	 * 
	 * Complexity O(w*h)
	 */
	public static EvPixels movingSumQuad(EvPixels in, int pw, int ph)
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
	
	
	
	
	/*
	 * TODO
	 * page 77. other average filters are possible.
	 * u^-1(E[u(p)])  with u(g)=g arithmetic,  u(g)=1/g harmonic,  u(g)=log g geometric
	 */
	
	
	
	
	
	}
