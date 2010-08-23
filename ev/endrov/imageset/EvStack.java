/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import endrov.util.EvDecimal;
import endrov.util.EvMathUtil;
import endrov.util.Tuple;

/**
 * One stack of images. Corresponds to one frame in one channel.
 * 
 * @author Johan Henriksson
 *
 */
public class EvStack implements AnyEvImage
	{
	/**
	 * TODO should not be public
	 */
	public TreeMap<EvDecimal, EvImage> loaders=new TreeMap<EvDecimal, EvImage>();
	
	/**
	 * Resolution [um/px]
	 */
	public double resX, resY;
	/**
	 * Resolution [um/px]. Do not use this variable directly
	 */
	public EvDecimal resZ;
	
	/**
	 * Displacement [pixels]
	 */
	public double dispX, dispY;

	/**
	 * ONLY used by putInt/getInt
	 * [um]
	 */
	public EvDecimal dispZ=EvDecimal.ZERO;
	
	/**
	 * Return combined resolution and binning. [px/um]
	 */
	public double getResbinX()
		{
		return 1.0/resX;
		}
	public double getResbinY()
		{
		return 1.0/resY;
		}

	/**
	 * Get combined resolution and binning. [um/px]
	 */
	public EvDecimal getResbinZinverted()
		{
		if(resZ==null)
			{
			//This is a temporary hack until we have moved completely to a new file format
			if(loaders.size()>=2)
				{
				Iterator<EvDecimal> f=loaders.keySet().iterator();
				EvDecimal z1=f.next();
				EvDecimal z2=f.next();
				resZ=z2.subtract(z1);
				dispZ=z1;
				}
			else
				{
				resZ=EvDecimal.ONE;
				System.out.println("Warning: getting resolution but there are no image planes to calculate it from");
				}
			}
		
		
		return resZ;
		}
	
	public double transformImageWorldX(double c){return (c+dispX)*resX;}
	public double transformImageWorldY(double c){return (c+dispY)*resY;}			
	public double transformImageWorldZ(double c){return c*getResbinZinverted().doubleValue()+dispZ.doubleValue();}
	
	
	public double transformWorldImageX(double c){return (c/resX-dispX);}
	public double transformWorldImageY(double c){return (c/resY-dispY);}
	public double transformWorldImageZ(double c){return (c-dispZ.doubleValue())/getResbinZinverted().doubleValue();}
	
	public double scaleImageWorldX(double c){return c/getResbinX();}
	public double scaleImageWorldY(double c){return c/getResbinY();}
	public double scaleImageWorldZ(double c){return c*getResbinZinverted().doubleValue();}
	
	public double scaleWorldImageX(double c){return c*getResbinX();}
	public double scaleWorldImageY(double c){return c*getResbinY();}
	public double scaleWorldImageZ(double c){return c/getResbinZinverted().doubleValue();}
	
	
	public Vector2d transformImageWorld(Vector2d v)
		{
		return new Vector2d(transformImageWorldX(v.x),transformImageWorldY(v.y));
		}
	
	public Vector3d transformImageWorld(Vector3d v)
		{
		return new Vector3d(transformImageWorldX(v.x),transformImageWorldY(v.y),transformImageWorldZ(v.z));
		}

	public Vector2d transformWorldImage(Vector2d v)
		{
		return new Vector2d(transformWorldImageX(v.x),transformWorldImageY(v.y));
		}

	public Vector3d transformWorldImage(Vector3d v)
		{
		return new Vector3d(transformWorldImageX(v.x),transformWorldImageY(v.y), transformWorldImageZ(v.z));
		}
	
	
	/**
	 * Copy metadata (resolution) from another stack
	 */
	public void getMetaFrom(EvStack o)
		{
		resX=o.resX;
		resY=o.resY;
		resZ=o.getResbinZinverted();
		dispX=o.dispX;
		dispY=o.dispY;
		dispZ=o.dispZ;
		}
	
	/**
	 * Allocate a 3d stack. ref will disappear later. instead have d.
	 * Covers getMetaFrom as well.
	 */
	public void allocate(int w, int h, int d, EvPixelsType type, EvStack ref)
		{
		if(ref==null)
			{
			resX=1;
			resY=1;
			resZ=EvDecimal.ONE;
			dispX=0;
			dispY=0;
			dispZ=EvDecimal.ZERO;
			}
		else
			{
			resX=ref.resX;
			resY=ref.resY;
			resZ=ref.getResbinZinverted();
			dispX=ref.dispX;
			dispY=ref.dispY;
			dispZ=ref.dispZ;
			}
		
		//Remove old images. Add up new image planes
		loaders.clear();
		for(int i=0;i<d;i++)
			{
			EvImage evim=new EvImage();
			evim.setPixelsReference(new EvPixels(type,w,h));
			putInt(i, evim);
			}
		}
	
	/**
	 * Allocate a 3d stack
	 */
	/*
	public void allocate(int w, int h, int d, int type)
		{
		setTrivialResolution();
		for(int i=0;i<d;i++)
			{
			EvImage evim=new EvImage();
			evim.setPixelsReference(new EvPixels(type,w,h));
			putInt(i, evim);
			}
		}
*/
	
	//TODO lazy generation of the stack
	
	public int closestZ(double worldZ)
		{
		int closestZ=(int)EvMathUtil.clamp((worldZ-dispZ.doubleValue())/resZ.doubleValue(),0,getDepth()-1);
		return closestZ;
		}
	
	/**
	 * Get one image plane
	 */
	/*
	public EvImage get(EvDecimal z)
		{
		return loaders.get(z);
		}*/
	
	public EvImage getInt(int z)
		{
		int i=0;
		for(EvImage evim:loaders.values())
			{
			if(i==z)
				return evim;
			i++;
			}
		throw new RuntimeException("Out of bounds");
		}
	
	/**
	 * Set one image plane
	 */
	public void put(EvDecimal z, EvImage im)
		{
		loaders.put(z,im);
		}
	
	/**
	 * Set one image plane
	 */
	public void putInt(int z, EvImage im)
		{
		loaders.put(new EvDecimal(z).multiply(getResbinZinverted()).add(dispZ),im);
		}

	/**
	 * Remove one image plane
	 */
	public void remove(EvDecimal z)
		{
		loaders.remove(z);
		}

	
	/**
	 * Find the closest slice given a frame and slice
	 * @param z Z we wish to match
	 * @return Same z if frame does not exist or no slices exist, otherwise the closest z
	 */
	/*
	public EvDecimal closestZ(EvDecimal z)
		{
		TreeMap<EvDecimal, EvImage> slices=loaders;
		if(slices==null || slices.size()==0)
			return z;
		else
			{
			SortedMap<EvDecimal,EvImage> before=slices.headMap(z);
			SortedMap<EvDecimal,EvImage> after=slices.tailMap(z);
			if(before.size()==0)
				return after.firstKey();
			else if(after.size()==0)
				return before.lastKey();
			else
				{
				EvDecimal afterkey=after.firstKey();
				EvDecimal beforekey=before.lastKey();
				
				if(afterkey.subtract(z).less(z.subtract(beforekey)))
					return afterkey;
				else
					return beforekey;
				}
			}
		}*/

	
	/**
	 * Find the closest slice above given a slice in a frame
	 * @param z Z we wish to match
	 * @return Same z if frame does not exist or no slices exist, otherwise the closest z above
	 */
	public EvDecimal closestZAbove(EvDecimal z)
		{
		TreeMap<EvDecimal,EvImage> slices=loaders;
		if(slices==null)
			return z;
		else
			{
			//Can be made faster
			SortedMap<EvDecimal,EvImage> after=new TreeMap<EvDecimal, EvImage>(slices.tailMap(z));
			after.remove(z);
			
			if(after.size()==0)
				return z;
			else
				return after.firstKey();
			}
		}
	
	
	/**
	 * Find the closest slice below given a slice in a frame
	 * @param z Z we wish to match
	 * @return Same z if frame does not exist or no slices exist, otherwise the closest z below
	 */
	public EvDecimal closestZBelow(EvDecimal z)
		{
		TreeMap<EvDecimal, EvImage> slices=loaders;
		if(slices==null)
			return z;
		else
			{
			SortedMap<EvDecimal, EvImage> before=slices.headMap(z);
			if(before.size()==0)
				return z;
			else
				return before.lastKey();
			}
		}		
	
	/**
	 * First (lowest) value of Z, or null
	 */
	/*public EvDecimal firstZ()
		{
		return loaders.firstKey();
		}*/
	
	 

	/**
	 * Last (largest) value of Z, or null
	 */
	/*public EvDecimal lastZ()
		{
		return loaders.lastKey();
		}*/

	/**
	 * Z's. This set can be modified and changes will be reflected.
	 * It might not be modifiable in the future.
	 */
	public Set<EvDecimal> keySet()
		{
		return loaders.keySet();
		}

	/**
	 * All image planes. This set can be modified and changes will be reflected.
	 * It might not be modifiable in the future.
	 */
	public Set<Map.Entry<EvDecimal, EvImage>> entrySet()
		{
		return loaders.entrySet();
		}

	public Tuple<EvDecimal, EvImage> firstEntry()
		{
		EvDecimal k=loaders.firstKey();
		return Tuple.make(k, loaders.get(k));
		}
	
	/**
	 * Get first (lowest) image in stack. Meant to be used when stack only contains one image (no z). 
	 */
	public EvImage getFirstImage()
		{
		return loaders.get(loaders.firstKey());
		}
	
	
	/**
	 * TODO require and enforce that somehow all layers have the same width and height
	 * Get width in number of pixels
	 */
	public int getWidth()
		{
		return firstEntry().snd().getPixels().getWidth();
		}
	
	/**
	 * Get height in number of pixels
	 */
	public int getHeight()
		{
		return firstEntry().snd().getPixels().getHeight();
		}
	

	/**
	 * Get the number of image planes
	 */
	public int getDepth()
		{
		return loaders.size();
		}
		
	/**
	 * Get array of pixels. This will cause all layers to be loaded so it should only be used
	 * when all pixels will be used
	 */
	public EvPixels[] getPixels()
		{
		EvPixels[] arr=new EvPixels[loaders.size()];
		int i=0;
		for(EvImage evim:loaders.values())
			arr[i++]=evim.getPixels();
		return arr;
		}
	
	/**
	 * Get array of images
	 */
	public EvImage[] getImages()
		{
		EvImage[] arr=new EvImage[loaders.size()];
		int i=0;
		for(EvImage evim:loaders.values())
			arr[i++]=evim;
		return arr;
		}
	
	/**
	 * Return the pixel arrays for every plane. Will do a read-only conversion automatically
	 */
	public int[][] getReadOnlyArraysInt()
		{
		EvPixels[] parr2=getPixels();
		int[][] parr=new int[parr2.length][];
		for(int i=0;i<parr2.length;i++)
			parr[i]=parr2[i].getReadOnly(EvPixelsType.INT).getArrayInt();
		return parr;
		}
	

	/**
	 * Return the pixel arrays for every plane. This is the original pixel data; meant for the
	 * time when you write it
	 */
	public int[][] getOrigArraysInt()
		{
		EvPixels[] parr2=getPixels();
		int[][] parr=new int[parr2.length][];
		for(int i=0;i<parr2.length;i++)
			parr[i]=parr2[i].getArrayInt();
		return parr;
		}

	/**
	 * Return the pixel arrays for every plane. This is the original pixel data; meant for the
	 * time when you write it
	 */
	public double[][] getOrigArraysDouble()
		{
		EvPixels[] parr2=getPixels();
		double[][] parr=new double[parr2.length][];
		for(int i=0;i<parr2.length;i++)
			parr[i]=parr2[i].getArrayDouble();
		return parr;
		}

	
	/**
	 * Return the pixel arrays for every plane. Will do a read-only conversion automatically
	 */
	public double[][] getReadOnlyArraysDouble()
		{
		EvPixels[] parr2=getPixels();
		double[][] parr=new double[parr2.length][];
		for(int i=0;i<parr2.length;i++)
			parr[i]=parr2[i].getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		return parr;
		}

	
	/**
	 * Set an arbitrary resolution, enough metadata to make it displayable. Useful for generated data such as kernels. 
	 */
	public void setTrivialResolution()
		{
		resX=1;
		resY=1;
		//binning=1;
		resZ=EvDecimal.ONE;
		}
	
	/**
	 * Calculate the pixel position in the middle, with decimals if needed.
	 * I have done minor (but relevant) mistakes in this calculation before so here it is, once and for all.
	 */
	public static double calcMidCoordinate(int w)
		{
		return (w-1)/2.0;
		}
	
	
	public void forceEvaluation()
		{
		for(EvImage im:getImages())
			im.getPixels();
//			im.forceEvaluation();
		}
	
	/*
	@Override
	protected void finalize() throws Throwable
		{
		super.finalize();
		if(!loaders.isEmpty())
			System.out.println("-----------------------------finalize stack "+this);
		}
*/	
	
	
	
	}
