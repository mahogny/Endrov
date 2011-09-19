/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.math.EvOpImageSubImage;
import endrov.imageset.EvPixels;
import endrov.util.ProgressHandle;

/**
 * Thin(image)=image-hitmiss(image). 
 * <p>
 * There are other versions that could be implemented
 * <p>
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
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return thin(ph, p[0], kernelHit, kernelMiss);
		}
	
	public static EvPixels thin(ProgressHandle ph, EvPixels in, MorphKernel kernelHit, MorphKernel kernelMiss)
		{
		return new EvOpImageSubImage().exec1(ph, in, EvOpBinMorphHitmiss2D.hitmiss(in,kernelHit,kernelMiss));
		
		//could be made a lot faster for repeated application by keeping a front-set. Either way, probably want to return if there is more to do?
		
		}
	}