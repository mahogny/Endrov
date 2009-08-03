package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * White Tophat: WTH(image)=image - open(image)
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphWhiteTophat2D extends EvOpSlice1
	{
	private final MorphKernel kernel;
	
	public EvOpMorphWhiteTophat2D(MorphKernel kernel)
		{
		this.kernel = kernel;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return kernel.whitetophat(p[0]);
		}

	}