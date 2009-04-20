package endrov.imageset;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.util.EvDecimal;

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
	 * Get the number of image planes
	 */
	public int size()
		{
		return loaders.size();
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
	public EvImage get(EvDecimal frame)
		{
		return loaders.get(frame);
		}
	
	/**
	 * Set one image plane
	 */
	public void put(EvDecimal frame, EvImage im)
		{
		loaders.put(frame,im);
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

	public Map.Entry<EvDecimal, EvImage> firstEntry()
		{
		return loaders.firstEntry();
		}
	}
