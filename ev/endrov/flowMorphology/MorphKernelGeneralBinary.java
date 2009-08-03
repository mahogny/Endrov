package endrov.flowMorphology;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import endrov.flowBasic.math.EvOpImageSubImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector2i;

/**
 * Binary morphology. Unstructured kernel, normally O(numKernelPixels) for each pixel.
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * 
 * @author Johan Henriksson
 *
 */
public class MorphKernelGeneralBinary extends MorphKernel
	{
	private List<Vector2i> kernelPixelList;
	
	/**
	 * Turn kernel image into a list of positions
	 * 
	 * Idea: some operations might be faster if the order is randomized due to spatial correlation.
	 * But: less memory locality
	 * 
	 */
	public MorphKernelGeneralBinary(EvPixels kernel, int kcx, int kcy)
		{
		LinkedList<Vector2i> list=new LinkedList<Vector2i>();
		kernel=kernel.getReadOnly(EvPixelsType.INT);
		int w=kernel.getWidth();
		int h=kernel.getHeight();
		int[] inPixels=kernel.getArrayInt();
		
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				if(inPixels[kernel.getPixelIndex(ax, ay)]!=0)
					list.add(new Vector2i(ax-kcx,ay-kcy));

		this.kernelPixelList=list;
		}
	
	public MorphKernelGeneralBinary(Collection<Vector2i> list)
		{
		this.kernelPixelList=new LinkedList<Vector2i>(list);
		}
	
	public MorphKernelGeneralBinary reflect()
		{
		LinkedList<Vector2i> list=new LinkedList<Vector2i>();
		for(Vector2i v:kernelPixelList)
			list.add(new Vector2i(-v.x,-v.y));
		return new MorphKernelGeneralBinary(list);
		}


	
	public List<Vector2i> getKernelPos()
		{
		return kernelPixelList;
		}
	
	
	
	/**
 * in (+) kernel. 
 * <br/>
 * Kernel has a specified center kcx,kcy. Outside image assumed empty. 
 * <br/>
 * Complexity O(w*h*kw*kh)
	 */
	public EvPixels dilate(EvPixels in)
		{
		in=in.getReadOnly(EvPixelsType.INT);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();

		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				{
				boolean found=false;
				find: for(Vector2i v:kernelPixelList)
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
	 * in (-) kernel.
	 * <br/>
	 * Kernel has a specified center kcx,kcy. Outside image assumed empty. 
	 * <br/>
	 * Complexity O(w*h*kw*kh)
	 */
	public EvPixels erode(EvPixels in)
		{
		in=in.getReadOnly(EvPixelsType.INT);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
				
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				{
				boolean found=true;
				find: for(Vector2i v:kernelPixelList)
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
	 * Open: dilate, then erode
	 */
	public EvPixels open(EvPixels in)
		{
		return reflect().dilate(erode(in));
		}


	/**
	 * Close: Erode, then dilate
	 * <br/>
	 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
	 */
	public EvPixels close(EvPixels in)
		{
		return reflect().erode(dilate(in));
		}

	
	/**
	 * White Tophat: WTH(image)=image - open(image)
	 */
	public EvPixels whitetophat(EvPixels in)
		{
		//This can be made about 50% faster by specializing the code
		return new EvOpImageSubImage().exec1(in, open(in));
		}

	
	/**
	 * Black Tophat: BTH(image)=close(image) - image
	 */
	public EvPixels blacktophat(EvPixels in)
		{
		//This can be made about 50% faster by specializing the code
		return new EvOpImageSubImage().exec1(close(in), in);
		}
	
	/**
	 * Internal gradient: image-erode(image)
	 */
	public EvPixels internalGradient(EvPixels in)
		{
		return new EvOpImageSubImage().exec1(in,erode(in));
		}

	/**
	 * External gradient: dilate(image)-image
	 */
	public EvPixels externalGradient(EvPixels in)
		{
		return new EvOpImageSubImage().exec1(dilate(in),in);
		}

	/**
	 * Whole gradient: dilate(image)-erode(image)
	 */
	public EvPixels wholeGradient(EvPixels in)
		{
		return new EvOpImageSubImage().exec1(dilate(in),erode(in));
		}

	
	}
