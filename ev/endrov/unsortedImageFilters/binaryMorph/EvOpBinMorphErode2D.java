package endrov.unsortedImageFilters.binaryMorph;

import java.util.List;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.util.Vector2i;

/**
 * in (-) kernel.
 * <br/>
 * Kernel has a specified center kcx,kcy. Outside image assumed empty. 
 * <br/>
 * Complexity O(w*h*kw*kh)
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpBinMorphErode2D extends EvOpSlice1
	{
	private int kcx,kcy;
	private EvPixels kernel;
	public EvOpBinMorphErode2D(int kcx, int kcy, EvPixels kernel)
		{
		this.kcx = kcx;
		this.kcy = kcy;
		this.kernel = kernel;
		}
	
	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return erode(p[0],kernel, kcx, kcy);
		}

	public static EvPixels erode(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		List<Vector2i> kpos=BinMorph.kernelPos(kernel, kcx, kcy);
		
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				{
				boolean found=true;
				find: for(Vector2i v:kpos)
					{
					int kx=v.x+ax;
					int ky=v.y+ay;
					if(kx>=0 && kx<w && ky>=0 && ky<h)
						{
						if(inPixels[in.getPixelIndex(kx, ky)]==0)
							{
							found=false;
							break find;
							}
						}
					else
						{
						found=false;
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