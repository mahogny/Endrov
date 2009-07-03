package endrov.flowBinaryMorph;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.math.EvOpImageSubImage;
import endrov.imageset.EvPixels;

/**
 * Thick(image)=image+hitmiss(image). 
 * <br/>
 * 
 * There are other versions that could be implemented
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpBinMorphThick2D extends EvOpSlice1
	{
	private final BinMorphKernel kernelHit, kernelMiss;
	
	
	public EvOpBinMorphThick2D(BinMorphKernel kernelHit, BinMorphKernel kernelMiss)
		{
		this.kernelHit = kernelHit;
		this.kernelMiss = kernelMiss;
		}


	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return thick(p[0], kernelHit, kernelMiss);
		}
	

	public static EvPixels thick(EvPixels in, BinMorphKernel kernelHit, BinMorphKernel kernelMiss)
		{
		return new EvOpImageSubImage().exec1(in, EvOpBinMorphHitmiss2D.hitmiss(in,kernelHit,kernelMiss));
		}
	}