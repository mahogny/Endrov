/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFourier;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;
import endrov.flow.EvOpStack1;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Circular convolution
 * 
 * Complexity, same as FourierTransform
 */
public class EvOpCircConv3D extends EvOpStack1
	{
	private final EvStack kernel;
	
	public EvOpCircConv3D(EvStack kernel)
		{
		this.kernel = kernel;
		}


	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return apply(ph, kernel,p[0]);
		}
	
	
	private static double[] forward(ProgressHandle progh, DoubleFFT_3D transform, EvStack ima)
		{
		int w=ima.getWidth();
		int h=ima.getHeight();
		int d=ima.getDepth();

		//Change memory layout
		double[][] arr=ima.getArraysDoubleReadOnly(progh);
		double[] swizzle=new double[w*h*d*2];
		for(int az=0;az<d;az++)
			System.arraycopy(arr[az],0,swizzle, w*h*az,w*h);
		
		//Transform
		//DoubleFFT_3D transform=new DoubleFFT_3D(d, h,w);
		transform.realForwardFull(swizzle);

		return swizzle;
		}
	
	public static EvStack apply(ProgressHandle ph, EvStack kernel, EvStack imb)
		{
		int w=kernel.getWidth();
		int h=kernel.getHeight();
		int d=kernel.getDepth();
		DoubleFFT_3D transform=new DoubleFFT_3D(d,h,w);

		//Copy out resolution so arrays can be GC:ed early
		EvStack stackMeta=new EvStack();
		stackMeta.copyMetaFrom(imb);
		
		//Into fourier space
		kernel=new EvOpWrapImage3D(null,null,null).exec1(ph, kernel);
		double[] arrayA=forward(ph, transform,kernel);
		double[] arrayB=forward(ph, transform,imb);
		
		//Complex multiplication
		for(int i=0;i<arrayA.length;i+=2)
			{
			//(a+bi)*(c+di)=ac-bd  + i( bc+ad  )
			double ar=arrayA[i];
			double ai=arrayA[i+1];
			double br=arrayB[i];
			double bi=arrayB[i+1];
			double real=ar*br - ai*bi;
			double imag=ai*br + ar*bi;
			arrayA[i]=real;
			arrayA[i+1]=imag;
			}
		System.gc();
		
		//Into normal space
		transform.complexInverse(arrayA, true);
		
		//Get data back on normal form
		return FourierTransform.unswizzle3d(arrayA, w, h, d, stackMeta).fst();
		}
	}