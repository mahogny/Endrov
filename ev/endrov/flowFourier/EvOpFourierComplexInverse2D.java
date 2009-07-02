package endrov.flowFourier;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import endrov.flow.EvOpSlice;
import endrov.imageset.EvPixels;
import endrov.util.Tuple;

/**
 * Inverse fourier transform. FFT if possible, otherwise DFT.
 * FFT is O(n log n), DFT is O(n^2)
 * 
 * @author Johan Henriksson
 */
public class EvOpFourierComplexInverse2D extends EvOpSlice
	{
	private final boolean scale;
	
	public EvOpFourierComplexInverse2D(boolean scale)
		{
		this.scale = scale;
		}

	@Override
	public EvPixels[] exec(EvPixels... p)
		{
		Tuple<EvPixels,EvPixels> out=transform(p[0], p[1], scale);
		return new EvPixels[]{out.fst(),out.snd()};
		}

	public int getNumberChannels()
		{
		return 2;
		}
	
		public static Tuple<EvPixels,EvPixels> transform(EvPixels inRe, EvPixels inIm, boolean scale)
			{
			int w=inRe.getWidth();
			int h=inRe.getHeight();
			
			inRe=inRe.convertTo(EvPixels.TYPE_DOUBLE, true);
			inIm=inIm.convertTo(EvPixels.TYPE_DOUBLE, true);
			
			//Library requires that data is stored swizzled
			double[] swizzle=FourierTransform.swizzle2d(inRe, inIm, w, h);
			
			//Transform
			DoubleFFT_2D transform=new DoubleFFT_2D(h,w);
			transform.complexInverse(swizzle, scale);
			
			//Get data back on normal form
			return FourierTransform.unswizzle2d(swizzle, w, h);
			}
	}