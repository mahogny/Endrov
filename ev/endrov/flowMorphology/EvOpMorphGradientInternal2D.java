/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.util.ProgressHandle;

/**
 * Internal gradient: image-erode(image)
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphGradientInternal2D extends EvOpSlice1
	{
	private final MorphKernel kernel;
	
	public EvOpMorphGradientInternal2D(MorphKernel kernel)
		{
		this.kernel = kernel;
		}

	@Override
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return kernel.internalGradient(ph, p[0]);
		}

	
	}
