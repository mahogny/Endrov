/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowImageStats;

import endrov.flow.EvOpSlice1;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * Kirsch filter
 * 
 * http://de.wikipedia.org/wiki/Kirsch-Operator
 * 
 * Complexity O(w*h)
 */
public class EvOpKirsch2D extends EvOpSlice1
	{

	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return apply(ph, p[0]);
		}
	
	
	

	public static EvPixels apply(ProgressHandle ph, EvPixels in) 
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] inPixels=in.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		
		int relx[]=new int[]{-1,+0,+1, -1,+1, -1,+0,+1};
		int rely[]=new int[]{+1,+1,+1, +0,+0, -1,-1,-1};
		int weight[][]=new int[][]{
					{5 , 5, 5,   -3,-3,  -3,-3,-3},
					{5 , 5,-3,    5,-3,  -3,-3,-3},
					{5 ,-3,-3,    5,-3,   5,-3,-3},
					{-3,-3,-3,    5,-3,   5, 5,-3},
					
					{-3,-3,-3,   -3,-3,   5, 5, 5},
					{-3,-3,-3,   -3, 5,  -3, 5, 5},
					{-3,-3, 5,   -3, 5,  -3,-3, 5},
					{-3, 5, 5,   -3, 5,  -3,-3,-3}
		};
		/*
		for(int[] onew:weight)
			{
			double sum=0;
			for(int i=0;i<relx.length;i++)
				sum+=onew[i];
			if(sum!=0)
				System.out.println("Code error: kernel does not sum to 0");
			}*/
		
		for(int ay=1;ay<h-1;ay++)
			for(int ax=1;ax<w-1;ax++)
				{
				Double max=null;
				for(int[] onew:weight)
					{
					double sum=0;
					for(int i=0;i<relx.length;i++)
						sum+=inPixels[(ay+rely[i])*w+(ax+relx[i])] * onew[i];
					if(max==null || sum>max)
						max=sum;
					}
				outPixels[ay*w+ax]=max;
				}
		return out;
		}
	}