package endrov.flowAveraging;

import endrov.flow.EvOpStack1;
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
		return apply(p[0], scaleX, scaleY);
		}
	
	
	public static EvStack apply(EvStack in, int scaleX, int scaleY)
		{
		
		EvStack out=new EvStack();
		out.getMetaFrom(in);
		out.resX*=scaleX; //not quite
		out.resY*=scaleY;
		
		//TODO
		
		return out;
		}
	
	
	public static EvPixels downSample(EvPixels in, int scaleX, int scaleY)
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		double[] inPixels=in.getArrayDouble();
		int w=in.getWidth();
		int h=in.getHeight();
		
		int outw=w/scaleX; //Rounds down. note! affects resolution!!
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,outw,h);
		double[] outPixels=out.getArrayDouble();
		
		//Could do it with cumsum. which is faster?
//		CumSumArea cumsum=new CumSumArea(in);
		
		//First resample in X-direction
		for(int ay=0;ay<h;ay++)
			{
			int ax=0;
			for(int aox=0;aox<outw;aox++)
				{
				double sum=0;
				for(int i=0;i<scaleX;i++)
					{
					sum+=inPixels[ax+ay*w];
					ax++;
					}
				outPixels[aox+ay*outw]=sum;
				}
			}

		//Make current out the new input
		inPixels=outPixels;
		w=outw;
		int outh=h/scaleY;
		out=new EvPixels(EvPixelsType.DOUBLE,outw,outh);
		outPixels=out.getArrayDouble();
		
		//Resample in Y, now with fewer pixels so memory locality should be higher
		for(int ax=0;ax<w;ax++)
			{
			int ay=0;
			for(int aoy=0;aoy<outh;aoy++)
				{
				double sum=0;
				for(int i=0;i<scaleY;i++)
					{
					sum+=inPixels[ax+ay*w];
					ay++;
					}
				outPixels[ax+aoy*outw]=sum;
				}
			}
		
		return out;
		}
	
	}