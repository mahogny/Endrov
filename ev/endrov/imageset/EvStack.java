/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.util.ArrayList;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import endrov.util.EvMathUtil;

/**
 * One stack of images. Corresponds to one frame in one channel.
 * 
 * @author Johan Henriksson
 *
 */
public class EvStack implements AnyEvImage
	{
	/**
	 * All the images
	 */
	private ArrayList<EvImage> loaders=new ArrayList<EvImage>();
	
	/**
	 * Resolution [um/px]
	 */
	public double resX;
	public double resY;
	public double resZ;
	
	/**
	 * Displacement [um]
	 */
	public double dispY;
	public double dispX;
	public double dispZ;

	/**
	 * Rotation of stack. Rotation is applied AFTER displacement.
	 * For speed, matrices in both directions are stored.
	 */
	private Matrix3d rotationToStack=new Matrix3d(
			1,0,0,
			0,1,0,
			0,0,1
			);
	private Matrix3d rotationToWorld=new Matrix3d(
			1,0,0,
			0,1,0,
			0,0,1
			);
	
	
	
	
	public double transformImageWorldX(double c){return c*resX+dispX;}
	public double transformImageWorldY(double c){return c*resY+dispY;}			
	public double transformImageWorldZ(double c){return c*resZ+dispZ;}	
	
	public double transformWorldImageX(double c){return (c-dispX)/resX;}
	public double transformWorldImageY(double c){return (c-dispY)/resY;}
	public double transformWorldImageZ(double c){return (c-dispZ)/resZ;}

	public Vector3d scaleWorldImage(Vector3d v)
		{
		return new Vector3d(v.x/resZ, v.y/resZ, v.z/resZ);
		}

	public Vector2d transformImageWorld(Vector2d v)
		{
		return new Vector2d(transformImageWorldX(v.x),transformImageWorldY(v.y));
		}
	
	public Vector2d transformWorldImage(Vector2d v)
		{
		return new Vector2d(transformWorldImageX(v.x),transformWorldImageY(v.y));
		}
	
	public Vector3d transformImageWorld(Vector3d v)
		{
		return new Vector3d(transformImageWorldX(v.x),transformImageWorldY(v.y), transformImageWorldZ(v.z));
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
		resZ=o.resZ;
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
			resZ=1;
			}
		else
			{
			resX=ref.resX;
			resY=ref.resY;
			resZ=ref.resZ;
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
			loaders.add(evim);
//			putInt(i, evim);
			}
		}
	
	/**
	 * Remove all evimage - doubtful if this method should stay
	 */
	public void clearStack()
		{
		loaders.clear();
		}
	
	
	//TODO lazy generation of the stack
	
	public int closestZint(double worldZ)
		{
		/*
		System.out.println("resz "+resZ+"  dispz "+dispZ);
		EvDecimal wc=closestZ(new EvDecimal(worldZ));
		int zi=(int)Math.round(wc.subtract(dispZ).divide(resZ).doubleValue());
		return zi;
		*/
		
		//This calculation is not really true yet. Use later
		int closestZ=(int)EvMathUtil.clamp(Math.round((worldZ-dispZ)/resZ),0,getDepth()-1);
		return closestZ;
		
		}
	
	/*
	public Set<Integer> getZints()
		{
		TreeSet<Integer> zs=new TreeSet<Integer>();
		for(EvDecimal d:loaders.keySet())
			zs.add((int)Math.round(d.subtract(dispZ).divide(resZ).doubleValue()));
		return zs;
		
		}*/
	
	
	/**
	 * Get one image plane
	 * TODO should be O(1)
	 */
	public EvImage getInt(int z)
		{
		return loaders.get(z);
		
		/*
		//// THIS CODE IS CORRECT! but it screws up a lot of code at the moment. convert all imagesets first
		EvDecimal wz=resZ.multiply(z).add(dispZ);  //Note. I always find these multiplications scary. need to get rid of them
		EvImage evim=loaders.get(wz);
		if(evim==null)
			throw new RuntimeException("Out of bounds: "+z+" (world z="+wz+")");
		else
			return evim;
			*/
		
		/*
		//TODO THIS CODE IS WRONG!!! sort of.
		//It does not work when slices are missing, but this should not be allowed in the future
		int i=0;
		for(EvImage evim:loaders.values())
			{
			if(i==z)
				return evim;
			i++;
			}
			
		throw new RuntimeException("Out of bounds: "+z+" depth is "+getDepth());
		*/
		}

	public boolean hasInt(int z)
		{
		try
			{
			getInt(z);
			return true;
			}
		catch (Exception e)
			{
			return false;
			}
		}
	
	/**
	 * Set one image plane
	 */
	public void putInt(int z, EvImage im)
		{
		while(loaders.size()<=z) //Ensure there are placeholders
			loaders.add(null);
		loaders.set(z,im);
		
//		loaders.put(z,im);
//		loaders.put(new EvDecimal(z).multiply(resZ).add(dispZ),im);
		}

	/**
	 * Remove one image plane
	 */
	/*
	public void remove(EvDecimal z)
		{
		loaders.remove(z);
		}
*/
	
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
	/*
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
		}*/
	
	
	/**
	 * Find the closest slice below given a slice in a frame
	 * @param z Z we wish to match
	 * @return Same z if frame does not exist or no slices exist, otherwise the closest z below
	 */
	/*
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
		}		*/
	

	/**
	 * Z's. This set can be modified and changes will be reflected.
	 * It might not be modifiable in the future.
	 * @deprecated
	 */
	/*
	public Set<EvDecimal> keySet()
		{
		return loaders.keySet();
		}*/

	
	/**
	 * Get first (lowest) image in stack. Meant to be used when stack only contains one image (no z). 
	 */
	public EvImage getFirstImage()
		{
		return loaders.get(0);
//		return loaders.get(loaders.firstKey());
		}
	
	
	/**
	 * TODO require and enforce that somehow all layers have the same width and height
	 * Get width in number of pixels
	 */
	public int getWidth()
		{
		return getFirstImage().getPixels().getWidth();
		}
	
	/**
	 * Get height in number of pixels
	 */
	public int getHeight()
		{
		return getFirstImage().getPixels().getHeight();
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
		for(EvImage evim:loaders)
//		for(EvImage evim:loaders.values())
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
		for(EvImage evim:loaders)
//		for(EvImage evim:loaders.values())
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
		resZ=1;
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
	public void setDispXpx(double dispX)
		{
			this.dispX = dispX;
		}
	public double getDispXpx()
		{
			return dispX;
		}
	public void setDispYpx(double dispY)
		{
			this.dispY = dispY;
		}
	public double getDispYpx()
		{
			return dispY;
		}
	*/
	
/*
	public void setDispXum(double dispX)
		{
			this.dispX = dispX;
		}
	public double getDispXum()
		{
			return dispX;
		}
	public void setDispYum(double dispY)
		{
			this.dispY = dispY;
		}
	public double getDispYum()
		{
			return dispY;
		}*/
	
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
