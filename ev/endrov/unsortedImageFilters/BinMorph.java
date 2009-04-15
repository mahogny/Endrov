package endrov.unsortedImageFilters;

import java.util.LinkedList;
import java.util.List;

import endrov.imageset.EvPixels;
import endrov.util.Vector2i;

//These operations can be made faster using RLE images
/**
 * Binary morphology
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 */
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
	

	/**
	 * Close: Erode, then dilate
	 */
	public static EvPixels close(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		return dilate(erode(in,kernel,kcx,kcy),kernel,kcx,kcy);
		}

	/**
	 * Open: dilate, then erode
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
		//TODO maybe only useful with graylevel morphology
		
		//This can be made about 50% faster by specializing the code
		return ImageMath.minus(in, open(in,kernel,kcx,kcy));
		}
	
	/**
	 * Black Tophat: BTH(image)=close(image) - image
	 */
	public static EvPixels blacktophat(EvPixels in, EvPixels kernel, int kcx, int kcy)
		{
		//TODO maybe only useful with graylevel morphology
		
		//This can be made about 50% faster by specializing the code
		return ImageMath.minus(close(in,kernel,kcx,kcy), in);
		}


	/**
	 * Image^c
	 */
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
	
	/**
	 * Hitmiss(image) = erosion(image) INTERSECT erosion(image^c)
	 */
	public static EvPixels hitmiss(EvPixels in, EvPixels kernelHit, int hitKcx, int hitKcy, EvPixels kernelMiss, int missKcx, int missKcy)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		List<Vector2i> hitkpos=kernelPos(kernelHit, hitKcx, hitKcy);
		List<Vector2i> misskpos=kernelPos(kernelMiss, missKcx, missKcy);
		
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				{
				//Find with Hit kernel
				boolean found=true;
				find: for(Vector2i v:hitkpos)
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
					{
					//Find with Miss kernel on the complement
					find: for(Vector2i v:misskpos)
						{
						int kx=v.x+ax;
						int ky=v.y+ay;
						if(kx>=0 && kx<w && ky>=0 && ky<h)
							{
							if(inPixels[in.getPixelIndex(kx, ky)]!=0)
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
					
					//Output value
					if(found)
						outPixels[i]=1;
					else
						outPixels[i]=0;
					}
				else
					outPixels[i]=0;
				}
		
		return out;
		}
	
	
	/**
	 * Thin(image)=image-hitmiss(image)
	 * 
	 * Can be used to skeletonize images by application until convergence
	 * 
	 * There are other versions that could be implemented
	 */
	public static EvPixels thin(EvPixels in, EvPixels kernelHit, int hitKcx, int hitKcy, EvPixels kernelMiss, int missKcx, int missKcy)
		{
		return ImageMath.minus(in, hitmiss(in,kernelHit,hitKcx,hitKcy,kernelMiss,missKcx,missKcy));
		
		//could be made a lot faster for repeated application by keeping a front-set. Either way, probably want to return if there is more to do?
		
		}
	
	/**
	 * Thick(image)=image+hitmiss(image)
	 * 
	 * There are other versions that could be implemented
	 */
	public static EvPixels thick(EvPixels in, EvPixels kernelHit, int hitKcx, int hitKcy, EvPixels kernelMiss, int missKcx, int missKcy)
		{
		return ImageMath.minus(in, hitmiss(in,kernelHit,hitKcx,hitKcy,kernelMiss,missKcx,missKcy));
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

