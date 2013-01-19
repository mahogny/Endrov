/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFourier;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.math.EvOpImageComplexMulImage;
import endrov.typeImageset.EvPixels;
import endrov.util.ProgressHandle;

/**
 * Circular convolution
 * Complexity: same as fourier transform
 * 
 * @author Johan Henriksson
 */
public class EvOpCircConv2D extends EvOpSlice1
	{
	private final EvPixels kernel;
	
	public EvOpCircConv2D(EvPixels kernel)
		{
		this.kernel = kernel;
		}


	@Override
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return apply(ph,kernel,p[0]);
		}

	
	public static EvPixels apply(ProgressHandle ph, EvPixels ima, EvPixels imb)
		{
		ima=new EvOpWrapImage2D(null,null).exec1(ph, ima);
		
		EvPixels[] ckernel=new EvOpFourierRealForwardFull2D().exec(ph,ima);
		EvPixels[] cin=new EvOpFourierRealForwardFull2D().exec(ph,imb);
		EvPixels[] mul=new EvOpImageComplexMulImage().exec(ph, ckernel[0],ckernel[1],cin[0],cin[1]);
		
		return EvOpFourierComplexInverse2D.transform(mul[0], mul[1],true).fst();
		}
	}