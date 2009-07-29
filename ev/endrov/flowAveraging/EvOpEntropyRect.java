package endrov.flowAveraging;

import java.util.Arrays;
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
		}
	
	
	/*
	private static void inc(HashMap<Double, Integer> m, Double d)
		{
		Integer cnt=m.get(d);
		cnt=cnt==null?1:cnt+1;
		m.put(d,cnt);
		}*/
	
	public static EvPixels apply(EvPixels in, int pw, int ph)
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		double[] inPixels=in.getArrayDouble();
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		double[] outPixels=out.getArrayDouble();


		//HashMap<Double, Integer> histogram=new HashMap<Double, Integer>();
//		LinkedHashMap<Double, Integer> linkedhistogram=new LinkedHashMap<Double, Integer>();

		/**
		 * Optimization: Specializing code, one near border and one not near
		 * Time before: 1861
		 * Time after: 1735
		 */

		/**
		 * Optimization: no hashmap, put in a list, sort and go through linearly. Higher complexity but...
		 * Time before: 1849
		 * Time after: 1314
		 * 
		 * For 4x4. Faster also for 8x8. I suspect the reason is that .values() goes through all hashmap, and there might be more than there are pixels 
		 * 
		 * for 50x50:
		 * t1 175343
		 * t2 137498
		 * 
		 * could try and make a specialized hash that keeps track of indices
		 * http://java.sun.com/j2se/1.4.2/docs/api/java/util/LinkedHashMap.html   option!
		 * 4x4 1766 ordinary hash, 1656 linked hash
		 * 
		 * 20x20 30820 ordinary, 28207 linked hash, 211729 sort
		 */

		/**
		 * Optimization: removing one branch in sorted version goes approximately 1300 -> 1200
		 */
	/*	
		long t1=System.currentTimeMillis();
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				nearBorderSorting(inPixels, w, h, outPixels, pw, ph, ax, ay);
		System.out.println("t1 "+(System.currentTimeMillis()-t1));

		long t2=System.currentTimeMillis();
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				nearBorderSorting2(inPixels, w, h, outPixels, pw, ph, ax, ay);
		System.out.println("t2 "+(System.currentTimeMillis()-t2));
*/
		//long t3=System.currentTimeMillis();
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				nearBorderSorting3(inPixels, w, h, outPixels, pw, ph, ax, ay);
		//System.out.println("t3 "+(System.currentTimeMillis()-t3));

		/*
		long t1=System.currentTimeMillis();
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				nearBorder(histogram, inPixels, w, h, outPixels, pw, ph, ax, ay);
		System.out.println("t1 "+(System.currentTimeMillis()-t1));
		

		long t2=System.currentTimeMillis();
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				nearBorderSorting(histogram, inPixels, w, h, outPixels, pw, ph, ax, ay);
		System.out.println("t2 "+(System.currentTimeMillis()-t2));


		long t3=System.currentTimeMillis();
		for(int ay=0;ay<h;ay++)
			{
			for(int ax=0;ax<w;ax++)
				nearBorderLinked(linkedhistogram, inPixels, w, h, outPixels, pw, ph, ax, ay);
//			System.out.println("line "+ay);
			}
		System.out.println("t3 "+(System.currentTimeMillis()-t3));
*/
		
		
		/*
		long t2=System.currentTimeMillis();
		int ay=0;
		for(;ay<ph;ay++)
			for(int ax=0;ax<w;ax++)
				nearBorder(histogram, inPixels, w, h, outPixels, pw, ph, ax, ay);
		for(;ay<h-ph;ay++)
			{
			int ax=0;
			for(;ax<pw;ax++)
				nearBorder(histogram, inPixels, w, h, outPixels, pw, ph, ax, ay);
			for(;ax<w-pw;ax++)
				fullyInside(histogram, inPixels, w, h, outPixels, pw, ph, ax, ay);
			for(;ax<w;ax++)
				nearBorder(histogram, inPixels, w, h, outPixels, pw, ph, ax, ay);
			}
		for(;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				nearBorder(histogram, inPixels, w, h, outPixels, pw, ph, ax, ay);
		System.out.println("t2 "+(System.currentTimeMillis()-t2));
		*/
		
		return out;
		}
	
	
	/*
	private static void nearBorder(HashMap<Double, Integer> histogram, double[] inPixels, int w, int h, double[] outPixels, int pw, int ph, int ax, int ay)
		{
		int fromx=Math.max(0,ax-pw);
		int tox=Math.min(w,ax+pw+1);

		int fromy=Math.max(0,ay-ph);
		int toy=Math.min(h,ay+ph+1);
		int area=(tox-fromx)*(toy-fromy);

		histogram.clear();

		int inIndexStart=w*fromy+fromx;//in.getPixelIndex(fromx, fromy);
		for(int y=fromy;y<toy;y++)
			{
			int index=inIndexStart;
			for(int x=fromx;x<tox;x++)
				{
				inc(histogram,inPixels[index]);
				index++;
				}
			inIndexStart+=w;
			}
		double entropy=0;
		for(int cnt:histogram.values())
			{
			double p=cnt/(double)area;
			entropy-=p*Math.log(p);
			}

		outPixels[ay*w+ax]=entropy;
		}
	*/
	
	/*
	private static void nearBorderLinked(LinkedHashMap<Double, Integer> histogram, double[] inPixels, int w, int h, double[] outPixels, int pw, int ph, int ax, int ay)
		{
		int fromx=Math.max(0,ax-pw);
		int tox=Math.min(w,ax+pw+1);

		int fromy=Math.max(0,ay-ph);
		int toy=Math.min(h,ay+ph+1);
		int area=(tox-fromx)*(toy-fromy);

		histogram.clear();

		int inIndexStart=w*fromy+fromx;//in.getPixelIndex(fromx, fromy);
		for(int y=fromy;y<toy;y++)
			{
			int index=inIndexStart;
			for(int x=fromx;x<tox;x++)
				{
				inc(histogram,inPixels[index]);
				index++;
				}
			inIndexStart+=w;
			}
		double entropy=0;
		for(int cnt:histogram.values())
			{
			double p=cnt/(double)area;
			entropy-=p*Math.log(p);
			}

		outPixels[ay*w+ax]=entropy;
		}
	*/
	
	/*
	private static void nearBorderSorting(double[] inPixels, int w, int h, double[] outPixels, int pw, int ph, int ax, int ay)
		{
		int fromx=Math.max(0,ax-pw);
		int tox=Math.min(w,ax+pw+1);

		int fromy=Math.max(0,ay-ph);
		int toy=Math.min(h,ay+ph+1);
		int area=(tox-fromx)*(toy-fromy);

		double[] vals=new double[area];

		int apos=0;
		int inIndexStart=w*fromy+fromx;//in.getPixelIndex(fromx, fromy);
		for(int y=fromy;y<toy;y++)
			{
			int index=inIndexStart;
			for(int x=fromx;x<tox;x++)
				{
				vals[apos]=inPixels[index];
				index++;
				apos++;
				}
			inIndexStart+=w;
			}
		Arrays.sort(vals);
		double entropy=0;
		int i=1;
		double curval=vals[0];
		int cnt=1;
		double iarea=1.0/area;
		while(i<area)
			{
			double nextval=vals[i];
			if(nextval!=curval)
				{
				double p=cnt*iarea;
				entropy-=p*Math.log(p);
				if(i<area-1)
					{
					curval=vals[i+1];
					cnt=1;
					i+=2;
					}
				else
					{
					cnt=0;
					i++;
					}
				}
			else
				i++;
			}
		if(cnt!=0)
			{
			double p=cnt/(double)area;
			entropy-=p*Math.log(p);
			}
		
		outPixels[ay*w+ax]=entropy;
		}
	
	
	
	private static void nearBorderSorting2(double[] inPixels, int w, int h, double[] outPixels, int pw, int ph, int ax, int ay)
		{
		int fromx=Math.max(0,ax-pw);
		int tox=Math.min(w,ax+pw+1);

		int fromy=Math.max(0,ay-ph);
		int toy=Math.min(h,ay+ph+1);
		int area=(tox-fromx)*(toy-fromy);

		double[] vals=new double[area];

		int apos=0;
		int inIndexStart=w*fromy+fromx;//in.getPixelIndex(fromx, fromy);
		for(int y=fromy;y<toy;y++)
			{
			int index=inIndexStart;
			for(int x=fromx;x<tox;x++)
				{
				vals[apos]=inPixels[index];
				index++;
				apos++;
				}
			inIndexStart+=w;
			}
		Arrays.sort(vals);
		double entropy=0;
		int i=1;
		double iarea=1.0/area;
		int cnt=0;
		major: while(i<area)
			{
			double curval=vals[i];
			cnt=1;
			while(i<area)
				{
				double nextval=vals[i];
				if(nextval!=curval)
					{
					double p=cnt*iarea;
					entropy-=p*Math.log(p);
					i++;
					continue major;
					}
				else
					{
					cnt++;
					i++;
					}
				}
			}		
		if(cnt!=0)
			{
			double p=cnt*iarea;
			entropy-=p*Math.log(p);
			}
		outPixels[ay*w+ax]=entropy;
		}
	*/
	
	
	private static void nearBorderSorting3(double[] inPixels, int w, int h, double[] outPixels, int pw, int ph, int ax, int ay)
		{
		int fromx=Math.max(0,ax-pw);
		int tox=Math.min(w,ax+pw+1);

		int fromy=Math.max(0,ay-ph);
		int toy=Math.min(h,ay+ph+1);
		int area=(tox-fromx)*(toy-fromy);

		double[] vals=new double[area];

		int apos=0;
		int inIndexStart=w*fromy+fromx;//in.getPixelIndex(fromx, fromy);
		for(int y=fromy;y<toy;y++)
			{
			int index=inIndexStart;
			for(int x=fromx;x<tox;x++)
				{
				vals[apos]=inPixels[index];
				index++;
				apos++;
				}
			inIndexStart+=w;
			}
		Arrays.sort(vals);
		double entropy=0;
		int i=1;
		double curval=vals[0];
		int cnt=1;
		double iarea=1.0/area;
		while(i<area-1) //Leaving one position means one if can be skipped for the double jump
			{
			double nextval=vals[i];
			if(nextval!=curval)
				{
				double p=cnt*iarea;
				entropy-=p*Math.log(p);
				curval=vals[i+1];
				cnt=1;
				i+=2;
				}
			else
				i++;
			}
		if(i==area)
			{
			//Just took a double step, hence this last pixel is different 
			double p=iarea;
			entropy-=p*Math.log(p);
			}
		else
			{
			//Last checked pixel was the same type. Check if there is one more of the same or not
			double nextval=vals[i];
			if(nextval==curval)
				{
				cnt++;
				double p=cnt*iarea;
				entropy-=p*Math.log(p);
				}
			else
				{
				//The ones so far
				double p=cnt*iarea; 
				entropy-=p*Math.log(p);
				//And the last pixel
				p=iarea;
				entropy-=p*Math.log(p);
				}		
			}
		
	
		
		outPixels[ay*w+ax]=entropy;
		}
	
	
	
	
	/*
	private static void fullyInside(HashMap<Double, Integer> histogram, double[] inPixels, int w, int h, double[] outPixels, int pw, int ph, int ax, int ay)
		{
				int fromx=ax-pw;
				int tox=ax+pw+1;
				
				int fromy=ay-ph;
				int toy=ay+ph+1;
				int area=(tox-fromx)*(toy-fromy);

				histogram.clear();
				
				int inIndexStart=w*fromy+fromx;//in.getPixelIndex(fromx, fromy);
				for(int y=fromy;y<toy;y++)
					{
					int index=inIndexStart;
					for(int x=fromx;x<tox;x++)
						{
						inc(histogram,inPixels[index]);
						index++;
						}
					inIndexStart+=w;
					}
				double entropy=0;
				for(int cnt:histogram.values())
					{
					double p=cnt/(double)area;
					entropy-=p*Math.log(p);
					}
				
				outPixels[ay*w+ax]=entropy;
		}
*/
	
	
	}



/*




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
	
	int inIndexStart=in.getPixelIndex(fromx, fromy);
	for(int y=fromy;y<toy;y++)
		{
		int index=inIndexStart;
		for(int x=fromx;x<tox;x++)
			{
			inc(histogram,inPixels[index]);
			index++;
			}
		inIndexStart+=w;
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


*/