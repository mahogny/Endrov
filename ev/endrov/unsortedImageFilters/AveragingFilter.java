package endrov.unsortedImageFilters;

import endrov.imageset.EvPixels;

/**
 * Different averaging filters
 * @author Johan Henriksson
 *
 */
public class AveragingFilter
	{

	//	http://www.ph.tn.tudelft.nl/Courses/FIP/noframes/fip.html
	
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
	
	
	
	/**
	 * Local average, but only average using pixels with threshold of current pixel value. This improves edge conservation
	 * 
	 * O(w*h*pw*ph)
	 * 
	 * http://www.roborealm.com/help/Bilateral.php
	 */
	public static EvPixels bilateralFilter(EvPixels in, int pw, int ph, int threshold)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
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
	
	
	/**
	 * Local variance
	 * 
	 * TODO move to other class
	 * TODO should maybe output floating point due to division
	 */
	public static EvPixels localVariance(EvPixels in, int pw, int ph)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] outPixels=out.getArrayInt();
		
		
		EvPixels cumsum=CumSum.cumsum(in);
		EvPixels cumsum2=CumSum.cumsum2(in);
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				
				//Var(x)=E(x^2)-(E(x))^2

				int v1=CumSum.integralFromCumSum(cumsum2, fromx, tox, fromy, toy);
				int v2=CumSum.integralFromCumSum(cumsum, fromx, tox, fromy, toy);
				
				outPixels[out.getPixelIndex(ax, ay)]=v1 - v2*v2;
				}
			}
		return out;
		}
	
	/**
	 * Kuwahara filter
	 * 
	 * http://www.ph.tn.tudelft.nl/Courses/FIP/noframes/fip-Smoothin.html#Heading88
	 * 
	 * TODO implement properly
	 * 
	 * Complexity O(w*h)
	 */
	public static EvPixels kuwahara(EvPixels in, int pw, int ph) //Note strange notation L
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] outPixels=out.getArrayInt();
		
		//Var(x)=E(x^2)-(E(x))^2
		
		EvPixels cumsum=CumSum.cumsum(in);
		EvPixels cumsum2=CumSum.cumsum2(in);
		
		
		
		//TODO
		
		
		/*
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
		*/
		return out;
		}
	
	
	/*
	 * TODO
	 * page 77. other average filters are possible.
	 * u^-1(E[u(p)])  with u(g)=g arithmetic,  u(g)=1/g harmonic,  u(g)=log g geometric
	 */
	
	
	
	
	
	}
