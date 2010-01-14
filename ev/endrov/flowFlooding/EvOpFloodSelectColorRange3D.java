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
import endrov.util.Vector3i;

/**
 * Floodfill area with the same color+-range
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpFloodSelectColorRange3D extends EvOpStack1
	{
	private final Vector3i startpos;
	private final Number rangeMinus, rangePlus;
	
	public EvOpFloodSelectColorRange3D(Vector3i startpos, Number rangeMinus, Number rangePlus)
		{
		this.startpos = startpos;
		this.rangeMinus=rangeMinus;
		this.rangePlus=rangePlus;
		}


	@Override
	public EvStack exec1(EvStack... p)
		{
		return fill(p[0], startpos, rangeMinus, rangePlus);
		}
	
	/**
	 * If range==null then it is set to 0
	 */
	public static EvStack fill(EvStack stack, Vector3i startpos, Number rangeMinus, Number rangePlus)
		{
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();

		if(rangeMinus==null)
			rangeMinus=0;
		if(rangePlus==null)
			rangePlus=0;
		double vRangeMinus=rangeMinus.doubleValue();
		double vRangePlus=rangePlus.doubleValue();
		
		EvStack markstack=new EvStack();
		markstack.getMetaFrom(stack);
		markstack.allocate(w, h, d, EvPixelsType.INT, stack);
		
		double[][] inarr=stack.getReadOnlyArraysDouble();
		int[][] outarr=markstack.getReadOnlyArraysInt();
		
		LinkedList<Vector3i> q=new LinkedList<Vector3i>();
		q.add(new Vector3i(startpos.x,startpos.y,startpos.z));
		double startval=inarr[startpos.z][startpos.y*w+startpos.x];
		
		
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
				double thisval=inarr[z][index];
				
				//Test if this pixel should be included
				if(thisval>=startval-vRangeMinus && thisval<=startval+vRangePlus)
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

	}
