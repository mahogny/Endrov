package endrov.unsortedImageFilters;

import java.util.*;

import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.EvMathUtil;
import endrov.util.Vector3i;

/**
 * Select area by flooding it
 * 
 * @author Johan Henriksson
 *
 */
public class FloodFill
	{
	
	
	
	/**
	 * Floodfill area with the same color
	 */
	public static EvStack sameColor(EvStack stack, int startx, int starty, int startz)
		{
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();

		EvStack markstack=new EvStack();
		markstack.getMetaFrom(stack);
		markstack.allocate(w, h, EvPixels.TYPE_INT, stack);
		
		int[][] inarr=stack.getArraysInt();
		int[][] outarr=markstack.getArraysInt();
		
		LinkedList<Vector3i> q=new LinkedList<Vector3i>();
		q.add(new Vector3i(startx,starty,startz));
		int startval=inarr[startz][starty*w+startx];
		
		
		while(!q.isEmpty())
			{
			Vector3i v=q.poll();
			int x=v.x;
			int y=v.y;
			int z=v.z;
			int index=y*w+x;

			//Check that this pixel has not been evaluated before
			if(outarr[z][index]==0)
				{
				int thisval=inarr[z][index];
				
				//Test if this pixel should be included
				if(thisval==startval)
					{
					outarr[z][index]=1;
					
					//Evaluate neighbours
					if(x>0)
						q.add(new Vector3i(x-1,y,z));
					if(x<w-1)
						q.add(new Vector3i(x+1,y,z));
					if(y>0)
						q.add(new Vector3i(x,y-1,z));
					if(y<h-1)
						q.add(new Vector3i(x,y+1,z));
					if(z>0)
						q.add(new Vector3i(x,y,z-1));
					if(z<d-1)
						q.add(new Vector3i(x,y,z+1));
					}
				}
			
			}
		return markstack;
		}
	
	
	/**
	 * Floodfill area. Keep adding pixels as long as neighbour is within [mu-f sigma, mu+f sigma]
	 */
	public static EvStack inSigma(EvStack stack, LinkedList<Vector3i> startv, double f)
		{
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();

		EvStack markstack=new EvStack();
		markstack.getMetaFrom(stack);
		markstack.allocate(w, h, EvPixels.TYPE_INT, stack);
		
		int[][] inarr=stack.getArraysInt();
		int[][] outarr=markstack.getArraysInt();

		double sum=0;
		double sum2=0;
		int count=0;
		
		LinkedList<Vector3i> q=new LinkedList<Vector3i>();
		for(Vector3i v:startv)
			{
			//Mark this position
			int x=v.x;
			int y=v.y;
			int z=v.z;
			int index=y*w+x;
			int thisval=inarr[z][index];
			sum+=thisval;
			sum2+=thisval*thisval;
			count++;
			outarr[z][index]=1;
			
			//Evaluate neighbours
			if(x>0)
				q.add(new Vector3i(x-1,y,z));
			if(x<w-1)
				q.add(new Vector3i(x+1,y,z));
			if(y>0)
				q.add(new Vector3i(x,y-1,z));
			if(y<h-1)
				q.add(new Vector3i(x,y+1,z));
			if(z>0)
				q.add(new Vector3i(x,y,z-1));
			if(z<d-1)
				q.add(new Vector3i(x,y,z+1));
			}
		
		
		while(!q.isEmpty())
			{
			Vector3i v=q.poll();
			int x=v.x;
			int y=v.y;
			int z=v.z;
			int index=y*w+x;

			//Check that this pixel has not been evaluated before
			if(outarr[z][index]==0)
				{
				//Calculate if this value is in range. Use a trick to avoid a Sqrt
				int thisval=inarr[z][index];
				double diff=thisval-sum/count;
				diff*=diff;
				double variance=EvMathUtil.unbiasedVariance(sum, sum2, count);
				
				//Test if this pixel should be included
				if(diff<variance*f)
					{
					//Include pixel
					sum+=thisval;
					sum2+=thisval*thisval;
					count++;
					outarr[z][index]=1;

					//Evaluate neighbours
					if(x>0)
						q.add(new Vector3i(x-1,y,z));
					if(x<w-1)
						q.add(new Vector3i(x+1,y,z));
					if(y>0)
						q.add(new Vector3i(x,y-1,z));
					if(y<h-1)
						q.add(new Vector3i(x,y+1,z));
					if(z>0)
						q.add(new Vector3i(x,y,z-1));
					if(z<d-1)
						q.add(new Vector3i(x,y,z+1));
					}
				}
			
			}
		return markstack;
		}
	
	
	}
