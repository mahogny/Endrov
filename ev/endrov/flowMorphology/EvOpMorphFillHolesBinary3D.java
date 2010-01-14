/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;

import java.util.LinkedList;

import endrov.flow.EvOpStack1;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.Vector3i;

/**
 * Fill holes in binary image. The algorithm is optimized for images with small holes. O(w h d)
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphFillHolesBinary3D extends EvOpStack1
	{
	@Override
	public EvStack exec1(EvStack... p)
		{
		return apply(p[0]);
		}

	public static EvStack apply(EvStack stack)
		{
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();

		EvStack markstack=new EvStack();
		markstack.getMetaFrom(stack);
		markstack.allocate(w, h, d, EvPixelsType.INT, stack);
		
		int[][] inarr=stack.getReadOnlyArraysInt();
		int[][] markarr=markstack.getReadOnlyArraysInt();
		
		//Move along border and mark all open pixels as starting point
		LinkedList<Vector3i> q=new LinkedList<Vector3i>();
		for(int ax=0;ax<w;ax++)
			for(int ay=0;ay<h;ay++)
				{
				int az=0;
				q.add(new Vector3i(ax,ay,az));
				az=d-1;
				q.add(new Vector3i(ax,ay,az));
				}

		for(int ax=0;ax<w;ax++)
			for(int az=0;az<d;az++)
				{
				int ay=0;
				q.add(new Vector3i(ax,ay,az));
				ay=h-1;
				q.add(new Vector3i(ax,ay,az));
				}

		for(int ay=0;ay<h;ay++)
			for(int az=0;az<d;az++)
				{
				int ax=0;
				q.add(new Vector3i(ax,ay,az));
				ax=w-1;
				q.add(new Vector3i(ax,ay,az));
				}
		
		while(!q.isEmpty())
			{
			Vector3i v=q.poll();
			int x=v.x;
			int y=v.y;
			int z=v.z;
			int index=y*w+x;

			//Check that this pixel has not been evaluated before
			if(markarr[z][index]==0)
				{
				int thisval=inarr[z][index];
				
				//Test if this pixel should be included
				if(thisval==0)
					{
					markarr[z][index]=1;
					
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
		
		//Invert the matrix to get the filled region
		for(int i=0;i<markarr.length;i++)
			{
			int[] arr=markarr[i];
			for(int j=0;j<arr.length;j++)
				arr[j]=1-arr[j];
			}
		
		return markstack;
		}

	
	}
