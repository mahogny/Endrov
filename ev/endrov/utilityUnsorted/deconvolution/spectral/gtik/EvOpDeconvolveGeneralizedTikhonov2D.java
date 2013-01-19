/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.deconvolution.spectral.gtik;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import endrov.imageset.EvPixels;
import endrov.utilityUnsorted.deconvolution.Deconvolver2D;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralResizingType;

/**
 * Deconvolution in 2D using generalized tikhonov
 * @author Johan Henriksson
 *
 */
public class EvOpDeconvolveGeneralizedTikhonov2D extends Deconvolver2D
	{
	private final EvPixels imPSF;
	private final DoubleMatrix2D stencil;
	private final SpectralResizingType resizing;
	private final double regParam;
	private final double threshold;
	private final SpectralPaddingType padding;
	
	 public EvOpDeconvolveGeneralizedTikhonov2D(EvPixels imPSF, DoubleMatrix2D stencil, SpectralResizingType resizing,double regParam, double threshold, SpectralPaddingType padding) 
		 {
		 this.imPSF=imPSF;
		 this.stencil=stencil;
		 this.resizing=resizing;
		 this.regParam=regParam;
		 this.threshold=threshold;
		 this.padding=padding;
		 }
   
	protected EvPixels internalDeconvolve(EvPixels ipB)
		{
		if(padding.equals(SpectralPaddingType.PERIODIC))
			{
			DoublePeriodicGeneralizedTikhonov2D d=new DoublePeriodicGeneralizedTikhonov2D(ipB, imPSF, stencil, resizing, regParam, threshold);
			return d.internalDeconvolve();
			}
		else
			{
			DoubleReflexiveGeneralizedTikhonov2D d=new DoubleReflexiveGeneralizedTikhonov2D(ipB, imPSF, stencil, resizing, regParam, threshold);
			return d.internalDeconvolve();
			}
		}
	
	
	}
