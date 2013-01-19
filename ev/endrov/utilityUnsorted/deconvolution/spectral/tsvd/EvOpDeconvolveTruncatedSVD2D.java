/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.deconvolution.spectral.tsvd;

import endrov.imageset.EvPixels;
import endrov.utilityUnsorted.deconvolution.Deconvolver2D;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralResizingType;

/**
 * Deconvolution in 2D using truncated SVD
 * @author Johan Henriksson
 *
 */
public class EvOpDeconvolveTruncatedSVD2D extends Deconvolver2D
	{
	private final EvPixels imPSF;
	private final SpectralResizingType resizing;
	private final double regParam;
	private final double threshold;
	private final SpectralPaddingType padding;
	
	 public EvOpDeconvolveTruncatedSVD2D(EvPixels imPSF, SpectralResizingType resizing,double regParam, double threshold, SpectralPaddingType padding) 
		 {
		 this.imPSF=imPSF;
		 this.resizing=resizing;
		 this.regParam=regParam;
		 this.threshold=threshold;
		 this.padding=padding;
		 }
   
	protected EvPixels internalDeconvolve(EvPixels ipB)
		{
		if(padding.equals(SpectralPaddingType.PERIODIC))
			{
			DoublePeriodicTruncatedSVD2D d=new DoublePeriodicTruncatedSVD2D(ipB, imPSF, resizing, regParam, threshold);
			return d.internalDeconvolve();
			}
		else
			{
			DoubleReflexiveTruncatedSVD2D d=new DoubleReflexiveTruncatedSVD2D(ipB, imPSF, resizing, regParam, threshold);
			return d.internalDeconvolve();
			}
		}
	
	
	}
