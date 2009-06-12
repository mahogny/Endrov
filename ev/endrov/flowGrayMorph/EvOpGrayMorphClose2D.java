package endrov.flowGrayMorph;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * Close: Erode, then dilate
 * <br/>
 * in (.) kernel
 * <br/>
 * in ⊙ kernel
 * @author Johan Henriksson
 */
public class EvOpGrayMorphClose2D extends EvOpSlice1
	{
	private int kcx,kcy;
	private EvPixels kernel;
	public EvOpGrayMorphClose2D(int kcx, int kcy, EvPixels kernel)
		{
		this.kcx = kcx;
		this.kcy = kcy;
		this.kernel = kernel;
		}
	
	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return close(p[0],kernel, kcx, kcy);
		}

	public static EvPixels close(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		return EvOpGrayMorphDilate2D.dilate(EvOpGrayMorphErode2D.erode(in,kernel,kcx,kcy),kernel,kcx,kcy);
		}
	}