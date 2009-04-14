package endrov.unsortedImageFilters;

import java.util.LinkedList;
import java.util.List;

import endrov.imageset.EvPixels;
import endrov.util.Vector2i;

//These operations can be made faster using RLE images
public class BinMorph
	{

	
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
	
	//Border is assumed to be empty for dilate and erode
	
	/**
	 * in (+) kernel. Kernel has a specified center kcx,kcy. Outside image assumed empty
	 */
	public static EvPixels dilate(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		List<Vector2i> kpos=kernelPos(kernel, kcx, kcy);
		
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
	
	/**
	 * in (-) kernel. Kernel has a specified center kcx,kcy. Outside image assumed empty
	 */
	public static EvPixels erode(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		List<Vector2i> kpos=kernelPos(kernel, kcx, kcy);
		
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
	
	
	public static EvPixels open(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		return dilate(erode(in,kernel,kcx,kcy),kernel,kcx,kcy);
		}

	
	public static EvPixels close(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		return erode(dilate(in,kernel,kcx,kcy),kernel,kcx,kcy);
		}
	
	
	
	//matlab handles borders differenly. matlab keeps more pixels with open . close
	

	}

