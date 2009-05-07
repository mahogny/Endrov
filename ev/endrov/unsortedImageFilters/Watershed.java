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
		int x,y,z;
		int intensity;
		
		public int compareTo(PriPixel b)
			{
			if(intensity<b.intensity)
				return -1;
			else if(intensity>b.intensity)
				return 1;
			else
				return 0;
			}
		
		
		
		
		}
	
	public static EvPixels watershed(EvStack stack, LinkedList<Vector3i> seeds)
		{
		
		PriorityQueue<PriPixel> q=new PriorityQueue<PriPixel>();
		for(Vector3i s:seeds)
			{
			
			
			
			}
		
		
		}
	
	
	}
