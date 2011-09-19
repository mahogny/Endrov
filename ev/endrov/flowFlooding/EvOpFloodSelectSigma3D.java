/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFlooding;

import java.util.Collection;
import java.util.LinkedList;

import endrov.flow.EvOpStack1;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.EvMathUtil;
import endrov.util.ProgressHandle;
import endrov.util.Vector3i;

/**
 * Floodfill area. Keep adding pixels as long as neighbour is within [mu-f sigma, mu+f sigma]
 * 
 * @author Johan Henriksson
 */
public class EvOpFloodSelectSigma3D extends EvOpStack1
	{
	private final Collection<Vector3i> startv;
	private final double f;
	
	
	public EvOpFloodSelectSigma3D(Collection<Vector3i> startv, double f)
		{
		this.startv = startv;
		this.f = f;
		}

	@Override
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return fill(ph, p[0], startv, f);
		}
	
	public static EvStack fill(ProgressHandle progh, EvStack stack, Collection<Vector3i> startv, double f)
		{
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();

		EvStack markstack=new EvStack();
		markstack.getMetaFrom(stack);
		markstack.allocate(w, h, d, EvPixelsType.INT, stack);
		
		double[][] inarr=stack.getReadOnlyArraysDouble(progh);
		int[][] outarr=markstack.getReadOnlyArraysInt(progh);

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
			double thisval=inarr[z][index];
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
		
		System.out.println("Flooding");
		while(!q.isEmpty())
			{
			Vector3i v=q.poll();
			//System.out.println("# in q "+q.size());
			int x=v.x;
			int y=v.y;
			int z=v.z;
			int index=y*w+x;

			//Check that this pixel has not been evaluated before
			if(outarr[z][index]==0)
				{
				//Calculate if this value is in range. Use a trick to avoid a Sqrt
				double thisval=inarr[z][index];
				double diff=thisval-sum/count;
				diff*=diff;
				double variance=EvMathUtil.unbiasedVariance(sum, sum2, count);
				//System.out.println(diff+"   vs  "+variance+"  count= "+count);
				
				//Test if this pixel should be included
				if(diff<variance*f*f || count<=1)
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
		System.out.println("end Flooding "+count);
		return markstack;
		}





	
	
	}
