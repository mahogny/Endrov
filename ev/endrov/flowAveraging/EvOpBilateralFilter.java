package endrov.flowAveraging;

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
public class EvOpBilateralFilter extends EvOpSlice1
	{
	private Number pw, ph, threshold;
	
	public EvOpBilateralFilter(Number pw, Number ph, Number threshold)
		{
		this.pw = pw;
		this.ph = ph;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return bilateralFilter(p[0],pw.intValue(), ph.intValue(), threshold.intValue());
		}
	
	public static EvPixels bilateralFilter(EvPixels in, int pw, int ph, int threshold)
		{
		in=in.getReadOnly(EvPixelsType.INT);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				
				int sum=0;
				int num=0;

				int curp=inPixels[in.getPixelIndex(ax, ay)];
				for(int y=fromy;y<toy;y++)
					for(int x=fromx;x<tox;x++)
						{
						
						int p=inPixels[in.getPixelIndex(x, y)];
						int dp=p-curp;
						if(dp>-threshold && dp<threshold)
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