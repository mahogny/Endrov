package endrov.unsortedImageFilters.grayMorph;

import endrov.flow.EvOpSlice1;
import endrov.flow.std.math.EvOpImageSubImage;
import endrov.imageset.EvPixels;

/**
 * White Tophat: WTH(image)=image - open(image)
 * @author Johan Henriksson
 */
public class EvOpGrayMorphWhiteTophat2D extends EvOpSlice1
{
private int kcx,kcy;
private EvPixels kernel;
public EvOpGrayMorphWhiteTophat2D(int kcx, int kcy, EvPixels kernel)
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
	return new EvOpImageSubImage().exec1(in, EvOpGrayMorphOpen2D.open(in,kernel,kcx,kcy));
	}
}