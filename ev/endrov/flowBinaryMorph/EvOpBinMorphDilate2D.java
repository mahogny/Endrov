package endrov.flowBinaryMorph;

import java.util.List;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector2i;

/**
 * in (+) kernel. 
 * <br/>
 * Kernel has a specified center kcx,kcy. Outside image assumed empty. 
 * <br/>
 * Complexity O(w*h*kw*kh)
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpBinMorphDilate2D extends EvOpSlice1
	{
	private BinMorphKernel kernel;

	public EvOpBinMorphDilate2D(BinMorphKernel kernel)
		{
		this.kernel = kernel;
		}


	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return dilate(p[0],kernel);
		}


	public static EvPixels dilate(EvPixels in, BinMorphKernel kernel)
		{
		in=in.getReadOnly(EvPixelsType.INT);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();

		List<Vector2i> kpos=kernel.kernel;

		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				{
				boolean found=false;
				find: for(Vector2i v:kpos)
					{
					int kx=v.x+ax;
					int ky=v.y+ay;
					if(kx>=0 && kx<w && ky>=0 && ky<h)
						if(inPixels[in.getPixelIndex(kx, ky)]!=0)
							{
							found=true;
							break find;
							}
					}
				int i=out.getPixelIndex(ax, ay);
				if(found)
					outPixels[i]=1;
				else
					outPixels[i]=0;
				}

		return out;
		}

	}