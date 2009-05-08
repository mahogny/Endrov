package endrov.unsortedImageFilters;

import java.util.LinkedList;
import java.util.PriorityQueue;

import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.Vector3i;

public class Watershed
	{
	/**
	 * Need seed points. Specify or use local maximas
	 * 
	 */

	public static class PriPixel implements Comparable<PriPixel>
		{
		
		
		public PriPixel(int x, int y, int z, int intensity, int generation)
			{
			this.x = x;
			this.y = y;
			this.z = z;
			this.intensity = intensity;
			this.generation = generation;
			}

		int x,y,z;
		
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
		
		public int compareTo(PriPixel b)
			{
			if(intensity<b.intensity)
				return -1;
			else if(intensity>b.intensity)
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
	
	public static EvPixels watershed(EvStack stack, LinkedList<Vector3i> seeds)
		{
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();
		int[][] parr=stack.getArraysInt();
		
		EvStack stackOut=new EvStack();
		stackOut.getMetaFrom(stack);
		
		
		PriorityQueue<PriPixel> q=new PriorityQueue<PriPixel>();
		for(Vector3i s:seeds)
			{
			PriPixel seedp=new PriPixel(s.x,s.y,s.z,parr[s.z][s.y*w+s.x],0);
			
			
			
			}
		
		
		}
	
	
	}
