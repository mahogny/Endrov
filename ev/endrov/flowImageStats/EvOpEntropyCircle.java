/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowImageStats;

import java.util.HashMap;

import endrov.flow.EvOpSlice1;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;

/**
 * Moving entropy. Entropy is taken over a circle of radius r. r=0 is a single point
 * 
 * Entropy is defined as S=-sum_i P[i] log(i), where i is intensity
 * 
 * Complexity O(w*h*r*r), could be made faster with a method similar to huangs median calculator
 * 
 */
public class EvOpEntropyCircle extends EvOpSlice1
	{
	private final Number r;
	
	public EvOpEntropyCircle(Number r)
		{
		this.r = r;
		}

	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return apply(ph, p[0],r.intValue());
//		return new EvOpImageMulScalar(-1.0).exec1(new EvOpMovingAverage(pw,ph).exec(new EvOpImageLog().exec(p[0])));
		
		
		//TODO WRONG!!!
		}
	
	
	
	private static void inc(HashMap<Double, Integer> m, Double d)
		{
		Integer cnt=m.get(d);
		cnt=cnt==null?1:cnt+1;
		m.put(d,cnt);
		}
	
	//private static void dec(HashMap<Double, Integer> m, Double d)
	
	
	
	public static EvPixels apply(ProgressHandle ph, EvPixels in, int r)
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		double[] inPixels=in.getArrayDouble();
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		double[] outPixels=out.getArrayDouble();
		
		
		HashMap<Double, Integer> histogram=new HashMap<Double, Integer>();
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				{
				int fromx=Math.max(0,ax-r);
				int tox=Math.min(w,ax+r+1);
				
				int fromy=Math.max(0,ay-r);
				int toy=Math.min(h,ay+r+1);
				int area=(tox-fromx)*(toy-fromy);

				histogram.clear();
				
				//int indexStart=in.getPixelIndex(fromx, fromy);
				for(int y=fromy;y<toy;y++)
					{
					int dy=ay-y;
					for(int x=fromx;x<tox;x++)
						{
						int dx=ax-x;
						if(dx*dx+dy*dy<=r)
							inc(histogram,inPixels[in.getPixelIndex(x, y)]);
						}
					}
				double entropy=0;
				for(int cnt:histogram.values())
					{
					double p=cnt/(double)area;
					entropy-=p*Math.log(p);
					}
				
				outPixels[out.getPixelIndex(ax, ay)]=entropy;
				}
			}
		return out;
		}
	}