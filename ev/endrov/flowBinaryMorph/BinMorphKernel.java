package endrov.flowBinaryMorph;

import java.util.LinkedList;
import java.util.List;

import endrov.flow.FlowType;
import endrov.imageset.EvPixels;
import endrov.util.Vector2i;

//These operations can be made faster using RLE images
/**
 * Binary morphology
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class BinMorphKernel
	{
	public List<Vector2i> kernel;
	
	public static final FlowType FLOWTYPE=new FlowType(BinMorphKernel.class);
	
	public BinMorphKernel(EvPixels kernel, int kcx, int kcy)
		{
		this.kernel=kernelPos(kernel, kcx, kcy);
		}
	
	/**
	 * Turn kernel image into a list of positions
	 */
	public static List<Vector2i> kernelPos(EvPixels kernel, int kcx, int kcy)
		{
		LinkedList<Vector2i> list=new LinkedList<Vector2i>();
		kernel=kernel.convertTo(EvPixels.TYPE_INT, true);
		int w=kernel.getWidth();
		int h=kernel.getHeight();
		int[] inPixels=kernel.getArrayInt();
		
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				if(inPixels[kernel.getPixelIndex(ax, ay)]!=0)
					list.add(new Vector2i(ax-kcx,ay-kcy));
		
		return list;
		}
	
	/**
	 * Prune skeleton: Remove endpoints numTimes or until converged.
	 */
	/*
	public static EvPixels pruneSkeleton(Integer numTimes)
		{
		
		}*/
	
	//matlab handles borders differenly. matlab keeps more pixels with open . close
	
	/**
	 * Fillhole: Fill all minimas not connected to the image border.
	 * Soille and gratin, 1994 - fast algorithm  
	 */
	
	/*
	 * http://www.fmwconcepts.com/imagemagick/morphology/index.php
	 * imagemagick operations left to code: majority, edgein,edgeout, feather, average, spread,bottomhat,  
	*/

	}

