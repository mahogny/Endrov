package endrov.unsortedImageFilters;

import java.util.*;

import endrov.flow.std.math.OpImageSubImage;
import endrov.imageset.EvPixels;
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
		kernel=kernel.convertTo(EvPixels.TYPE_INT, true);
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
	
	
	/**
	 * in ⊕ kernel. Kernel has a specified center kcx,kcy. Outside image assumed empty
	 * 
	 * Definition: f⊕b (s,t) = max{f(s-x,t-y)+b(x,y) | (s-x,t-y)∊D_f, (x,y)∊D_b}
	 * 
	 * Complexity O(w*h*kw*kh)
	 */
	public static EvPixels dilate(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		List<Tuple<Vector2i,Integer>> kpos=kernelPos(kernel, kcx, kcy);
		
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
	
	
	/**
	 * in ⊖ kernel. Kernel has a specified center kcx,kcy. Outside image assumed empty
	 * 
	 * Definition: f(-)b (s,t) = min{f(s-x,t-y)-b(x,y) | (s-x,t-y)<-D_f, (x,y)<-D_b}
	 * Definition: f⊖b (s,t) = min{f(s-x,t-y)-b(x,y) | (s-x,t-y)∊D_f, (x,y)∊D_b}
	 * 
	 * Complexity O(w*h*kw*kh)
	 */
	public static EvPixels erode(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		List<Tuple<Vector2i,Integer>> kpos=kernelPos(kernel, kcx, kcy);
		
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
						int c=inPixels[in.getPixelIndex(kx, ky)]-p;
						if(outval==null || outval>c)
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
	
	
	/**
	 * Close: Erode, then dilate
	 * in (.) kernel
	 * in ⊙ kernel
	 */
	public static EvPixels close(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		return dilate(erode(in,kernel,kcx,kcy),kernel,kcx,kcy);
		}

	/**
	 * Open: dilate, then erode
	 * in (o) kernel
	 * in ⊚ kernel
	 * 
	 */
	public static EvPixels open(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		return erode(dilate(in,kernel,kcx,kcy),kernel,kcx,kcy);
		}
	
	
	/**
	 * White Tophat: WTH(image)=image - open(image)
	 */
	public static EvPixels whitetophat(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		//This can be made about 50% faster by specializing the code
		return new OpImageSubImage().exec(in, open(in,kernel,kcx,kcy));
		}
	
	/**
	 * Black Tophat: BTH(image)=close(image) - image
	 * Also called Bottomhat
	 */
	public static EvPixels blacktophat(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		//This can be made about 50% faster by specializing the code
		return new OpImageSubImage().exec(close(in,kernel,kcx,kcy), in);
		}
	
	
	
	}
