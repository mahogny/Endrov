package endrov.unsortedImageFilters.fourier;

import endrov.imageset.EvPixels;
import endrov.util.Tuple;

/**
 * Fourier transform
 * 
 * TODO add the other transforms
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class FourierTransform
	{
	/**
	 * Numerical libraries prefer swizzled real and complex data
	 */
	public static double[] swizzle(EvPixels inRe, EvPixels inIm, int w, int h)
		{
		inRe=inRe.convertTo(EvPixels.TYPE_DOUBLE, true);
		inIm=inIm.convertTo(EvPixels.TYPE_DOUBLE, true);
		double[] pinReal=inRe.getArrayDouble();
		double[] pinComplex=inIm.getArrayDouble();
		
		//Library requires that data is stored swizzled
		double[] swizzle=new double[w*h*2];
		int numPix=w*h;
		int pos=0;
		for(int i=0;i<numPix;i++)
			{
			swizzle[pos]=pinReal[i];
			swizzle[pos+1]=pinComplex[i];
			pos+=2;
			}
		return swizzle;
		}
	
	
	
	/**
	 * Undo swizzle
	 */
	public static Tuple<EvPixels,EvPixels> unswizzle(double[] swizzle, int w, int h)
		{
		EvPixels outRe=new EvPixels(EvPixels.TYPE_DOUBLE,w,h);
		EvPixels outIm=new EvPixels(EvPixels.TYPE_DOUBLE,w,h);
		double[] outRePixels=outRe.getArrayDouble();
		double[] outImPixels=outIm.getArrayDouble();
		
		int pos=0;
		int numPix=w*h;
		for(int i=0;i<numPix;i++)
			{
			outRePixels[i]=swizzle[pos];
			outImPixels[i]=swizzle[pos+1];
			pos+=2;
			}
		
		return Tuple.make(outRe, outIm);
		}
	
	
	}
