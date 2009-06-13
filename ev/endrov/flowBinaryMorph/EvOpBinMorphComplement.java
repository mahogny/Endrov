package endrov.flowBinaryMorph;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;

/**
 * Image^c
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpBinMorphComplement extends EvOpSlice1
	{
	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return complement(p[0]);
		}

	

public static EvPixels complement(EvPixels in)
	{
	in=in.convertTo(EvPixels.TYPE_INT, true);
	int w=in.getWidth();
	int h=in.getHeight();
	EvPixels out=new EvPixels(in.getType(),w,h);
	int[] inPixels=in.getArrayInt();
	int[] outPixels=out.getArrayInt();
	
	for(int i=0;i<inPixels.length;i++)
		outPixels[i]=inPixels[i]!=0 ? 0 : 1;
		
	return out;
	}
	}