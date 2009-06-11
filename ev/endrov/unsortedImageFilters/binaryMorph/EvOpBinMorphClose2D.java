package endrov.unsortedImageFilters.binaryMorph;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * Close: Erode, then dilate
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpBinMorphClose2D extends EvOpSlice1
	{
	private int kcx,kcy;
	private EvPixels kernel;
	public EvOpBinMorphClose2D(int kcx, int kcy, EvPixels kernel)
		{
		this.kcx = kcx;
		this.kcy = kcy;
		this.kernel = kernel;
		}
	
	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return close(p[0],kernel, kcx, kcy);
		}

	public static EvPixels close(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		return EvOpBinMorphDilate2D.dilate(EvOpBinMorphErode2D.erode(in,kernel,kcx,kcy),kernel,kcx,kcy);
		}
	}