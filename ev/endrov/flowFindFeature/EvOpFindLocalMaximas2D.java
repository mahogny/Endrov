/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFindFeature;

import java.util.*;

import endrov.flow.EvOpSlice1;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;
import endrov.util.math.Vector3i;


/**
 * Find local extremes. Can handle flat areas - a region of the same value is the extreme, as opposed to a single pixel.
 * <br/>
 * O(w h d)
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpFindLocalMaximas2D extends EvOpSlice1
	{

	//Could also generate this as a new channel. less efficient but good to see what is happening
	//or - some simple way of superimposing the pixels on a channel

	private boolean alsoDiagonals;
	
	public EvOpFindLocalMaximas2D(boolean alsoDiagonals)
		{
		this.alsoDiagonals=alsoDiagonals;
		}
	
	/**
	 * Find local maximas
	 */
	public static List<Vector3i> findMaximas(ProgressHandle ph, EvPixels p, int z, boolean alsoDiagonals)
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
		//TODO faster with an array?
		
		int w=p.getWidth();
		int h=p.getHeight();
		
		
		double[] inarr=p.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
		for(int y=1;y<h-1;y++)
			for(int x=1;x<w-1;x++)
				{
				Vector3i here=new Vector3i(x,y,z);
				if(!visited.contains(here))
					{
					double thisValue=inarr[y*w+x];
					
					LinkedList<Vector3i> eqVal=new LinkedList<Vector3i>();
					
					/*
					if(isHigher(eqVal, inarr, thisValue, w, thisValue, d, x-1, y, z) ||
							isHigher(eqVal, inarr, thisValue, w, thisValue, d, x+1, y, z) ||
							isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y-1, z) ||
							isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y+1, z) ||
							isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y, z-1) ||
							isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y, z+1))*/
					if(isAnyHigher(alsoDiagonals, eqVal, inarr, thisValue, w, h, x, y, z))
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
							visited.add(v);
							
							/*
							//Increase locality
							int vx=v.x;
							int vy=v.y;
							int vz=v.z;*/
							
							//Find a pixel which is higher. 
							//Cannot simply stop here; if this area is not completely marked as
							//visited then problems can arise later
							if(isAnyHigher(alsoDiagonals, eqVal, inarr, thisValue, w, h, v.x, v.y, v.z))
								foundHigher=true;
							/*
							if(
									(vx>0   && isHigher(eqVal, inarr, thisValue, w, h, vx-1, vy, vz)) ||
									(vx<w-1 && isHigher(eqVal, inarr, thisValue, w, h, vx+1, vy, vz)) ||
									(vy>0   && isHigher(eqVal, inarr, thisValue, w, h, vx, vy-1, vz)) ||
									(vy<h-1 && isHigher(eqVal, inarr, thisValue, w, h, vx, vy+1, vz)))
								foundHigher=true;
							
							System.out.println("diagonals "+alsoDiagonals);
							if(alsoDiagonals)
								if(
										(vx>0   && vy>0   && isHigher(eqVal, inarr, thisValue, w, h, vx-1, vy-1, vz)) ||
										(vx<w-1 && vy<h-1 && isHigher(eqVal, inarr, thisValue, w, h, vx+1, vy+1, vz)) ||
										(vy>0   && vy<h-1 && isHigher(eqVal, inarr, thisValue, w, h, vx-1, vy+1, vz)) ||
										(vy<h-1 && vy>0   && isHigher(eqVal, inarr, thisValue, w, h, vx+1, vy-1, vz)))
									foundHigher=true;
									*/
							}
						
						//If no higher value was found in the surrounding of this set, then add representative pixel
						if(!foundHigher)
							list.add(here);
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
	 * Is any higher? Add to queue if equal
	 */
	private static boolean isAnyHigher(boolean alsoDiagonals, LinkedList<Vector3i> eqVal, double[] inarr, double thisValue, int w, int h,int x, int y, int z)
		{
		boolean foundHigher=false;
		if(
				(x>0   && isHigher(eqVal, inarr, thisValue, w, h, x-1, y, z)) ||
				(x<w-1 && isHigher(eqVal, inarr, thisValue, w, h, x+1, y, z)) ||
				(y>0   && isHigher(eqVal, inarr, thisValue, w, h, x, y-1, z)) ||
				(y<h-1 && isHigher(eqVal, inarr, thisValue, w, h, x, y+1, z)))
			foundHigher=true; //Important to not just return. Side-effects in if
		
		//System.out.println("diagonals "+alsoDiagonals);
		if(alsoDiagonals)
			if(
					(x>0   && y>0   && isHigher(eqVal, inarr, thisValue, w, h, x-1, y-1, z)) ||
					(x<w-1 && y<h-1 && isHigher(eqVal, inarr, thisValue, w, h, x+1, y+1, z)) ||
					(y>0   && y<h-1 && isHigher(eqVal, inarr, thisValue, w, h, x-1, y+1, z)) ||
					(y<h-1 && y>0   && isHigher(eqVal, inarr, thisValue, w, h, x+1, y-1, z)))
				foundHigher=true;
		
		return foundHigher;
		
		
		/*
		return
				isHigher(eqVal, inarr, thisValue, w, h, x-1, y, z) ||
				isHigher(eqVal, inarr, thisValue, w, h, x+1, y, z) ||
				isHigher(eqVal, inarr, thisValue, w, h, x, y-1, z) ||
				isHigher(eqVal, inarr, thisValue, w, h, x, y+1, z);
		
		
		*/
		}
	
	/**
	 * Helper function: check if a pixel has a higher value. add to queue if value is equal
	 */
	private static boolean isHigher(LinkedList<Vector3i> eqVal, double[] inarr, double thisValue, int w, int h, int x, int y, int z)
		{
		double newValue=inarr[y*w+x];
		if(newValue>thisValue)
			return true;
		else if(newValue==thisValue)
			eqVal.add(new Vector3i(x,y,z));
		return false;
		}


	
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return apply(ph, p[0]);
		}
	
	public EvPixels apply(ProgressHandle ph, EvPixels p)
		{
		EvPixels pout=new EvPixels(EvPixelsType.INT,p.getWidth(),p.getHeight());
		int[] arr=pout.getArrayInt();
		int w=p.getWidth();
		
		for(Vector3i v:findMaximas(ph, p, 0, alsoDiagonals))
			arr[v.y*w+v.x]=1;
		
		return pout;
		}
	
	
	
	
	}
