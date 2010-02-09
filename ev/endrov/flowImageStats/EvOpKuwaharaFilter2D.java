/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowImageStats;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.CumSumArea;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Kuwahara filter
 * 
 * http://www.ph.tn.tudelft.nl/Courses/FIP/noframes/fip-Smoothin.html#Heading88
 * 
 * pw=ph=0 corresponds to the identity operation
 * 
 * Complexity O(w*h)
 */
public class EvOpKuwaharaFilter2D extends EvOpSlice1
	{
	private final Number pw, ph;
	
	public EvOpKuwaharaFilter2D(Number pw, Number ph)
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
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] outPixels=out.getArrayDouble();
		
		
		CumSumArea cumsum=new CumSumArea(in);
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

				double var1=cumsum2.integralFromCumSumDouble(fromx, ax, fromy, ay);
				double var2=cumsum2.integralFromCumSumDouble(ax, ax+1,  fromy, ay);
				double var3=cumsum2.integralFromCumSumDouble(ax+1, tox, fromy, ay);
				
				double var4=cumsum2.integralFromCumSumDouble(fromx, ax, ay, ay+1);
				double var5=cumsum2.integralFromCumSumDouble(ax, ax+1,  ay, ay+1);
				double var6=cumsum2.integralFromCumSumDouble(ax+1, tox, ay, ay+1);

				double var7=cumsum2.integralFromCumSumDouble(fromx, ax, ay+1, toy);
				double var8=cumsum2.integralFromCumSumDouble(ax, ax+1,  ay+1, toy);
				double var9=cumsum2.integralFromCumSumDouble(ax+1, tox, ay+1, toy);

				double sum1=cumsum.integralFromCumSumDouble(fromx, ax, fromy, ay);
				double sum2=cumsum.integralFromCumSumDouble(ax, ax+1,  fromy, ay);
				double sum3=cumsum.integralFromCumSumDouble(ax+1, tox, fromy, ay);
				
				double sum4=cumsum.integralFromCumSumDouble(fromx, ax, ay, ay+1);
				double sum5=cumsum.integralFromCumSumDouble(ax, ax+1,  ay, ay+1);
				double sum6=cumsum.integralFromCumSumDouble(ax+1, tox, ay, ay+1);

				double sum7=cumsum.integralFromCumSumDouble(fromx, ax, ay+1, toy);
				double sum8=cumsum.integralFromCumSumDouble(ax, ax+1,  ay+1, toy);
				double sum9=cumsum.integralFromCumSumDouble(ax+1, tox, ay+1, toy);


				//TODO optimization: in the middle, these regions are of equal size
				//(pw+1)*(ph+1); normally
				int part1245=(ax+1-fromx)*(ay+1-fromy);
				int part2356=((tox-ax)*(ay+1-fromy));
				int part5689=((tox-ax)*(toy-ay));
				int part4578=((ax+1-fromx)*(toy-ay));
				
				double mean1245=(sum1+sum2+sum4+sum5)/part1245;
				double mean2356=(sum2+sum3+sum5+sum6)/part2356;
				double mean5689=(sum5+sum6+sum8+sum9)/part5689;
				double mean4578=(sum4+sum5+sum7+sum8)/part4578;
				
				//Find kuwahara region with least variance
				//Var(x)=E(x^2)-(E(x))^2
				double varArr[]=new double[]{
						(var1+var2+var4+var5)/part1245-mean1245*mean1245,
						(var2+var3+var5+var6)/part2356-mean2356*mean2356,
						(var5+var6+var8+var9)/part5689-mean5689*mean5689,
						(var4+var5+var7+var8)/part4578-mean4578*mean4578};
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
					mean=mean1245;
					//mean=(sum1+sum2+sum4+sum5)/part1245;//((ax+1-fromx)*(ay+1-fromy));
				else if(minIndex==1)
					mean=mean2356;
				//mean=(sum2+sum3+sum5+sum6)/part2356;
				else if(minIndex==2)
					mean=mean5689;
					//mean=(sum5+sum6+sum8+sum9)/part5689;
				else
					mean=mean4578;
					//mean=(sum4+sum5+sum7+sum8)/part4578;
				
				//Store mean
				outPixels[ay*w+ax]=mean;
				}
			}
		return out;
		}
	}