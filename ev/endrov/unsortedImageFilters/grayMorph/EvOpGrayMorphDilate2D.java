package endrov.unsortedImageFilters.grayMorph;

import java.util.List;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.util.Tuple;
import endrov.util.Vector2i;

/**
 * in ⊕ kernel.
 * <br/>
 * Kernel has a specified center kcx,kcy. Outside image assumed empty
 * <br/>
 * Definition: f⊕b (s,t) = max{f(s-x,t-y)+b(x,y) | (s-x,t-y)∊D_f, (x,y)∊D_b}
 * <br/>
 * Complexity O(w*h*kw*kh)
 * @author Johan Henriksson
 */
public class EvOpGrayMorphDilate2D extends EvOpSlice1
{
private int kcx,kcy;
private EvPixels kernel;
public EvOpGrayMorphDilate2D(int kcx, int kcy, EvPixels kernel)
	{
	this.kcx = kcx;
	this.kcy = kcy;
	this.kernel = kernel;
	}

@Override
public EvPixels exec1(EvPixels... p)
	{
	return dilate(p[0],kernel, kcx, kcy);
	}


public static EvPixels dilate(EvPixels in, EvPixels kernel, int kcx, int kcy)
	{
	in=in.convertTo(EvPixels.TYPE_INT, true);
	int w=in.getWidth();
	int h=in.getHeight();
	EvPixels out=new EvPixels(in.getType(),w,h);
	int[] inPixels=in.getArrayInt();
	int[] outPixels=out.getArrayInt();
	
	List<Tuple<Vector2i,Integer>> kpos=GrayMorph.kernelPos(kernel, kcx, kcy);
	
	for(int ay=0;ay<h;ay++)
		for(int ax=0;ax<w;ax++)
			{
			Integer outval=null;
			for(Tuple<Vector2i,Integer> e:kpos)
				{
				Vector2i v=e.fst();
				int p=e.snd();
				int kx=v.x+ax;
				int ky=v.y+ay;
				if(kx>=0 && kx<w && ky>=0 && ky<h)
					{
					int c=inPixels[in.getPixelIndex(kx, ky)]+p;
					if(outval==null || outval<c)
						outval=c;
					}
				}
			int i=out.getPixelIndex(ax, ay);
			if(outval==null)
				outPixels[i]=0; //Here we would have a serious problem?
			else
				outPixels[i]=outval;
			}
	
	return out;
	}
}