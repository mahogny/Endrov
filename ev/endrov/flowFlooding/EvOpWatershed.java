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

	@Override
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		
		EvStack result=watershed(ph, p[0], p[1]);
		
		return result;
		}
	
	/**
	 * Need seed points. Specify or use local maximas
	 * 
	 */
	
	/**
	 * TODO
	 * search method for local minima that deals with area minimas ie a semi-floodfill to look for smaller values 
	 */
	
	
	private static class SpecialQueue
		{
		
//		int curLargestValue=Integer.MAX_VALUE;
		private LinkedList<Integer> intensityList=new LinkedList<Integer>();
		
		
		private HashMap<Integer,LinkedList<PriPixel>> queues=new HashMap<Integer, LinkedList<PriPixel>>();
		
		
		public PriPixel getNext()
			{
			for(;;)
				{
				if(intensityList.isEmpty())
					return null;
				
				int curIntensity=intensityList.getFirst();
//				curLargestValue=curIntensity;
				LinkedList<PriPixel> curList=queues.get(curIntensity);
				if(curList.isEmpty())
					intensityList.removeFirst();
				else
					return curList.removeFirst();
				}
			}
		
		public void addAllIntensity(int[][] intensities)
			{
			//Find all intensities
			HashSet<Integer> fi=new HashSet<Integer>();
			for(int[] i:intensities)
				for(int j:i)
					fi.add(j);
			
			TreeSet<Integer> allValues=new TreeSet<Integer>();
			allValues.addAll(fi);
			allValues.add(Integer.MAX_VALUE);
			
			//Create queues
			for(int i:allValues)
				queues.put(i, new LinkedList<PriPixel>());
			
			//A list of intensities to consider. Finding the next intensity is always O(1)
			intensityList.addAll(allValues.descendingSet());
			}
		
		public void add(int x, int y, int z,
				int group, int intensity) 
			{
			//If finding a new peak, then just grow according to distance. To not backtrack, add them to the currently considered intensity
			int curIntensity=intensityList.getFirst();
			if(intensity>curIntensity)
				intensity=curIntensity;
			LinkedList<PriPixel> list=queues.get(intensity);
			
			if(list==null)
				System.out.println("Missing intensity "+intensity);
			
			list.addLast(new PriPixel(x,y,z,group));
			}
		
		
		}
	

	/**
	 * One point on the queue
	 */
	private static class PriPixel
		{
		int x,y,z;
		
		int group;
		
		public PriPixel(int x, int y, int z, int group)
			{
			this.x = x;
			this.y = y;
			this.z = z;
			this.group=group;
			}
		
		}
	
	public static class IDPoint
		{
		int x,y,z;
		int id;
		
		public IDPoint(Vector3i p, int id)
			{
			this.x=p.x;
			this.y=p.y;
			this.z=p.z;
			this.id=id;
			}
		
		public IDPoint(int x, int y, int z, int id)
			{
			this.x=x;
			this.y=y;
			this.z=z;
			this.id=id;
			}
		}
	
	
	public static List<IDPoint> makeUniquePoints(Collection<Vector3i> seeds)
		{
		LinkedList<IDPoint> n=new LinkedList<IDPoint>();
		int id=1;
		for(Vector3i p:seeds)
			n.add(new IDPoint(p, id++));
		return n;
		}

	/**
	 * Create seed points. The IDs will be taken from the image, thus the color of each blob should be unique 
	 */
	public static List<IDPoint> getSeedPoints(ProgressHandle progh, EvStack seedStack)
		{
		//Collect seed points
		LinkedList<IDPoint> seeds=new LinkedList<IDPoint>();
		int w=seedStack.getWidth();
		int h=seedStack.getHeight();
		for(int az=0;az<seedStack.getDepth();az++)
			{
			int[] arr=seedStack.getInt(az).getPixels(progh).convertToInt(true).getArrayInt();
			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w;ax++)
					{
					int thisID=arr[ay*w+ax];
					if(thisID!=0)
						seeds.add(new IDPoint(ax,ay,az, thisID));
					}
			}
		
		return seeds;
		}
	
	public static EvStack watershed(ProgressHandle progh, EvStack stack, EvStack seedStack)
		{
		return watershed(progh, stack, getSeedPoints(progh, seedStack));
		}
	
	public static EvStack watershed(ProgressHandle progh, EvStack stack, Collection<IDPoint> seeds)
		{
		
		
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();
		
		EvStack stackOut=new EvStack();
		stackOut.getMetaFrom(stack);
		stackOut.allocate(w, h, d, EvPixelsType.INT, stack);
		
		int[][] inarr=stack.getReadOnlyArraysInt(progh);
		int[][] outarr=stackOut.getOrigArraysInt(progh);
		
		long startTime=System.currentTimeMillis();

		SpecialQueue queue=new SpecialQueue();
		queue.addAllIntensity(inarr);

		//Start queue with seed pixels
		//PriorityQueue<PriPixel> q=new PriorityQueue<PriPixel>();
		//int curGroup=1;
		for(IDPoint s:seeds)
			queue.add(s.x, s.y, s.z, s.id, inarr[s.z][s.y*w+s.x]);
		
		
		//Go through all pixels
		PriPixel p;
		while((p=queue.getNext())!=null)
			{
			//Take the next pixel off queue
//			PriPixel p=q.poll();
			
			//Make sure the compiler can assume the values to be static
			int x=p.x;
			int y=p.y;
			int z=p.z;
			int thisi=p.y*w+p.x;
			int group=p.group;
			
			//Check if this pixel should be marked: if neighbours are unmarked or belong to this group.
			//This will cause basins to have the original value
			if(outarr[z][thisi]==0 /* &&
					checkNeighFreeOrThisGroup(outarr, w, h, d, x-1, y, z, group) &&
					checkNeighFreeOrThisGroup(outarr, w, h, d, x+1, y, z, group) &&
					checkNeighFreeOrThisGroup(outarr, w, h, d, x, y-1, z, group) &&    ////orig. 53s. without these, 51s. can remove but need a better viewing mode for ID-data
					checkNeighFreeOrThisGroup(outarr, w, h, d, x, y+1, z, group) &&
					checkNeighFreeOrThisGroup(outarr, w, h, d, x, y, z-1, group) &&
					checkNeighFreeOrThisGroup(outarr, w, h, d, x, y, z+1, group)*/)
				{
				//Mark this pixel
				outarr[z][thisi]=group;

				
				
				//Put neighbours in the queue, make sure they are within boundary
				//int nextgen=p.generation+1;
				if(x>0)
					queue.add(x-1, y, z, group, inarr[z  ][(y  )*w+(x-1)]);
				if(x<w-1)
					queue.add(x+1, y, z, group, inarr[z  ][(y  )*w+(x+1)]);
				if(y>0)
					queue.add(x, y-1, z, group, inarr[z  ][(y-1)*w+(x  )]);
				if(y<h-1)
					queue.add(x, y+1, z, group, inarr[z  ][(y+1)*w+(x  )]);
				if(z>0)
					queue.add(x, y, z-1, group, inarr[z-1][(y)*w+(x  )]);
				if(z<d-1)
					queue.add(x, y, z+1, group, inarr[z+1][(y)*w+(x  )]);
					
				}
			}
		
		
		long endTime=System.currentTimeMillis();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> watershed time "+(endTime-startTime));
		
		return stackOut;
		}
	
	/**
	 * Check if a neighbour pixel belongs to the same group. 
	 * TODO test could be made faster by factoring out cases of borders
	 * TODO multiplication could be removed by plugging thisi and doing addition at the caller
	 */
	/*
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
		}*/

	}
