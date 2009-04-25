package endrov.unsortedImageFilters;

import endrov.imageset.EvPixels;
import endrov.util.Tuple;

public class FourierTransform
	{
	/**
	 * Discrete fourier transform
	 * 
	 * TODO It might be faster to invoke FFTW if available
	 * TODO surely there must be better java implementations
	 * 
	 * Complexity O(w^2*h^2)
	 */
	public static Tuple<EvPixels,EvPixels> movingAverage(EvPixels in)
		{
		in=in.convertTo(EvPixels.TYPE_DOUBLE, true);
		double[] inPixels=in.getArrayDouble();
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels outRe=new EvPixels(in.getType(),w,h);
		EvPixels outIm=new EvPixels(in.getType(),w,h);
		double[] outRePixels=outRe.getArrayDouble();
		double[] outImPixels=outIm.getArrayDouble();
		
		for(int v=0;v<h;v++)
			{
			for(int u=0;u<w;u++)
				{
				double sumRe=0;
				double sumIm=0;
				for(int y=0;y<h;y++)
					{
					for(int x=0;x<w;x++)
						{
						//add exp(i(ux+vy))
						double p=u*x+v*y;
						p*=inPixels[in.getPixelIndex(x, y)];
						sumRe+=Math.cos(p);
						sumIm+=Math.sin(p);
						}
					}

				int index=outRe.getPixelIndex(u, v);
				outRePixels[index]=sumRe;
				outImPixels[index]=sumRe;
				
				}
			}
		return Tuple.make(outRe,outIm);
		}
	}
