/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * in (-) kernel.
 * <br/>
 * Kernel has a specified center kcx,kcy. Outside image assumed empty. 
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphErode2D extends EvOpSlice1
	{
	private final MorphKernel kernel;
	
	public EvOpMorphErode2D(MorphKernel kernel)
		{
		this.kernel = kernel;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return kernel.erode(p[0]);
		}

	
	}
