/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFourier;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;
import endrov.flow.EvOpStack;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.collection.Tuple;

/**
 * Fourier transform. FFT if possible, otherwise DFT.
 * FFT is O(n log n), DFT is O(n^2)
 *
 * TODO. verify that this works
 * @author Johan Henriksson
 * 
 */
public class EvOpFourierRealForwardFull3D extends EvOpStack
	{
	@Override
	public EvStack[] exec(ProgressHandle ph, EvStack... p)
		{
		Tuple<EvStack,EvStack> out=transform(ph, p[0]);
		return new EvStack[]{out.fst(),out.snd()};
		}

	public int getNumberChannels()
		{
		return 2;
		}
	
	public static Tuple<EvStack,EvStack> transform(ProgressHandle progh, EvStack inRe)
		{
		int w=inRe.getWidth();
		int h=inRe.getHeight();
		int d=inRe.getDepth();
		
		//Copy out resolution so inRe can be GC:ed early
		EvStack stackMeta=new EvStack();
		stackMeta.copyMetaFrom(inRe);

		//Change memory layout
		double[][] arr=inRe.getArraysDoubleReadOnly(progh);
		double[] swizzle=new double[w*h*d*2];
		for(int az=0;az<d;az++)
			System.arraycopy(arr[az],0,swizzle, w*h*az,w*h);
		
		//Transform
		DoubleFFT_3D transform=new DoubleFFT_3D(d, h,w);
		transform.realForwardFull(swizzle);
		
		//Get data back on normal form
		return FourierTransform.unswizzle3d(swizzle, w, h, d, stackMeta);
		}
	}