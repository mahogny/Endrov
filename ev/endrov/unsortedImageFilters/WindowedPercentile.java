package endrov.unsortedImageFilters;

import java.util.ArrayList;
import java.util.Collections;

import endrov.imageset.EvPixels;

/**
 * Compute percentile for a local square area around every pixel
 * @author Johan Henriksson
 *
 */
public class WindowedPercentile
	{
	
	
	/**
	 * Several percentiles can be calculated at the same time; this is because it's very cheap to do several
	 * 
	 * 
	 * @param in Input image
	 * @param pw Pixels to the left and right. 0=just middle
	 * @param ph Pixels to the top and bottom. 0=just middle
	 * @param percentile 0-1
	 */
	public static EvPixels[] run(EvPixels in, int pw, int ph, double... percentile)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out[]=new EvPixels[percentile.length];
		int[] inPixels=in.getArrayInt();
		
		for(int i=0;i<percentile.length;i++)
			out[i]=new EvPixels(in.getType(),w,h);
		
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				{
				ArrayList<Integer> listPixels=new ArrayList<Integer>();
				Collections.sort(listPixels);
				for(int sy=Math.max(0,ay-ph);sy<Math.min(h,ay+h+1);sy++)
					for(int sx=Math.max(0,ax-pw);sx<Math.min(w,ax+w+1);sx++)
						listPixels.add(inPixels[in.getPixelIndex(sx, sy)]);
				
				for(int i=0;i<percentile.length;i++)
					out[i].getArrayInt()[out[i].getPixelIndex(ax, ay)]=listPixels.get((int)((listPixels.size()-1)*percentile[i]));
				}
		
		return out;
		}
	
	//TODO lazy evaluation, apply to imageset!

	}
