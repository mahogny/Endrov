/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFindFeature;

import java.util.*;

import endrov.flow.EvOpStack1;
import endrov.flowBasic.math.EvOpImageMulScalar;
import endrov.typeImageset.EvPixelsType;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.math.Vector3i;


/**
 * Find local extremes. Can handle flat areas - a region of the same value is the extreme, as opposed to a single pixel.
 * Does not consider border.
 * <br/>
 * O(w h d)
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpFindLocalMaximas3D extends EvOpStack1
	{
	/**
	 * Find local maximas
	 */
	public static List<Vector3i> findMaximas(ProgressHandle progh, EvStack stack)
		{
		LinkedList<Vector3i> list=new LinkedList<Vector3i>();

		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();

		/*
		 * Keep list of visited pixels to avoid double reporting. 
		 * This solution is optimized for few areas and/or with the same value: 
		 * by using a hashset, very little memory is used in total. the usage 
		 * per pixel is however very high compared to a full 3d array. an array 
		 * would hence work better for the extreme cases.
		 *
		 * No single-pixel areas will be added to the list.
		 */
		//HashSet<Vector3i> visited=new HashSet<Vector3i>();
		boolean visited[][][]=new boolean[d][h][w];
		//TODO faster with hashset?
		
		double[][] inarr=stack.getArraysDoubleReadOnly(progh);
		for(int z=1;z<d-1;z++)
			for(int y=1;y<h-1;y++)
				for(int x=1;x<w-1;x++)
					{
					//Dismiss pixels that has been used already
					Vector3i here=new Vector3i(x,y,z);
					if(!visited[z][y][x])
					//if(!visited.contains(here))
						{
						double thisValue=inarr[z][y*w+x];
						
						LinkedList<Vector3i> eqVal=new LinkedList<Vector3i>();
						
						/*
						if(isHigher(eqVal, inarr, thisValue, w, thisValue, d, x-1, y, z) ||
								isHigher(eqVal, inarr, thisValue, w, thisValue, d, x+1, y, z) ||
								isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y-1, z) ||
								isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y+1, z) ||
								isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y, z-1) ||
								isHigher(eqVal, inarr, thisValue, w, thisValue, d, x, y, z+1))*/
						if(isAnyHigher(eqVal, inarr, thisValue, w, h, d, x, y, z))
							{
							//One value is higher for sure. Can ignore this pixel
							}
						else if(!eqVal.isEmpty())
							{
							//No value is higher but some are equal. have to explore neighbourhood.
							//Mark current pixel
							//visited.add(here);
							visited[z][y][x]=true;
							
							boolean foundHigher=false;
							while(!eqVal.isEmpty())
								{
								Vector3i v=eqVal.poll();
								
								try
									{
									//Ignore already tested pixels
									if(visited[v.z][v.y][v.x])
									//if(visited.contains(v))
										continue;
									visited[v.z][v.y][v.x]=true;
									
									//Increase locality
									
									//Find a pixel which is higher. 
									//Cannot simply stop here; if this area is not completely marked as
									//visited then problems can arise later
									
									int vx=v.x;
									int vy=v.y;
									int vz=v.z;
									if(
											(vx>0    && isHigher(eqVal, inarr, thisValue, w, h, d, vx-1, vy, vz)) ||
											(vx<w-1  && isHigher(eqVal, inarr, thisValue, w, h, d, vx+1, vy, vz)) ||
											(vy>0    && isHigher(eqVal, inarr, thisValue, w, h, d, vx, vy-1, vz)) ||
											(vy<h-1  && isHigher(eqVal, inarr, thisValue, w, h, d, vx, vy+1, vz)) ||
											(vz>0    && isHigher(eqVal, inarr, thisValue, w, h, d, vx, vy, vz-1)) ||
											(vz<d-1  && isHigher(eqVal, inarr, thisValue, w, h, d, vx, vy, vz+1)))
									foundHigher=true;
									}
								catch (Exception e)
									{
									e.printStackTrace();
									System.out.println(v);
									System.exit(1);
									}
								
//								if(isAnyHigher(eqVal, inarr, thisValue, w, h, d, v.x, v.y, v.z))
	//								foundHigher=true;
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
	private static boolean isAnyHigher(LinkedList<Vector3i> eqVal, double[][] inarr, double thisValue, int w, int h, int d, int x, int y, int z)
		{
		return 
		isHigher(eqVal, inarr, thisValue, w, h, d, x-1, y, z) ||
		isHigher(eqVal, inarr, thisValue, w, h, d, x+1, y, z) ||
		isHigher(eqVal, inarr, thisValue, w, h, d, x, y-1, z) ||
		isHigher(eqVal, inarr, thisValue, w, h, d, x, y+1, z) ||
		isHigher(eqVal, inarr, thisValue, w, h, d, x, y, z-1) ||
		isHigher(eqVal, inarr, thisValue, w, h, d, x, y, z+1);
		}
	
	/**
	 * Helper function: check if a pixel has a higher value. add to queue if value is equal
	 */
	private static boolean isHigher(LinkedList<Vector3i> eqVal, double[][] inarr, double thisValue, int w, int h, int d, int x, int y, int z)
		{
		double newValue=inarr[z][y*w+x];
		if(newValue>thisValue)
			return true;
		else if(newValue==thisValue)
			eqVal.add(new Vector3i(x,y,z));
		return false;
		}
	
	/**
	 * Find local minimas. O(w h d). Could be faster by a constant factor
	 */
	public static List<Vector3i> findMinimas(ProgressHandle ph, EvStack stack)
		{
		return findMaximas(ph, new EvOpImageMulScalar(-1).exec1(ph,stack));
		}
	
	

	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return apply(ph, p[0]);
		}
	
	public static EvStack apply(ProgressHandle progh, EvStack p)
		{
		EvStack pout=new EvStack();
		pout.allocate(p.getWidth(), p.getHeight(), p.getDepth(), EvPixelsType.INT, p);
		
		int[][] arr=pout.getArraysIntOrig(progh);
		int w=p.getWidth();
		List<Vector3i> vlist=findMaximas(progh, p);
		for(Vector3i v:vlist)
			arr[v.z][v.y*w+v.x]=1;
		
		System.out.println("# max: "+vlist.size());
		
		return pout;
		}
	
	
	
	
	}
