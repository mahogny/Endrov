package endrov.unsortedImageFilters.binaryMorph;

import endrov.flow.EvOpSlice1;
import endrov.flow.std.math.EvOpImageSubImage;
import endrov.imageset.EvPixels;

/**
 * Thin(image)=image-hitmiss(image). 
 * <br/>
 * 
 * Can be used to skeletonize images by application until convergence. 
 * 
 * There are other versions that could be implemented
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpBinMorphThin2D extends EvOpSlice1
	{
	private EvPixels kernelHit;
	private int hitKcx;
	private int hitKcy;
	private EvPixels kernelMiss;
	private int missKcx;
	private int missKcy;
	
	public EvOpBinMorphThin2D(EvPixels kernelHit, int hitKcx, int hitKcy,
			EvPixels kernelMiss, int missKcx, int missKcy)
		{
		this.kernelHit = kernelHit;
		this.hitKcx = hitKcx;
		this.hitKcy = hitKcy;
		this.kernelMiss = kernelMiss;
		this.missKcx = missKcx;
		this.missKcy = missKcy;
		}

	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return thin(p[0], kernelHit, hitKcx, hitKcy, kernelMiss, missKcx, missKcy);
		}
	
	public static EvPixels thin(EvPixels in, EvPixels kernelHit, int hitKcx, int hitKcy, EvPixels kernelMiss, int missKcx, int missKcy)
		{
		return new EvOpImageSubImage().exec1(in, EvOpBinMorphHitmiss2D.hitmiss(in,kernelHit,hitKcx,hitKcy,kernelMiss,missKcx,missKcy));
		
		//could be made a lot faster for repeated application by keeping a front-set. Either way, probably want to return if there is more to do?
		
		}
	}