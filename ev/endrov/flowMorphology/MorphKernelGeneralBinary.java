/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import endrov.flowBasic.math.EvOpImageSubImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.ProgressHandle;
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
	private List<Vector2i> kernelPixelHitList;
	private List<Vector2i> kernelPixelMissList;
	
	/**
	 * Turn kernel image into a list of positions
	 * 
	 * Idea: some operations might be faster if the order is randomized due to spatial correlation.
	 * But: less memory locality
	 * 
	 */
	public MorphKernelGeneralBinary(EvPixels kernelHit, int kcx, int kcy)
		{
		LinkedList<Vector2i> list=new LinkedList<Vector2i>();
		kernelHit=kernelHit.getReadOnly(EvPixelsType.INT);
		int w=kernelHit.getWidth();
		int h=kernelHit.getHeight();
		int[] inPixels=kernelHit.getArrayInt();
		
		for(int ay=0;ay<h;ay++)
			for(int ax=0;ax<w;ax++)
				if(inPixels[kernelHit.getPixelIndex(ax, ay)]!=0)
					list.add(new Vector2i(ax-kcx,ay-kcy));

		this.kernelPixelHitList=list;
		}
	
	public MorphKernelGeneralBinary(Collection<Vector2i> listHit, Collection<Vector2i> listMiss)
		{
		this.kernelPixelHitList=new LinkedList<Vector2i>(listHit);
		this.kernelPixelMissList=new LinkedList<Vector2i>(listMiss);
		}
	
	public MorphKernelGeneralBinary reflect()
		{
		LinkedList<Vector2i> listHit=new LinkedList<Vector2i>();
		for(Vector2i v:kernelPixelHitList)
			listHit.add(new Vector2i(-v.x,-v.y));
		LinkedList<Vector2i> listMiss=new LinkedList<Vector2i>();
		for(Vector2i v:kernelPixelMissList)
			listMiss.add(new Vector2i(-v.x,-v.y));
		return new MorphKernelGeneralBinary(listHit, listMiss);
		}

	/**
	 * Hit and miss transform
	 * <br/>
	 * Complexity O(w*h*#kernelPixels)
	 */
	public EvPixels hitmiss(ProgressHandle ph, EvPixels in)
		{
		return hitmissBinary(ph, new MorphKernelGeneralBinary(kernelPixelMissList,new LinkedList<Vector2i>()), in);
		}
	
	
	public List<Vector2i> getKernelPos()
		{
		return kernelPixelHitList;
		}
	
	
	
	/**
 * in (+) kernel. 
 * <br/>
 * Kernel has a specified center kcx,kcy. Outside image assumed empty. 
 * <br/>
 * Complexity O(w*h*#kernelPixels)
	 */
	public EvPixels dilate(ProgressHandle ph, EvPixels in)
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
				find: for(Vector2i v:kernelPixelHitList)
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
	 * Complexity O(w*h*#kernelPixels)
	 */
	public EvPixels erode(ProgressHandle ph, EvPixels in)
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
				find: for(Vector2i v:kernelPixelHitList)
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
	public EvPixels open(ProgressHandle ph, EvPixels in)
		{
		return reflect().dilate(ph, erode(ph, in));
		}


	/**
	 * Close: Erode, then dilate
	 * <br/>
	 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
	 */
	public EvPixels close(ProgressHandle ph, EvPixels in)
		{
		return reflect().erode(ph, dilate(ph, in));
		}

	
	/**
	 * White Tophat: WTH(image)=image - open(image)
	 */
	public EvPixels whitetophat(ProgressHandle ph, EvPixels in)
		{
		//This can be made about 50% faster by specializing the code
		return new EvOpImageSubImage().exec1(ph, in, open(ph, in));
		}

	
	/**
	 * Black Tophat: BTH(image)=close(image) - image
	 */
	public EvPixels blacktophat(ProgressHandle ph, EvPixels in)
		{
		//This can be made about 50% faster by specializing the code
		return new EvOpImageSubImage().exec1(ph, close(ph, in), in);
		}
	
	/**
	 * Internal gradient: image-erode(image)
	 */
	public EvPixels internalGradient(ProgressHandle ph, EvPixels in)
		{
		return new EvOpImageSubImage().exec1(ph, in,erode(ph, in));
		}

	/**
	 * External gradient: dilate(image)-image
	 */
	public EvPixels externalGradient(ProgressHandle ph, EvPixels in)
		{
		return new EvOpImageSubImage().exec1(ph, dilate(ph, in),in);
		}

	/**
	 * Whole gradient: dilate(image)-erode(image)
	 */
	public EvPixels wholeGradient(ProgressHandle ph, EvPixels in)
		{
		return new EvOpImageSubImage().exec1(ph, dilate(ph, in),erode(ph, in));
		}

	
	}
