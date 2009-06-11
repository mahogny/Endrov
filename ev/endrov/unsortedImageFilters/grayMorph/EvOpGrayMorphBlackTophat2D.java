package endrov.unsortedImageFilters.grayMorph;

import endrov.flow.EvOpSlice1;
import endrov.flow.std.math.EvOpImageSubImage;
import endrov.imageset.EvPixels;

/**
 * Black Tophat: BTH(image)=close(image) - image
 * <br/>
 * Also called Bottomhat
 * @author Johan Henriksson
 */
public class EvOpGrayMorphBlackTophat2D extends EvOpSlice1
{
private int kcx,kcy;
private EvPixels kernel;
public EvOpGrayMorphBlackTophat2D(int kcx, int kcy, EvPixels kernel)
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
	return new EvOpImageSubImage().exec1(EvOpGrayMorphClose2D.close(in,kernel,kcx,kcy), in);
	}
}