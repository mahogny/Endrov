package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * Black Tophat (aka bottomhat): BTH(image)=close(image) - image
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphBlackTophat2D extends EvOpSlice1
	{
	private final MorphKernel kernel;
	
	public EvOpMorphBlackTophat2D(MorphKernel kernel)
		{
		this.kernel = kernel;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return kernel.blacktophat(p[0]);
		}

	

	}