package endrov.unsortedImageFilters.grayMorph;

import endrov.flow.OpSlice1;
import endrov.imageset.EvPixels;

/**
 * Close: Erode, then dilate
 * <br/>
 * in (.) kernel
 * <br/>
 * in ⊙ kernel
 * @author Johan Henriksson
 */
public class OpGrayMorphClose2D extends OpSlice1
	{
	private int kcx,kcy;
	private EvPixels kernel;
	public OpGrayMorphClose2D(int kcx, int kcy, EvPixels kernel)
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
		return OpGrayMorphDilate2D.dilate(OpGrayMorphErode2D.erode(in,kernel,kcx,kcy),kernel,kcx,kcy);
		}
	}