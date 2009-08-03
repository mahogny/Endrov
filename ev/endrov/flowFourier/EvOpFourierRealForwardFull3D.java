package endrov.flowFourier;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;
import endrov.flow.EvOpStack;
import endrov.imageset.EvStack;
import endrov.util.Tuple;

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
	public EvStack[] exec(EvStack... p)
		{
		Tuple<EvStack,EvStack> out=transform(p[0]);
		return new EvStack[]{out.fst(),out.snd()};
		}

	public int getNumberChannels()
		{
		return 2;
		}
	
	public static Tuple<EvStack,EvStack> transform(EvStack inRe)
		{
		int w=inRe.getWidth();
		int h=inRe.getHeight();
		int d=inRe.getDepth();
		
		//Copy out resolution so inRe can be GC:ed early
		EvStack stackMeta=new EvStack();
		stackMeta.getMetaFrom(inRe);

		//Change memory layout
		double[][] arr=inRe.getReadOnlyArraysDouble();
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