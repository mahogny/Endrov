package endrov.flowAveraging;

import endrov.flow.EvOpStack1;
import endrov.flowBasic.CumSumArea;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;

/**
 * Downsample image. This will change the number of pixels but not the size of the total image.
 * Complexity O(w*h)
 */
public class EvOpDownsampleIntegral extends EvOpStack1
	{
	private final Integer scaleX, scaleY;
	
	public EvOpDownsampleIntegral(Integer scaleX, Integer scaleY)
		{
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		}

	public EvStack exec1(EvStack... p)
		{
		return movingSumRect(p[0], scaleX, scaleY);
		}
	
	
	public static EvStack movingSumRect(EvStack in, int scaleX, int scaleY)
		{
		
		EvStack out=new EvStack();
		out.getMetaFrom(in);
		out.resX/=scaleX;
		out.resY/=scaleY;
		
		
		
		
		
		
		
		/*
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
			}*/
		return out;
		}
	
	
	public static EvPixels downSample(EvPixels in, int scaleX, int scaleY)
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
				
				
				
				
				//from ax*scaleX to
				
				
				
				
				
				
				
				/*
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				outPixels[out.getPixelIndex(ax, ay)]=cumsum.integralFromCumSumDouble(fromx, tox, fromy, toy);
				*/
				}
			}
		return out;
		}
	
	}