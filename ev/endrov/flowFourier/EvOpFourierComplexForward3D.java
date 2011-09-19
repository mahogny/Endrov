/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFourier;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;
import endrov.flow.EvOpStack;
import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.Tuple;

/**
 * Fourier transform. FFT if possible, otherwise DFT.
 * FFT is O(n log n), DFT is O(n^2)
 * 
 * @author Johan Henriksson
 */
public class EvOpFourierComplexForward3D extends EvOpStack
	{
	@Override
	public EvStack[] exec(ProgressHandle ph, EvStack... p)
		{
		Tuple<EvStack,EvStack> out=transform(ph, p[0], p[1]);
		return new EvStack[]{out.fst(),out.snd()};
		}

	public int getNumberChannels()
		{
		return 2;
		}
	
	public static Tuple<EvStack,EvStack> transform(ProgressHandle progh, EvStack inRe, EvStack inIm)
		{
		int w=inRe.getWidth();
		int h=inRe.getHeight();
		int d=inRe.getDepth();
		
		//Library requires that data is stored swizzled
		double[] swizzle=FourierTransform.swizzle3d(progh, inRe, inIm, w, h, d);
		
		//Transform
		DoubleFFT_3D transform=new DoubleFFT_3D(h,w,d);
		transform.complexForward(swizzle);
		
		//Get data back on normal form
		return FourierTransform.unswizzle3d(swizzle, w, h, d, inRe);
		}
	}