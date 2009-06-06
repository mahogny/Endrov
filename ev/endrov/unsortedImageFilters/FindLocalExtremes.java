package endrov.unsortedImageFilters;

import java.util.*;

import endrov.imageset.EvStack;
import endrov.unsortedImageFilters.imageMath.ImageMulScalarOp;
import endrov.util.Vector3i;


/**
 * Find local extremes. Can handle flat areas - a region of the same value is the extreme, as opposed to a single pixel.
 * O(w h d)
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class FindLocalExtremes
	{

	/**
	 * Find local maximas
	 */
	public static List<Vector3i> findMaximas(EvStack stack)
		{
		LinkedList<Vector3i> list=new LinkedList<Vector3i>();

		/*
		 * Keep list of visited pixels to avoid double reporting. 
		 * This solution is optimized for few areas and/or with the same value: 
		 * by using a hashset, very little memory is used in total. the usage 
		 * per pixel is however very high compared to a full 3d array. an array 
		 * would hence work better for the extreme cases.
		 *
		 * No single-pixel areas will be added to the list.
		 */
		HashSet<Vector3i> visited=new HashSet<Vector3i>();
		
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();
		
		
		int[][] inarr=stack.getArraysInt();
		for(int z=1;z<d-1;z++)
			for(int y=1;y<h-1;y++)
				for(int x=1;x<w-1;x++)
					{
					Vector3i here=new Vector3i(x,y,z);
					if(!visited.contains(here))
						{
						int thisValue=inarr[z][y*w+x];
						
						LinkedList<Vector3i> eqVal=new LinkedList<Vector3i>();
						
						if(isHigher(eqVal, inarr, thisValue, w, thisValue, d, x-1, y, z) ||
								isHigher(eqVal, inarr, thisValue, w, thisValue, d, x+1, y, z) ||
								isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y-1, z) ||
								isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y+1, z) ||
								isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y, z-1) ||
								isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y, z+1))
							{
							//One value is higher for sure. Can ignore this pixel
							}
						else if(!eqVal.isEmpty())
							{
							//No value is higher but some are equal. have to explore neighbourhood.
							//Mark current pixel
							visited.add(here);
							
							boolean foundHigher=false;
							while(!eqVal.isEmpty())
								{
								Vector3i v=eqVal.poll();
								
								//Ignore already tested pixels
								if(visited.contains(v))
									continue;
								
								//Increase locality
								int vx=v.x;
								int vy=v.y;
								int vz=v.z;
								
								//Find a pixel which is higher. 
								//Cannot simply stop here; if this area is not completely marked as
								//visited then problems can arise later
								if(isHigher(eqVal, inarr, thisValue, w, thisValue, d, vx-1, vy, vz) ||
										isHigher(eqVal, inarr, thisValue, w, thisValue, d, vx+1, vy, vz) ||
										isHigher(eqVal, inarr, thisValue, w, thisValue, d, vx, vy-1, vz) ||
										isHigher(eqVal, inarr, thisValue, w, thisValue, d, vx, vy+1, vz) ||
										isHigher(eqVal, inarr, thisValue, w, thisValue, d, vx, vy, vz-1) ||
										isHigher(eqVal, inarr, thisValue, w, thisValue, d, vx, vy, vz+1))
									foundHigher=true;
								}
							if(!foundHigher)
								{
								//Add representative pixel for this area
								list.add(here);
								}
							}
						else
							{
							//This pixel is a local maximum
							list.add(here);
							}
						
						}
					}
		
		return list;
		}
	
	/**
	 * Helper function: check if a pixel has a higher value. add to queue if value is equal
	 */
	private static boolean isHigher(LinkedList<Vector3i> eqVal, int[][] inarr, int thisValue, int w, int h, int d, int x, int y, int z)
		{
		int newValue=inarr[z][y*w+x];
		if(newValue>thisValue)
			return true;
		else if(newValue==thisValue)
			eqVal.add(new Vector3i(x,y,z));
		return false;
		}
	
	/**
	 * Find local minimas. O(w h d). Could be faster by a constant factor
	 */
	public static List<Vector3i> findMinimas(EvStack stack)
		{
//		return findMaximas(NewImageSystem.makeStackOp(new ImageMath.TimesOp(-1)).exec(stack));
		return findMaximas(new ImageMulScalarOp(-1).exec(stack));
		}
	
	
	
	}
