package endrov.deconvolution.spectral.tik;

import endrov.deconvolution.Deconvolver2D;
import endrov.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.deconvolution.spectral.SpectralEnums.SpectralResizingType;
import endrov.imageset.EvPixels;

/**
 * Deconvolution in 2D using tikhonov
 * @author Johan Henriksson
 *
 */
public class DoubleTikhonovDeconvolver2D extends Deconvolver2D
	{
	private final EvPixels imPSF;
	private final SpectralResizingType resizing;
	private final double regParam;
	private final double threshold;
	private final SpectralPaddingType padding;
	
	 public DoubleTikhonovDeconvolver2D(EvPixels imPSF, SpectralResizingType resizing,double regParam, double threshold, SpectralPaddingType padding) 
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
			DoublePeriodicTikhonov2D d=new DoublePeriodicTikhonov2D(ipB, imPSF, resizing, regParam, threshold);
			return d.internalDeconvolve();
			}
		else
			{
			DoubleReflexiveTikhonov2D d=new DoubleReflexiveTikhonov2D(ipB, imPSF, resizing, regParam, threshold);
			return d.internalDeconvolve();
			}
		}
	
	
	}
