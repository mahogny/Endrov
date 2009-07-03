package endrov.flowBinaryMorph;

import endrov.flow.EvOpSlice1;
import endrov.flowBasic.math.EvOpImageSubImage;
import endrov.imageset.EvPixels;

/**
 * Black Tophat: BTH(image)=close(image) - image
 * <br/>
 * Also called Bottomhat
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpBinMorphBlackTophat2D extends EvOpSlice1
	{
	private final BinMorphKernel kernel;
	
	
	public EvOpBinMorphBlackTophat2D(BinMorphKernel kernel)
		{
		this.kernel = kernel;
		}



	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return blacktophat(p[0],kernel);
		}

	

	public static EvPixels blacktophat(EvPixels in, BinMorphKernel kernel)
		{
		//TODO maybe only useful with graylevel morphology
		
		//This can be made about 50% faster by specializing the code
		return new EvOpImageSubImage().exec1(EvOpBinMorphClose2D.close(in,kernel), in);
		}
	}