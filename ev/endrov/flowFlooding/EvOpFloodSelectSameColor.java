package endrov.flowFlooding;

import java.util.*;

import endrov.flow.EvOpStack1;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.Vector3i;

/**
 * Floodfill area with the same color
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpFloodSelectSameColor extends EvOpStack1
	{
	private final Vector3i startpos;
	
	public EvOpFloodSelectSameColor(Vector3i startpos)
		{
		this.startpos = startpos;
		}


	@Override
	public EvStack exec1(EvStack... p)
		{
		return fill(p[0], startpos);
		}
	
	
	public static EvStack fill(EvStack stack, Vector3i startpos)
		{
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();

		EvStack markstack=new EvStack();
		markstack.getMetaFrom(stack);
		markstack.allocate(w, h, d, EvPixelsType.INT, stack);
		
		int[][] inarr=stack.getArraysInt();
		int[][] outarr=markstack.getArraysInt();
		
		LinkedList<Vector3i> q=new LinkedList<Vector3i>();
		q.add(new Vector3i(startpos.x,startpos.y,startpos.z));
		int startval=inarr[startpos.z][startpos.y*w+startpos.x];
		
		
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

	}
