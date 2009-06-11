package endrov.unsortedImageFilters.binaryMorph;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * Open: dilate, then erode
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpBinMorphOpen2D extends EvOpSlice1
	{
	private int kcx,kcy;
	private EvPixels kernel;
	public EvOpBinMorphOpen2D(int kcx, int kcy, EvPixels kernel)
		{
		this.kcx = kcx;
		this.kcy = kcy;
		this.kernel = kernel;
		}
	
	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return open(p[0],kernel, kcx, kcy);
		}
	
	public static EvPixels open(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		return EvOpBinMorphErode2D.erode(EvOpBinMorphDilate2D.dilate(in,kernel,kcx,kcy),kernel,kcx,kcy);
		}
	}