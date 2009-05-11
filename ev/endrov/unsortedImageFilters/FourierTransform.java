package endrov.unsortedImageFilters;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
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
	 * Discrete fourier transform
	 * 
	 * Complexity O(w^2*h^2)
	 */
	/*
	public static Tuple<EvPixels,EvPixels> fftOld(EvPixels in)
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
	*/
	
	
	/**
	 * Fourier transform. FFT if possible, otherwise DFT.
	 * FFT is O(n log n), DFT is O(n^2)
	 * 
	 */
	public static Tuple<EvPixels,EvPixels> forwardComplex(EvPixels inRe, EvPixels inIm)
		{
		int w=inRe.getWidth();
		int h=inRe.getHeight();
		
		inRe=inRe.convertTo(EvPixels.TYPE_DOUBLE, true);
		inIm=inIm.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		//Library requires that data is stored swizzled
		double[] swizzle=swizzle(inRe, inIm, w, h);
		
		//Transform
		DoubleFFT_2D transform=new DoubleFFT_2D(h,w);
		transform.complexForward(swizzle);
		
		//Get data back on normal form
		return unswizzle(swizzle, w, h);
		}
	
	/**
	 * Fourier transform. FFT if possible, otherwise DFT.
	 * FFT is O(n log n), DFT is O(n^2)
	 *
	 * TODO. verify that this works
	 * 
	 */
	public static Tuple<EvPixels,EvPixels> realForwardFull(EvPixels inRe)
		{
		int w=inRe.getWidth();
		int h=inRe.getHeight();
		
		inRe=inRe.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		//Library requires that data is stored swizzled
		double[] swizzle=new double[w*h*2];
		System.arraycopy(inRe.getArrayDouble(), 0, swizzle, 0, w*h);
		
		//Transform
		DoubleFFT_2D transform=new DoubleFFT_2D(h,w);
		transform.realForwardFull(swizzle);
		
		//Get data back on normal form
		return unswizzle(swizzle, w, h);
		}
	
	/**
	 * Inverse fourier transform. FFT if possible, otherwise DFT.
	 * FFT is O(n log n), DFT is O(n^2)
	 *
	 * TODO. verify that this works
	 * 
	 */
	public static Tuple<EvPixels,EvPixels> realInverseFull(EvPixels inRe, boolean scale)
		{
		int w=inRe.getWidth();
		int h=inRe.getHeight();
		
		inRe=inRe.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		//Library requires that data is stored swizzled
		double[] swizzle=new double[w*h];
		System.arraycopy(inRe.getArrayDouble(), 0, swizzle, 0, w*h);
		
		//Transform
		DoubleFFT_2D transform=new DoubleFFT_2D(h,w);
		transform.realInverseFull(swizzle,scale);
		
		//Get data back on normal form
		return unswizzle(swizzle, w, h);
		}
	
	/**
	 * Inverse fourier transform. FFT if possible, otherwise DFT.
	 * FFT is O(n log n), DFT is O(n^2)
	 * 
	 */
	public static Tuple<EvPixels,EvPixels> complexInverse(EvPixels inRe, EvPixels inIm, boolean scale)
		{
		int w=inRe.getWidth();
		int h=inRe.getHeight();
		
		inRe=inRe.convertTo(EvPixels.TYPE_DOUBLE, true);
		inIm=inIm.convertTo(EvPixels.TYPE_DOUBLE, true);
		
		//Library requires that data is stored swizzled
		double[] swizzle=swizzle(inRe, inIm, w, h);
		
		//Transform
		DoubleFFT_2D transform=new DoubleFFT_2D(h,w);
		transform.complexInverse(swizzle, scale);
		
		//Get data back on normal form
		return unswizzle(swizzle, w, h);
		}
	
	
	
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
