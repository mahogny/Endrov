/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFlooding;

import java.util.*;

import endrov.flow.EvOpStack1;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.Vector3i;

/**
 * Segment image through watershedding. Output image will have pixels with value corresponding to the group they belong to.
 * Watershed grows in 6 directions. Goes from large values to smaller values
 * <br/>
 * Worst case up to O(whd log(whd)). More realistically closer to O(whd).
 * 
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpWatershed extends EvOpStack1
	{
	/*
	private Collection<Vector3i> seeds;

	public EvOpWatershed(Collection<Vector3i> seeds)
		{
		this.seeds = seeds;
		}
*/

	@Override
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return watershed(ph, p[0], p[1]);
		}
	
	/**
	 * Need seed points. Specify or use local maximas
	 * 
	 */
	
	/**
	 * TODO
	 * search method for local minima that deals with area minimas ie a semi-floodfill to look for smaller values 
	 */
	
	

	/**
	 * One point on the queue
	 */
	private static class PriPixel implements Comparable<PriPixel>
		{
		int x,y,z;
		
		int group;
		
		/**
		 * Intensity of pixel. Could be looked up using position but keeping a copy here increases memory locality
		 */
		int intensity;
		
		/**
		 * By introducing generation collisions on flat areas are handled better.
		 * This turns watershedding into an extremely primitive levelset method. 
		 * 
		 * TODO should generation be reset whenever a new intensity level is hit?
		 */
		int generation; 
		
		
		public PriPixel(int x, int y, int z, int group, int intensity, int generation)
			{
			//System.out.println("intensity "+intensity+"\t"+group+"\t"+generation);
			
			this.x = x;
			this.y = y;
			this.z = z;
			this.group=group;
			this.intensity = intensity;
			this.generation = generation;
			}

		
		public int compareTo(PriPixel b)
			{
			if(intensity>b.intensity)
				return -1;
			else if(intensity<b.intensity)
				return 1;
			
			return 0;
			/*
			else if(generation<b.generation)
				return -1;
			else if(generation>b.generation)
				return 1;
			else 
				return 0;*/
			}
		
		
		
		
		}
	
	//TODO seeds should maybe be groups?
	
	
	public static List<Vector3i> getSeedPoints(ProgressHandle progh, EvStack seedStack)
		{
		//Collect seed points
		LinkedList<Vector3i> seeds=new LinkedList<Vector3i>();
		int w=seedStack.getWidth();
		int h=seedStack.getHeight();
		for(int az=0;az<seedStack.getDepth();az++)
			{
			int[] arr=seedStack.getInt(az).getPixels(progh).convertToInt(true).getArrayInt();
			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w;ax++)
					if(arr[ay*w+ax]!=0)
						seeds.add(new Vector3i(ax, ay, az));
			}
		
		return seeds;
		}
	
	public static EvStack watershed(ProgressHandle progh, EvStack stack, EvStack seedStack)
		{
		return watershed(progh, stack, getSeedPoints(progh, seedStack));
		}
	
	public static EvStack watershed(ProgressHandle progh, EvStack stack, Collection<Vector3i> seeds)
		{
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();
		
		EvStack stackOut=new EvStack();
		stackOut.getMetaFrom(stack);
		stackOut.allocate(w, h, d, EvPixelsType.INT, stack);
		
		int[][] inarr=stack.getReadOnlyArraysInt(progh);
		int[][] outarr=stackOut.getOrigArraysInt(progh);
		
		//Start queue with seed pixels
		PriorityQueue<PriPixel> q=new PriorityQueue<PriPixel>();
		int curGroup=1;
		for(Vector3i s:seeds)
			q.add(new PriPixel(s.x,s.y,s.z,
					curGroup++, 
					inarr[s.z][s.y*w+s.x],
					0));
		
		//Go through all pixels
		while(!q.isEmpty())
			{
			//Take the next pixel off queue
			PriPixel p=q.poll();
			
			//Make sure the compiler can assume values to be static
			int x=p.x;
			int y=p.y;
			int z=p.z;
			int thisi=p.y*w+p.x;
			int group=p.group;
			
			//Check if this pixel should be marked: if neighbours are unmarked or belong to this group.
			//This will cause basins to have the original value
			if(outarr[z][thisi]==0 &&
					checkNeighFreeOrThisGroup(outarr, w, h, d, x-1, y, z, group) &&
					checkNeighFreeOrThisGroup(outarr, w, h, d, x+1, y, z, group) &&
					checkNeighFreeOrThisGroup(outarr, w, h, d, x, y-1, z, group) &&
					checkNeighFreeOrThisGroup(outarr, w, h, d, x, y+1, z, group) &&
					checkNeighFreeOrThisGroup(outarr, w, h, d, x, y, z-1, group) &&
					checkNeighFreeOrThisGroup(outarr, w, h, d, x, y, z+1, group))
				{
				//Mark this pixel
				outarr[z][thisi]=group;

				
				
				//Put neighbours on the queue, make sure they are within boundary
				int nextgen=p.generation+1;
				if(x>0)
					q.add(new PriPixel(x-1,y,z, group, inarr[z  ][(y  )*w+(x-1)],nextgen));
				if(x<w-1)
					q.add(new PriPixel(x+1,y,z, group, inarr[z  ][(y  )*w+(x+1)],nextgen));
				if(y>0)
					q.add(new PriPixel(x,y-1,z, group, inarr[z  ][(y-1)*w+(x  )],nextgen));
				if(y<h-1)
					q.add(new PriPixel(x,y+1,z, group, inarr[z  ][(y+1)*w+(x  )],nextgen));
				if(z>0)
					q.add(new PriPixel(x,y,z-1, group, inarr[z-1][(y  )*w+(x  )],nextgen));
				if(z<d-1)
					q.add(new PriPixel(x,y,z+1, group, inarr[z+1][(y  )*w+(x  )],nextgen));
					
				}
			}
		
		return stackOut;
		}
	
	/**
	 * Check if a neighbour pixel belongs to the same group. 
	 * TODO test could be made faster by factoring out cases of borders
	 * TODO multiplication could be removed by plugging thisi and doing addition at the caller
	 */
	private static boolean checkNeighFreeOrThisGroup(int[][] outarr, int w, int h, int d, int x, int y, int z, int group)
		{
		if(x>=0 && y>=0 && z>=0 && 
				x<w && y<h && z<d)
			{
			int i=y*w+x;
			int p=outarr[z][i];
			return p==0 || p==group;
			}
		else
			return true;
		}

	}
