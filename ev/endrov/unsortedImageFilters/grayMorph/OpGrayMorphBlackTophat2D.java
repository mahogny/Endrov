package endrov.unsortedImageFilters.grayMorph;

import endrov.flow.OpSlice1;
import endrov.flow.std.math.OpImageSubImage;
import endrov.imageset.EvPixels;

/**
 * Black Tophat: BTH(image)=close(image) - image
 * <br/>
 * Also called Bottomhat
 * @author Johan Henriksson
 */
public class OpGrayMorphBlackTophat2D extends OpSlice1
{
private int kcx,kcy;
private EvPixels kernel;
public OpGrayMorphBlackTophat2D(int kcx, int kcy, EvPixels kernel)
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
	//This can be made about 50% faster by specializing the code
	return new OpImageSubImage().exec1(OpGrayMorphClose2D.close(in,kernel,kcx,kcy), in);
	}
}