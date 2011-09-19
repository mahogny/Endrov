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
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return kernel.blacktophat(ph, p[0]);
		}

	

	}