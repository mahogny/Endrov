package endrov.flowFourier;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;
import endrov.flow.EvOpStack;
import endrov.imageset.EvStack;
import endrov.util.Tuple;

/**
 * Inverse fourier transform. FFT if possible, otherwise DFT.
 * FFT is O(n log n), DFT is O(n^2)
 * 
 * @author Johan Henriksson
 */
public class EvOpFourierComplexInverse3D extends EvOpStack
	{
	private final boolean scale;
	
	public EvOpFourierComplexInverse3D(boolean scale)
		{
		this.scale = scale;
		}

	@Override
	public EvStack[] exec(EvStack... p)
		{
		Tuple<EvStack,EvStack> out=transform(p[0], p[1], scale);
		return new EvStack[]{out.fst(),out.snd()};
		}

	public int getNumberChannels()
		{
		return 2;
		}
	
	public static Tuple<EvStack,EvStack> transform(EvStack inRe, EvStack inIm, boolean scale)
		{
		int w=inRe.getWidth();
		int h=inRe.getHeight();
		int d=inRe.getDepth();
		
		//Copy out resolution so inRe can be GC:ed early
		EvStack stackMeta=new EvStack();
		stackMeta.getMetaFrom(inRe);
		
		//Transform
		double[] swizzle=FourierTransform.swizzle3d(inRe, inIm, w, h, d);
		DoubleFFT_3D transform=new DoubleFFT_3D(d,h,w);
		transform.complexInverse(swizzle, scale);
		
		//Get data back on normal form
		return FourierTransform.unswizzle3d(swizzle, w, h, d, stackMeta);
		}
	}