package endrov.flowBinaryMorph;

import endrov.flow.EvOpSlice1;
import endrov.flow.std.math.EvOpImageSubImage;
import endrov.imageset.EvPixels;

/**
 * White Tophat: WTH(image)=image - open(image)
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpBinMorphWhiteTophat2D extends EvOpSlice1
	{
	private final BinMorphKernel kernel;
	
	public EvOpBinMorphWhiteTophat2D(BinMorphKernel kernel)
		{
		this.kernel = kernel;
		}



	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return whitetophat(p[0],kernel);
		}

	

	public static EvPixels whitetophat(EvPixels in, BinMorphKernel kernel)
		{
		//TODO maybe only useful with graylevel morphology
		
		//This can be made about 50% faster by specializing the code
		return new EvOpImageSubImage().exec1(in, EvOpBinMorphOpen2D.open(in,kernel));
		}
	}