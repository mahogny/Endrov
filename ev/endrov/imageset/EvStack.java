package endrov.imageset;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * One stack of images. Corresponds to one frame in one channel.
 * 
 * @author Johan Henriksson
 *
 */
public class EvStack
	{
	private TreeMap<EvDecimal, EvImage> loaders=new TreeMap<EvDecimal, EvImage>();
	
	/**
	 * Resolution [px/um].
	 * Binning not taken into account
	 */
	public double resX, resY;
	
	/**
	 * Binning. 4 would mean the image is 4 times smaller than what it depicts.
	 */
	public double binning;
	
	/**
	 * Displacement in micrometer
	 */
	public double dispX, dispY;

	/**
	 * Return combined resolution and binning. [px/um]
	 */
	public double getResbinX()
		{
		return resX/binning;
		}
	public double getResbinY()
		{
		return resY/binning;
		}

	public double transformImageWorldX(double c){return (c*binning+dispX)/resX;}
	public double transformImageWorldY(double c){return (c*binning+dispY)/resY;}			
	public double transformWorldImageX(double c){return (c*resX-dispX)/binning;}
	public double transformWorldImageY(double c){return (c*resY-dispY)/binning;}
	public double scaleImageWorldX(double c){return c/(resX/binning);}
	public double scaleImageWorldY(double c){return c/(resY/binning);}
	public double scaleWorldImageX(double c){return c*resX/binning;}
	public double scaleWorldImageY(double c){return c*resY/binning;}
	
	
	/*
	public double transformImageWorldZ(double c)
		{
		//TODO
		}
	public double transformWorldImageZ(double c)
		{
		//TODO
		}
	public double scaleWorldImageZ(double c){}
	public double scaleImageWorldZ(double c){}
	*/
	
	public void getMetaFrom(EvStack o)
		{
		resX=o.resX;
		resY=o.resY;
		binning=o.binning;
		dispX=o.dispX;
		dispY=o.dispY;
		}
	
	//TODO lazy generation of the stack
	
	//public EvStack(){}
	
	/*
	//Temp, can be removed later
	public EvStack(TreeMap<EvDecimal, EvImage> l)
		{
		loaders.putAll(l);
		}*/
	
	/**
	 * Get one image plane
	 */
	public EvImage get(EvDecimal z)
		{
		return loaders.get(z);
		}
	
	/**
	 * Set one image plane
	 */
	public void put(EvDecimal z, EvImage im)
		{
		loaders.put(z,im);
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
		}

	
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
	public EvDecimal firstZ()
		{
		return loaders.firstKey();
		}
	
	 

	/**
	 * Last (largest) value of Z, or null
	 */
	public EvDecimal lastZ()
		{
		return loaders.lastKey();
		}

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
	 * TODO require and enforce that somehow all layers have the same width and height
	 * Get width in number of pixels
	 */
	public int getWidth()
		{
		return firstEntry().snd().getPixels().getWidth();
		}
	
	/**
	 * Get height in number of pixels
	 * @return
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
	
	
	
	
	}
