package endrov.flowImageStats;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.EvListUtil;
import endrov.util.EvMathUtil;

/**
 * Compute percentile for a local square area around every pixel. Percentile should be within [0,1],
 * values outside will be clamped.
 * <br/>
 * Complexity O(w*h*pw*ph)
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpPercentileRect extends EvOpSlice1
	{
	private final Number pw, ph;
	private final Number percentile;
	
	public EvOpPercentileRect(Number pw, Number ph, Number percentile)
		{
		this.pw = pw;
		this.ph = ph;
		this.percentile = percentile;
		}
	
	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0], pw.intValue(), ph.intValue(), percentile.doubleValue());
		}
	
	
	public static EvPixels apply(EvPixels in, int pw, int ph, double percentile)
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] inPixels=in.getArrayDouble();
		double[] outPixels=out.getArrayDouble();
		
		percentile=EvMathUtil.clamp(percentile, 0, 1);
		if(percentile==0)
			{
			//Special optimized case: Get minimum
			int outi=0;
			for(int ay=0;ay<h;ay++)
				{
				for(int ax=0;ax<w;ax++)
					{
					int fromx=Math.max(0,ax-pw);
					int tox=Math.min(w,ax+pw+1);
					int fromy=Math.max(0,ay-ph);
					int toy=Math.min(h,ay+ph+1);
					double curOut=inPixels[fromy*w+fromx];
					for(int sy=fromy;sy<toy;sy++)
						{
						int pi=sy*w+fromx;
						for(int sx=fromx;sx<tox;sx++)
							{
							double nextOut=inPixels[pi];
							if(nextOut<curOut)
								curOut=nextOut;
							pi++;
							}
						}
					outPixels[outi]=curOut;
					outi++;
					}
				}			
			
			}
		else if(percentile==1)
			{
			//Special optimized case: Get maximum
			int outi=0;
			for(int ay=0;ay<h;ay++)
				{
				for(int ax=0;ax<w;ax++)
					{
					int fromx=Math.max(0,ax-pw);
					int tox=Math.min(w,ax+pw+1);
					int fromy=Math.max(0,ay-ph);
					int toy=Math.min(h,ay+ph+1);
					double curOut=inPixels[fromy*w+fromx];
					for(int sy=fromy;sy<toy;sy++)
						{
						int pi=sy*w+fromx;
						for(int sx=fromx;sx<tox;sx++)
							{
							double nextOut=inPixels[pi];
							if(nextOut>curOut)
								curOut=nextOut;
							pi++;
							}
						}
					outPixels[outi]=curOut;
					outi++;
					}
				}
			}
		else
			{
			//General case of getting percentile. Same complexity
			int outi=0;
			for(int ay=0;ay<h;ay++)
				{
				for(int ax=0;ax<w;ax++)
					{
					int fromx=Math.max(0,ax-pw);
					int tox=Math.min(w,ax+pw+1);
					int fromy=Math.max(0,ay-ph);
					int toy=Math.min(h,ay+ph+1);
					int area=(tox-fromx)*(toy-fromy);
					double gotpixels[]=new double[area];
					int curpixi=0;
					for(int sy=fromy;sy<toy;sy++)
						{
						int pi=sy*w+fromx;
						for(int sx=fromx;sx<tox;sx++)
							{
							gotpixels[curpixi]=inPixels[pi];
							pi++;
							curpixi++;
							}
						}
					outPixels[outi]=EvListUtil.findPercentileDouble(gotpixels, percentile);
					outi++;
					}
				}
			}
		
		
		
		return out;
		}
	
	
	}
