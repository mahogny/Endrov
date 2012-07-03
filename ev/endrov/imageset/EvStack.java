/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.util.ArrayList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import endrov.coordinateSystem.CoordinateSystem;
import endrov.util.EvMathUtil;
import endrov.util.ProgressHandle;

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
	
	public void setRes(double resX, double resY, double resZ)
		{
		this.resX=resX;
		this.resY=resY;
		this.resZ=resZ;

		//Update CS as well
/*		Matrix4d m=cs.getTransformToWorld();
		//Actually, matrix supposed to be 1 along diagonal for now - these values are not used. matrix is only for displacement
		m.m00=resX;
		m.m11=resY;
		m.m22=resZ;*/
		}
	
	public Vector3d getRes()
		{
		return new Vector3d(resX, resY, resZ);
		}
	
	/**
	 * Coordinate system for displacing and rotating the stack
	 */
	public CoordinateSystem cs=new CoordinateSystem();
	
	
	
	/**
	 * Displacement [um]
	 */
//	public double dispX;
//	public double dispY;
//	public double dispZ;

	/**
	 * Get displacement [um]
	 * Adding displacement takes a local coordinate to a world coordinate
	 */
	public Vector3d getDisplacement()
		{
		Matrix4d m=cs.getTransformToWorld();
		return new Vector3d(m.m03, m.m13, m.m23);
		}
	
	/**
	 * Set displacement [um]
	 */
	public void setDisplacement(Vector3d disp)
		{
		Matrix4d m=new Matrix4d(cs.getTransformToSystem()); 
		m.m03=disp.x;
		m.m13=disp.y;
		m.m23=disp.z;
		//System.out.println(m);
		Matrix4d toWorld=new Matrix4d();
		toWorld.invert(m);
		cs.setFromMatrices(m, toWorld);
		}
	
	public double oldGetDispX(){return cs.getTransformToWorld().m03;}
	public double oldGetDispY(){return cs.getTransformToWorld().m13;}
	public double oldGetDispZ(){return cs.getTransformToWorld().m23;}

	
	public double transformImageWorldX(double c){return c*resX+oldGetDispX();} //TODO: I think this is wrong
	public double transformImageWorldY(double c){return c*resY+oldGetDispY();}			
	public double transformImageWorldZ(double c){return c*resZ+oldGetDispZ();}	
	
	public double transformWorldImageX(double c){return (c-oldGetDispX())/resX;}
	public double transformWorldImageY(double c){return (c-oldGetDispY())/resY;}
	public double transformWorldImageZ(double c){return (c-oldGetDispZ())/resZ;}

	public Vector3d scaleWorldImage(Vector3d v)
		{
		return new Vector3d(v.x/resX, v.y/resY, v.z/resZ);
		}

	//TODO!!!! below must be equivalent with above
	
	
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
		return cs.transformToWorld(new Vector3d(v.x*resX, v.y*resY, v.z*resZ));   //Note ToSystem. this is because of the format of the matrix
		}
	public Vector3d transformWorldImage(Vector3d v)
		{
		Vector3d vv=cs.transformToSystem(v);
		return new Vector3d(vv.x/resX, vv.y/resY, vv.z/resZ);
		}
	
	public static void main(String[] args)
		{
		//Test of transforms!!
		
		EvStack st=new EvStack();
		st.setRes(2,1,3);
		st.setDisplacement(new Vector3d(1,2,3));
		Vector3d v=new Vector3d(5,6,7);
		
		System.out.println(st.transformWorldImage(v)+"    vs    "+st.transformWorldImageX(v.x)+"   "+st.transformWorldImageY(v.y)+"    "+st.transformWorldImageZ(v.z));
		}

	
	
	/**
	 * Copy metadata (resolution) from another stack
	 */
	public void getMetaFrom(EvStack o)
		{
		resX=o.resX;
		resY=o.resY;
		resZ=o.resZ;
		cs=o.cs.clone();
		}
	
	
	public void allocate(int w, int h, int d, EvPixelsType type)
		{
		allocate(w, h, d, type, null);
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
			cs=ref.cs.clone();
			/*dispX=ref.dispX;
			dispY=ref.dispY;
			dispZ=ref.dispZ;*/
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
		
		//double dispZ=cs.getTransformToSystem().m23;
		
		//This calculation is not really true yet. Use later
		int closestZ=(int)EvMathUtil.clamp(Math.round((worldZ-oldGetDispZ())/resZ),0,getDepth()-1);
		
		if(closestZ<0)
			{
			System.out.println("Strange closestz "+closestZ+"  oldgetdispz "+oldGetDispZ()+" resz "+resZ+"   getDepth "+getDepth());
			System.out.println(this);
			System.out.println(loaders);
			}
		
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
		
		
//		System.out.println("loaders "+loaders);
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
		if(loaders.isEmpty())
			throw new RuntimeException("Stack is empty");
		else
			{
			EvImage evim=loaders.get(0);  //null for some recordings!!!
			if(evim==null)
				throw new RuntimeException("First image is null, got #images: "+loaders.size());
			return evim;
			}
//		return loaders.get(loaders.firstKey());
		}
	
	
	/**
	 * TODO require and enforce that somehow all layers have the same width and height
	 * Get width in number of pixels
	 */
	public int getWidth()
		{
		EvImage evim=getFirstImage();
		EvPixels p=evim.getPixels(new ProgressHandle());
		return p.getWidth();  //TODO move width and height into stack instead. then this handle will not be needed
		}
	
	/**
	 * Get height in number of pixels
	 */
	public int getHeight()
		{
		EvImage evim=getFirstImage();
		EvPixels p=evim.getPixels(new ProgressHandle());
		return p.getHeight();  //TODO move width and height into stack instead. then this handle will not be needed
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
	public EvPixels[] getPixels(ProgressHandle progh)
		{
		EvPixels[] arr=new EvPixels[loaders.size()];
		int i=0;
		for(EvImage evim:loaders)
			arr[i++]=evim.getPixels(progh);
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
	public int[][] getReadOnlyArraysInt(ProgressHandle progh)
		{
		EvPixels[] parr2=getPixels(progh);
		int[][] parr=new int[parr2.length][];
		for(int i=0;i<parr2.length;i++)
			parr[i]=parr2[i].getReadOnly(EvPixelsType.INT).getArrayInt();
		return parr;
		}
	

	/**
	 * Return the pixel arrays for every plane. This is the original pixel data; meant for the
	 * time when you write it
	 */
	public int[][] getOrigArraysInt(ProgressHandle progh)
		{
		EvPixels[] parr2=getPixels(progh);
		int[][] parr=new int[parr2.length][];
		for(int i=0;i<parr2.length;i++)
			parr[i]=parr2[i].getArrayInt();
		return parr;
		}

	/**
	 * Return the pixel arrays for every plane. This is the original pixel data; meant for the
	 * time when you write it
	 */
	public double[][] getOrigArraysDouble(ProgressHandle progh)
		{
		EvPixels[] parr2=getPixels(progh);
		double[][] parr=new double[parr2.length][];
		for(int i=0;i<parr2.length;i++)
			parr[i]=parr2[i].getArrayDouble();
		return parr;
		}

	
	/**
	 * Return the pixel arrays for every plane. Will do a read-only conversion automatically
	 */
	public double[][] getReadOnlyArraysDouble(ProgressHandle progh)
		{
		EvPixels[] parr2=getPixels(progh);
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
	
	//Have to improve this one
	/*
	public void forceEvaluation(ProgressHandle progh)
		{
		for(EvImage im:getImages())
			im.getPixels(progh);
		}
	*/
	
	
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
	

	@Override
	public String toString()
		{
		return "stack "+super.toString()+"  "+loaders;
		}
	
	/**
	 * Check if any image is dirty
	 */
	public boolean isDirty()
		{
		for(EvImage evim:loaders)
			if(evim.isDirty)
				return true;
		return false;
		}

	public EvPixelsType getPixelFormat()
		{
		EvPixels p=getFirstImage().getPixels();
		return p.getType();
		}

	
	}
