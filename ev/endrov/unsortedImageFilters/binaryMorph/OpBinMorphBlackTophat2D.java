package endrov.unsortedImageFilters.binaryMorph;

import endrov.flow.OpSlice1;
import endrov.flow.std.math.OpImageSubImage;
import endrov.imageset.EvPixels;

/**
 * Black Tophat: BTH(image)=close(image) - image
 * <br/>
 * Also called Bottomhat
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class OpBinMorphBlackTophat2D extends OpSlice1
	{
	private int kcx,kcy;
	private EvPixels kernel;
	public OpBinMorphBlackTophat2D(int kcx, int kcy, EvPixels kernel)
		{
		this.kcx = kcx;
		this.kcy = kcy;
		this.kernel = kernel;
		}
	
	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return blacktophat(p[0],kernel, kcx, kcy);
		}

	

	public static EvPixels blacktophat(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		//TODO maybe only useful with graylevel morphology
		
		//This can be made about 50% faster by specializing the code
		return new OpImageSubImage().exec1(OpBinMorphClose2D.close(in,kernel,kcx,kcy), in);
		}
	}