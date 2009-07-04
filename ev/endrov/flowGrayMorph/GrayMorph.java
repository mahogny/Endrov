package endrov.flowGrayMorph;

import java.util.*;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Tuple;
import endrov.util.Vector2i;

/**
 * Gray scale morphology
 * 
 * TODO verify against matlab
 * TODO what about 0 pixels? definition area?
 * 
 * @author Johan Henriksson
 *
 */
public class GrayMorph
	{
	
	/**
	 * Turn kernel image into a list of positions
	 */
	public static List<Tuple<Vector2i,Integer>> kernelPos(EvPixels kernel, int kcx, int kcy)
		{
		LinkedList<Tuple<Vector2i,Integer>> list=new LinkedList<Tuple<Vector2i,Integer>>();
		kernel=kernel.getReadOnly(EvPixelsType.INT);
		int w=kernel.getWidth();
		int h=kernel.getHeight();
		int[] inPixels=kernel.getArrayInt();
		
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				{
				int p=inPixels[kernel.getPixelIndex(ax, ay)];
				if(p!=0)
					list.add(Tuple.make(new Vector2i(ax-kcx,ay-kcy),p));
				}
		
		return list;
		}
	
	
	}
