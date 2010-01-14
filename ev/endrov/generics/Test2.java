/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.generics;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector3i;
import static endrov.generics.EvGenerics.*;

public class Test2
	{

	
	/**
	 * @GenericTypeA(p)
	 * 
	 * this static call will be replaced with a call to another class with all static functions replaced.
	 * line numbers will be preserved in this transform so bugs are pointed out at the right places. or?
	 * what about the name of the class? can a class loader wrap everything to keep the name?
	 * 
	 * how to transform: find declared variables, replace with new type.
	 * 
	 * 
	 * 
	 * genius idea: import static, if there are interclass inlines. add(foo,bar)
	 * 
	 * search-and-replace, would it be sufficient for a first generation generics?
	 * 
	 */
	public static List<Vector3i> findMaximas(EvPixels p, int z)
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
	
	
	//double[] inarr=p.getReadOnly(EvPixelsType.DOUBLE).getArrayDouble();
	//EvGenericsA[] inarr=p.getArrayDouble();
	EvGenericsA[] inarr=EvGenerics.getPixelsA(p);
	for(int y=1;y<h-1;y++)
		for(int x=1;x<w-1;x++)
			{
			Vector3i here=new Vector3i(x,y,z);
			if(!visited.contains(here))
				{
				//double thisValue=inarr[y*w+x];
				EvGenericsA thisValue=inarr[y*w+x];
				
				LinkedList<Vector3i> eqVal=new LinkedList<Vector3i>();
				
				if(isAnyHigher(eqVal, inarr, thisValue, w, h, x, y, z))
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
						
						//Increase locality
						int vx=v.x;
						int vy=v.y;
						int vz=v.z;
						
						//Find a pixel which is higher. 
						//Cannot simply stop here; if this area is not completely marked as
						//visited then problems can arise later
						if(
								(vx>0   && isHigher(eqVal, inarr, thisValue, w, h, vx-1, vy, vz)) ||
								(vx<w-1 && isHigher(eqVal, inarr, thisValue, w, h, vx+1, vy, vz)) ||
								(vy>0   && isHigher(eqVal, inarr, thisValue, w, h, vx, vy-1, vz)) ||
								(vy<h-1 && isHigher(eqVal, inarr, thisValue, w, h, vx, vy+1, vz)))
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
 * Is any higher?
 */
//private static boolean isAnyHigher(LinkedList<Vector3i> eqVal, double[] inarr, double thisValue, int w, int h,int x, int y, int z)
private static boolean isAnyHigher(LinkedList<Vector3i> eqVal, EvGenericsA[] inarr, EvGenericsA thisValue, int w, int h,int x, int y, int z)
	{
	return isHigher(eqVal, inarr, thisValue, w, h, x-1, y, z) ||
	isHigher(eqVal, inarr, thisValue, w, h, x+1, y, z) ||
	isHigher(eqVal, inarr, thisValue, w, h, x, y-1, z) ||
	isHigher(eqVal, inarr, thisValue, w, h, x, y+1, z);
	}

/**
 * Helper function: check if a pixel has a higher value. add to queue if value is equal
 */
private static boolean isHigher(LinkedList<Vector3i> eqVal, EvGenericsA[] inarr, EvGenericsA thisValue, int w, int h, int x, int y, int z)
	{
	EvGenericsA newValue=inarr[y*w+x];
//	if(newValue.greaterThan(thisValue))
	if(greaterThan(newValue,thisValue))
		return true;
	else if(newValue==thisValue)
		eqVal.add(new Vector3i(x,y,z));
	return false;
	}
	
	}
