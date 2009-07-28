package endrov.flowAveraging;

import java.util.HashMap;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * Moving entropy. Entropy is taken over an area of size (2pw+1)x(2ph+1).
 * 
 * Entropy is defined as S=-sum_i P[i] log(i), where i is intensity
 * 
 * Complexity O(w*h*pw*ph), could be made faster with a method similar to huangs median calculator
 * 
 */
public class EvOpEntropyRect extends EvOpSlice1
	{
	private final Number pw, ph;
	
	public EvOpEntropyRect(Number pw, Number ph)
		{
		this.pw = pw;
		this.ph = ph;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0],pw.intValue(),ph.intValue());
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
	
	
	
	public static EvPixels apply(EvPixels in, int pw, int ph)
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
				int fromx=Math.max(0,ax-pw);
				int tox=Math.min(w,ax+pw+1);
				
				int fromy=Math.max(0,ay-ph);
				int toy=Math.min(h,ay+ph+1);
				int area=(tox-fromx)*(toy-fromy);

				histogram.clear();
				
				//int indexStart=in.getPixelIndex(fromx, fromy);
				for(int y=fromy;y<toy;y++)
					{
					for(int x=fromx;x<tox;x++)
						{
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