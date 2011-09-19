/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFourier;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import endrov.flow.EvOpSlice;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.ProgressHandle;
import endrov.util.Tuple;

/**
 * Inverse fourier transform. FFT if possible, otherwise DFT.
 * FFT is O(n log n), DFT is O(n^2)
 *
 * TODO. verify that this works
 * @author Johan Henriksson
 * 
 */
public class EvOpFourierRealInverseFull2D extends EvOpSlice
	{
	private final boolean scale;
	
	public EvOpFourierRealInverseFull2D(boolean scale)
		{
		this.scale = scale;
		}

	@Override
	public EvPixels[] exec(ProgressHandle ph, EvPixels... p)
		{
		Tuple<EvPixels,EvPixels> out=transform(p[0], scale);
		return new EvPixels[]{out.fst(),out.snd()};
		}

	public int getNumberChannels()
		{
		return 2;
		}
	
	public static Tuple<EvPixels,EvPixels> transform(EvPixels inRe, boolean scale)
		{
		int w=inRe.getWidth();
		int h=inRe.getHeight();
		
		inRe=inRe.getReadOnly(EvPixelsType.DOUBLE);
		
		//Library requires that data is stored swizzled
		double[] swizzle=new double[w*h];
		System.arraycopy(inRe.getArrayDouble(), 0, swizzle, 0, w*h);
		
		//Transform
		DoubleFFT_2D transform=new DoubleFFT_2D(h,w);
		transform.realInverseFull(swizzle,scale);
		
		//Get data back on normal form
		return FourierTransform.unswizzle2d(swizzle, w, h);
		}
	}