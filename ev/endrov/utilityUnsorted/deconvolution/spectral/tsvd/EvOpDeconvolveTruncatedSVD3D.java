/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.deconvolution.spectral.tsvd;

import endrov.typeImageset.EvStack;
import endrov.utilityUnsorted.deconvolution.DeconvPixelsStack;
import endrov.utilityUnsorted.deconvolution.Deconvolver3D;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralResizingType;

/**
 * Deconvolution in 3D using truncated SVD
 * @author Johan Henriksson
 *
 */
public class EvOpDeconvolveTruncatedSVD3D extends Deconvolver3D
	{
	private final EvStack imPSF;
	private final SpectralResizingType resizing;
	private final double regParam;
	private final double threshold;
	private final SpectralPaddingType padding;
	
	 public EvOpDeconvolveTruncatedSVD3D(EvStack imPSF, SpectralResizingType resizing,double regParam, double threshold, SpectralPaddingType padding) 
		 {
		 this.imPSF=imPSF;
		 this.resizing=resizing;
		 this.regParam=regParam;
		 this.threshold=threshold;
		 this.padding=padding;
		 }
   
	protected DeconvPixelsStack internalDeconvolve(EvStack ipB)
		{
		if(padding.equals(SpectralPaddingType.PERIODIC))
			{
			DoublePeriodicTruncatedSVD3D d=new DoublePeriodicTruncatedSVD3D(imPSF, resizing, regParam, threshold);
			return d.internalDeconvolve(ipB);
			}
		else
			{
			DoubleReflexiveTruncatedSVD3D d=new DoubleReflexiveTruncatedSVD3D(imPSF, resizing, regParam, threshold);
			return d.internalDeconvolve(ipB);
			}
		}
	
	
	}
