package endrov.flowAveraging;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.unsortedImageFilters.CumSumArea;

/**
 * Moving average. Average is taken over an area of size (2pw+1)x(2ph+1). r=0 hence corresponds
 * to the identity operation.
 * 
 * Complexity O(w*h)
 */
public class EvOpMovingAverage extends EvOpSlice1
	{
	Number pw, ph;
	
	public EvOpMovingAverage(Number pw, Number ph)
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
		in=in.getReadOnly(EvPixelsType.INT);
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
				outPixels[out.getPixelIndex(ax, ay)]=cumsum.integralFromCumSumInteger(fromx, tox, fromy, toy)/(int)area;
				}
			}
		return out;
		}
	}