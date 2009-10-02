package endrov.flowImageStats;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.CumSumArea;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Kirsch filter
 * 
 * http://de.wikipedia.org/wiki/Kirsch-Operator
 * 
 * Complexity O(w*h)
 */
public class EvOpKirsch2D extends EvOpSlice1
	{

	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0]);
		}
	
	
	

	public static EvPixels apply(EvPixels in) 
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] inPixels=in.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		for(int ay=1;ay<h-1;ay++)
			{
			for(int ax=1;ax<w-1;ax++)
				{
				int relx[]=new int[]{-1,+0,+1, -1,+1, -1,+0,+1};
				int rely[]=new int[]{+1,+1,+1, +0,+0, +0,+0,+0};
				int weight[][]=new int[][]{
							{5,5,5,     -3,-3, -3,-3,-3},
							{5,5,3,      5,-3, -3,-3,-3},
							{5,-3,-3,    5,-3,  5,-3,-3},
							{-3,-3,-3,   5,-3,  5,5,-3},
							
							{-3,-3,-3,  -3,-3,  5,5,5},
							{-3,-3,-3,  -3,5,  -3,5,5},
							{-3,-3,5,   -3,5,  -3,-3,5},
							{-3,5,5,    -3,5,  -3,-3,-3}
				};

				Double max=null;
				for(int[] onew:weight)
					{
					double sum=0;
					for(int i=0;i<relx.length;i++)
						sum+=inPixels[(ay+rely[i])*w+ax+relx[i]]*onew[i];
					if(max==null || sum>max)
						max=sum;
					}
				
				//Store final value
				outPixels[ay*w+ax]=max;
				}
			}
		return out;
		}
	}