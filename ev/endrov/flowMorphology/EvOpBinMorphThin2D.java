package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.math.EvOpImageSubImage;
import endrov.imageset.EvPixels;

/**
 * Thin(image)=image-hitmiss(image). 
 * <br/>
 * 
 * Can be used to skeletonize images by application until convergence. 
 * 
 * There are other versions that could be implemented
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpBinMorphThin2D extends EvOpSlice1
	{
	private final MorphKernel kernelHit, kernelMiss;

	public EvOpBinMorphThin2D(MorphKernel kernelHit, MorphKernel kernelMiss)
		{
		this.kernelHit = kernelHit;
		this.kernelMiss = kernelMiss;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return thin(p[0], kernelHit, kernelMiss);
		}
	
	public static EvPixels thin(EvPixels in, MorphKernel kernelHit, MorphKernel kernelMiss)
		{
		return new EvOpImageSubImage().exec1(in, EvOpBinMorphHitmiss2D.hitmiss(in,kernelHit,kernelMiss));
		
		//could be made a lot faster for repeated application by keeping a front-set. Either way, probably want to return if there is more to do?
		
		}
	}