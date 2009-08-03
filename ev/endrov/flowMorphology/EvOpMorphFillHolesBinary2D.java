package endrov.flowMorphology;

import java.util.LinkedList;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.util.Vector3i;

/**
 * Fill holes in binary image. The algorithm is optimized for images with small holes. O(w h)
 * <br/>
 * P.Soille - Morphological Image Analysis, Principles and applications. 2nd edition
 * @author Johan Henriksson
 */
public class EvOpMorphFillHolesBinary2D extends EvOpSlice1
	{
	@Override
	public EvPixels exec1(EvPixels... p)
		{
		return apply(p[0]);
		}
	
	
	public static EvPixels apply(EvPixels pixels)
		{
		int w=pixels.getWidth();
		int h=pixels.getHeight();

		EvPixels markstack=new EvPixels(EvPixelsType.INT, w, h); 
		
		int[] inarr=pixels.getArrayInt();
		int[] markarr=markstack.getArrayInt();
		
		//Move along border and mark all open pixels as starting point
		LinkedList<Vector3i> q=new LinkedList<Vector3i>();
		for(int ax=0;ax<w;ax++)
			{
			q.add(new Vector3i(ax,0,0));
			q.add(new Vector3i(ax,h-1,0));
			}
		for(int ay=0;ay<h;ay++)
			{
			q.add(new Vector3i(0,ay,0));
			q.add(new Vector3i(w-1,ay,0));
			}
		
		while(!q.isEmpty())
			{
			Vector3i v=q.poll();
			int x=v.x;
			int y=v.y;
			int index=y*w+x;

			//Check that this pixel has not been evaluated before
			if(markarr[index]==0)
				{
				int thisval=inarr[index];
				
				//Test if this pixel should be included
				if(thisval==0)
					{
					markarr[index]=1;
					
					//Evaluate neighbours
					if(x>0)
						q.add(new Vector3i(x-1,y,0));
					if(x<w-1)
						q.add(new Vector3i(x+1,y,0));
					if(y>0)
						q.add(new Vector3i(x,y-1,0));
					if(y<h-1)
						q.add(new Vector3i(x,y+1,0));
					}
				}
			}
		
		//Invert the matrix to get the filled region
		for(int i=0;i<markarr.length;i++)
			markarr[i]=1-markarr[i];
		
		return markstack;
		}

	
	}