package endrov.flowBinaryMorph;

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
	private final BinMorphKernel kernel;
	
	public EvOpBinMorphOpen2D(BinMorphKernel kernel)
		{
		this.kernel = kernel;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return open(p[0],kernel);
		}
	
	public static EvPixels open(EvPixels in, BinMorphKernel kernel)
		{
		return EvOpBinMorphErode2D.erode(EvOpBinMorphDilate2D.dilate(in,kernel),kernel);
		}
	}