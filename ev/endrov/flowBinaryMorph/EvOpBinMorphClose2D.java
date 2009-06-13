package endrov.flowBinaryMorph;

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
	private final BinMorphKernel kernel;
	
	
	public EvOpBinMorphClose2D(BinMorphKernel kernel)
		{
		this.kernel = kernel;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return close(p[0],kernel);
		}

	public static EvPixels close(EvPixels in, BinMorphKernel kernel)
		{
		return EvOpBinMorphDilate2D.dilate(EvOpBinMorphErode2D.erode(in,kernel),kernel);
		}
	}