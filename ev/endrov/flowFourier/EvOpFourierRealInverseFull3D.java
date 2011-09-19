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
 * Inverse fourier transform. FFT if possible, otherwise DFT.
 * FFT is O(n log n), DFT is O(n^2)
 * 
 * @author Johan Henriksson
 */
public class EvOpFourierRealInverseFull3D extends EvOpStack
	{
	private final boolean scale;
	
	public EvOpFourierRealInverseFull3D(boolean scale)
		{
		this.scale = scale;
		}

	@Override
	public EvStack[] exec(ProgressHandle ph, EvStack... p)
		{
		Tuple<EvStack,EvStack> out=transform(ph, p[0], scale);
		return new EvStack[]{out.fst(),out.snd()};
		}

	public int getNumberChannels()
		{
		return 2;
		}
	
	public static Tuple<EvStack,EvStack> transform(ProgressHandle progh, EvStack inRe, boolean scale)
		{
		int w=inRe.getWidth();
		int h=inRe.getHeight();
		int d=inRe.getDepth();
		
		double[][] arr=inRe.getReadOnlyArraysDouble(progh);
		double[] swizzle=new double[w*h*d*2];
		for(int az=0;az<d;az++)
			System.arraycopy(arr[az],0,swizzle, w*h*az,w*h);
		
		//Transform
		DoubleFFT_3D transform=new DoubleFFT_3D(h,w,d);
		transform.realInverseFull(swizzle, scale);
		
		//Get data back on normal form
		return FourierTransform.unswizzle3d(swizzle, w, h, d, inRe);
		}
	}