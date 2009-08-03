package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * External gradient: dilate(image)-image
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphGradientExternal2D extends EvOpSlice1
	{
	private final MorphKernel kernel;
	
	public EvOpMorphGradientExternal2D(MorphKernel kernel)
		{
		this.kernel = kernel;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return kernel.externalGradient(p[0]);
		}

	
	}