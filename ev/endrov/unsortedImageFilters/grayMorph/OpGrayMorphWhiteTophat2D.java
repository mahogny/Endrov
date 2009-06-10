package endrov.unsortedImageFilters.grayMorph;

import endrov.flow.OpSlice1;
import endrov.flow.std.math.OpImageSubImage;
import endrov.imageset.EvPixels;

/**
 * White Tophat: WTH(image)=image - open(image)
 * @author Johan Henriksson
 */
public class OpGrayMorphWhiteTophat2D extends OpSlice1
{
private int kcx,kcy;
private EvPixels kernel;
public OpGrayMorphWhiteTophat2D(int kcx, int kcy, EvPixels kernel)
	{
	this.kcx = kcx;
	this.kcy = kcy;
	this.kernel = kernel;
	}

@Override
public EvPixels exec1(EvPixels... p)
	{
	return whitetophat(p[0],kernel, kcx, kcy);
	}


public static EvPixels whitetophat(EvPixels in, EvPixels kernel, int kcx, int kcy)
	{
	//This can be made about 50% faster by specializing the code
	return new OpImageSubImage().exec1(in, OpGrayMorphOpen2D.open(in,kernel,kcx,kcy));
	}
}