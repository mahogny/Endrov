package endrov.unsortedImageFilters;

import endrov.imageset.EvPixels;

/**
 * Special convolutions
 * @author Johan Henriksson
 *
 */
public class MiscFilter
	{

	/**
	 * Moving average. Average is taken over an area of size (2pw+1)x(2ph+1). r=0 hence corresponds
	 * to the identity operation.
	 * 
	 * Complexity O(w*h)
	 */
	public static EvPixels movingAverage(EvPixels in, int pw, int ph)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] outPixels=out.getArrayInt();
		
		EvPixels cumsum=CumSum.cumsum(in);
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				int area=(tox-fromx)*(toy-fromy);
				outPixels[out.getPixelIndex(ax, ay)]=CumSum.integralFromCumSum(cumsum, fromx, tox, fromy, toy)/(int)area;
				}
			}
		return out;
		}
	
	
	/**
	 * Moving sum. Sum is taken over an area of size (2pw+1)x(2ph+1). r=0 hence corresponds
	 * to the identity operation. Pixels outside assumed 0.
	 * 
	 * Complexity O(w*h)
	 */
	public static EvPixels movingSum(EvPixels in, int pw, int ph)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] outPixels=out.getArrayInt();
		
		EvPixels cumsum=CumSum.cumsum(in);
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				outPixels[out.getPixelIndex(ax, ay)]=CumSum.integralFromCumSum(cumsum, fromx, tox, fromy, toy);
				}
			}
		return out;
		}
	}
