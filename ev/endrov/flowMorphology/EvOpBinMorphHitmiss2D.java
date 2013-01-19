/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import java.util.List;

import endrov.flow.EvOpSlice1;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;
import endrov.util.math.Vector2i;

/**
 * Hitmiss(image) = erosion(image) INTERSECT erosion(image^c)
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpBinMorphHitmiss2D extends EvOpSlice1
	{
	private final MorphKernel kernelHit, kernelMiss;
	
	
	public EvOpBinMorphHitmiss2D(MorphKernel kernelHit,	MorphKernel kernelMiss)
		{
		this.kernelHit = kernelHit;
		this.kernelMiss = kernelMiss;
		}

	@Override
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return hitmiss(p[0], kernelHit, kernelMiss);
		}
	
	public static EvPixels hitmiss(EvPixels in, MorphKernel kernelHit, MorphKernel kernelMiss)
		{
		in=in.getReadOnly(EvPixelsType.INT);
		int w=in.getWidth();
		int h=in.getHeight();
		EvPixels out=new EvPixels(in.getType(),w,h);
		int[] inPixels=in.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		List<Vector2i> hitkpos=kernelHit.getKernelPos();
		List<Vector2i> misskpos=kernelMiss.getKernelPos();
		
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
	}