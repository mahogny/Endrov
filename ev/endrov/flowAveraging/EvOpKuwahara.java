package endrov.flowAveraging;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.unsortedImageFilters.CumSumArea;

/**
 * Kuwahara filter
 * 
 * http://www.ph.tn.tudelft.nl/Courses/FIP/noframes/fip-Smoothin.html#Heading88
 * 
 * pw=ph=0 corresponds to the identity operation
 * 
 * Complexity O(w*h)
 */
public class EvOpKuwahara extends EvOpSlice1
	{
	Number pw, ph;
	
	public EvOpKuwahara(Number pw, Number ph)
		{
		this.pw = pw;
		this.ph = ph;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0],pw.intValue(), ph.intValue());
		}
	
	
	

	public static EvPixels apply(EvPixels in, int pw, int ph) //Note strange notation L
		{
		in=in.getReadOnly(EvPixelsType.INT);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] outPixels=out.getArrayDouble();
		
		//Var(x)=E(x^2)-(E(x))^2
		
		CumSumArea cumsum=new CumSumArea(in);
//		EvPixels cumsum=CumSumArea.cumsum(in);
		CumSumArea cumsum2=CumSumArea.cumsum2(in);
		
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				
				/**
				 * Regions named like this:
				 * 1 2 3
				 * 4 5 6
				 * 7 8 9
				 * 
				 * When comparing variance, can compare without the division.
				 * This is useful for summing variance between the regions
				 */

				/*
				double var1=CumSumArea.integralFromCumSumInteger(cumsum2, fromx, ax, fromy, ay);
				double var2=CumSumArea.integralFromCumSumInteger(cumsum2, ax, ax+1, fromy, ay);
				double var3=CumSumArea.integralFromCumSumInteger(cumsum2, ax+1, tox, fromy, ay);
				
				double var4=CumSumArea.integralFromCumSumInteger(cumsum2, fromx, ax, ay, ay+1);
				double var5=CumSumArea.integralFromCumSumInteger(cumsum2, ax, ax+1, ay, ay+1);
				double var6=CumSumArea.integralFromCumSumInteger(cumsum2, ax+1, tox, ay, ay+1);

				double var7=CumSumArea.integralFromCumSumInteger(cumsum2, fromx, ax, ay+1, toy);
				double var8=CumSumArea.integralFromCumSumInteger(cumsum2, ax, ax+1, ay+1, toy);
				double var9=CumSumArea.integralFromCumSumInteger(cumsum2, ax+1, tox, ay+1, toy);
*/
				double var1=cumsum2.integralFromCumSumInteger(fromx, ax, fromy, ay);
				double var2=cumsum2.integralFromCumSumInteger(ax, ax+1, fromy, ay);
				double var3=cumsum2.integralFromCumSumInteger(ax+1, tox, fromy, ay);
				
				double var4=cumsum2.integralFromCumSumInteger(fromx, ax, ay, ay+1);
				double var5=cumsum2.integralFromCumSumInteger(ax, ax+1, ay, ay+1);
				double var6=cumsum2.integralFromCumSumInteger(ax+1, tox, ay, ay+1);

				double var7=cumsum2.integralFromCumSumInteger(fromx, ax, ay+1, toy);
				double var8=cumsum2.integralFromCumSumInteger(ax, ax+1, ay+1, toy);
				double var9=cumsum2.integralFromCumSumInteger(ax+1, tox, ay+1, toy);


/*				
				double sum1=CumSumArea.integralFromCumSumInteger(cumsum, fromx, ax, fromy, ay);
				double sum2=CumSumArea.integralFromCumSumInteger(cumsum, ax, ax+1, fromy, ay);
				double sum3=CumSumArea.integralFromCumSumInteger(cumsum, ax+1, tox, fromy, ay);
				
				double sum4=CumSumArea.integralFromCumSumInteger(cumsum, fromx, ax, ay, ay+1);
				double sum5=CumSumArea.integralFromCumSumInteger(cumsum, ax, ax+1, ay, ay+1);
				double sum6=CumSumArea.integralFromCumSumInteger(cumsum, ax+1, tox, ay, ay+1);

				double sum7=CumSumArea.integralFromCumSumInteger(cumsum, fromx, ax, ay+1, toy);
				double sum8=CumSumArea.integralFromCumSumInteger(cumsum, ax, ax+1, ay+1, toy);
				double sum9=CumSumArea.integralFromCumSumInteger(cumsum, ax+1, tox, ay+1, toy);
	*/
				double sum1=cumsum.integralFromCumSumInteger(fromx, ax, fromy, ay);
				double sum2=cumsum.integralFromCumSumInteger(ax, ax+1, fromy, ay);
				double sum3=cumsum.integralFromCumSumInteger(ax+1, tox, fromy, ay);
				
				double sum4=cumsum.integralFromCumSumInteger(fromx, ax, ay, ay+1);
				double sum5=cumsum.integralFromCumSumInteger(ax, ax+1, ay, ay+1);
				double sum6=cumsum.integralFromCumSumInteger(ax+1, tox, ay, ay+1);

				double sum7=cumsum.integralFromCumSumInteger(fromx, ax, ay+1, toy);
				double sum8=cumsum.integralFromCumSumInteger(ax, ax+1, ay+1, toy);
				double sum9=cumsum.integralFromCumSumInteger(ax+1, tox, ay+1, toy);
				

				//Find kuwahara region with least variance
				double varArr[]=new double[]{
						var1+var2+var4+var5,
						var2+var3+var5+var6,
						var5+var6+var8+var9,
						var4+var5+var7+var8};
				double min=varArr[1];
				int minIndex=0;
				for(int i=1;i<varArr.length;i++)
					{
					if(varArr[i]<min)
						{
						min=varArr[i];
						minIndex=i;
						}
					}
				
				//Find mean of this region. One complication is that the area near borders is
				//different. algorithm could be optimized by treating inner area differently.
				double mean;
				if(minIndex==0)
					mean=(sum1+sum2+sum4+sum5)/((ax+1-fromx)*(ay+1-fromy));
				else if(minIndex==1)
					mean=(sum2+sum3+sum5+sum6)/((tox-ax)*(ay+1-fromy));
				else if(minIndex==2)
					mean=(sum5+sum6+sum8+sum9)/((tox-ax)*(toy-ay));
				else
					mean=(sum4+sum5+sum7+sum8)/((ax+1-fromy)*(toy-ay));
				
				//Store mean
				outPixels[out.getPixelIndex(ax, ay)]=mean;
				}
			}
		return out;
		}
	}