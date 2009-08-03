package endrov.flowFourier;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.Tuple;

/**
 * Fourier transform helper functions
 * 
 * @author Johan Henriksson
 *
 */
class FourierTransform
	{
	/**
	 * Numerical libraries prefer swizzled real and complex data
	 */
	public static double[] swizzle2d(EvPixels inRe, EvPixels inIm, int w, int h)
		{
		inRe=inRe.getReadOnly(EvPixelsType.DOUBLE);
		inIm=inIm.getReadOnly(EvPixelsType.DOUBLE);
		double[] pinReal=inRe.getArrayDouble();
		double[] pinComplex=inIm.getArrayDouble();
		
		//Library requires that data is stored swizzled
		double[] swizzle=new double[w*h*2];
		int pos=0;
		int numPix=w*h;
		for(int i=0;i<numPix;i++)
			{
			swizzle[pos]=pinReal[i];
			swizzle[pos+1]=pinComplex[i];
			pos+=2;
			}
		return swizzle;
		}
	
	
	/**
	 * Numerical libraries prefer swizzled real and complex data
	 */
	public static double[] swizzle3d(EvStack inRe, EvStack inIm, int w, int h, int d)
		{
		double[][] arrRe=inRe.getReadOnlyArraysDouble();
		double[][] arrIm=inIm.getReadOnlyArraysDouble();

		double[] swizzle=new double[w*h*d*2];

		int pos=0;
		for(int az=0;az<d;az++)
			{
			double[] pinReal=arrRe[az];
			double[] pinComplex=arrIm[az];
			
			int numPix=w*h;
			for(int i=0;i<numPix;i++)
				{
				swizzle[pos]=pinReal[i];
				swizzle[pos+1]=pinComplex[i];
				pos+=2;
				}
			}
		
		return swizzle;
		}
	
	/**
	 * Undo swizzle 2d
	 */
	public static Tuple<EvPixels,EvPixels> unswizzle2d(double[] swizzle, int w, int h)
		{
		EvPixels outRe=new EvPixels(EvPixelsType.DOUBLE,w,h);
		EvPixels outIm=new EvPixels(EvPixelsType.DOUBLE,w,h);
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
	

	/**
	 * Undo swizzle 3d
	 */
	public static Tuple<EvStack,EvStack> unswizzle3d(double[] swizzle, int w, int h, int d, EvStack template)
		{
		EvStack outRe=new EvStack();
		EvStack outIm=new EvStack();

		outRe.allocate(w, h, d, EvPixelsType.DOUBLE, template);
		outIm.allocate(w, h, d, EvPixelsType.DOUBLE, template);
		
		EvPixels[] pRe=outRe.getPixels();
		EvPixels[] pIm=outIm.getPixels();
		int pos=0;
		for(int az=0;az<d;az++)
			{
			double[] outRePixels=pRe[az].getArrayDouble();
			double[] outImPixels=pIm[az].getArrayDouble();
			
			int numPix=w*h;
			for(int i=0;i<numPix;i++)
				{
				outRePixels[i]=swizzle[pos];
				outImPixels[i]=swizzle[pos+1];
				pos+=2;
				}
			}
		return Tuple.make(outRe, outIm);
		}
	
	}
