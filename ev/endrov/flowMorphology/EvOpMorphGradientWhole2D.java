package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * Whole gradient: dilate(image)-erode(image)
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphGradientWhole2D extends EvOpSlice1
	{
	private final MorphKernel kernel;
	
	public EvOpMorphGradientWhole2D(MorphKernel kernel)
		{
		this.kernel = kernel;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return kernel.wholeGradient(p[0]);
		}

	
	}