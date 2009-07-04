package endrov.flowFourier;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import endrov.flow.EvOpSlice;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Tuple;

/**
 * Fourier transform. FFT if possible, otherwise DFT.
 * FFT is O(n log n), DFT is O(n^2)
 *
 * TODO. verify that this works
 * @author Johan Henriksson
 * 
 */
public class EvOpFourierRealForwardFull2D extends EvOpSlice
	{
	@Override
	public EvPixels[] exec(EvPixels... p)
		{
		Tuple<EvPixels,EvPixels> out=transform(p[0]);
		return new EvPixels[]{out.fst(),out.snd()};
		}

	public int getNumberChannels()
		{
		return 2;
		}
	
	public static Tuple<EvPixels,EvPixels> transform(EvPixels inRe)
		{
		int w=inRe.getWidth();
		int h=inRe.getHeight();
		
		inRe=inRe.getReadOnly(EvPixelsType.DOUBLE);
		
		//Library requires that data is stored swizzled
		double[] swizzle=new double[w*h*2];
		System.arraycopy(inRe.getArrayDouble(), 0, swizzle, 0, w*h);
		
		//Transform
		DoubleFFT_2D transform=new DoubleFFT_2D(h,w);
		transform.realForwardFull(swizzle);
		
		//Get data back on normal form
		return FourierTransform.unswizzle2d(swizzle, w, h);
		}
	}