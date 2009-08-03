package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * Open: dilate, then erode
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphOpen2D extends EvOpSlice1
	{
	private final MorphKernel kernel;
	
	public EvOpMorphOpen2D(MorphKernel kernel)
		{
		this.kernel = kernel;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return kernel.open(p[0]);
		}
	
	}