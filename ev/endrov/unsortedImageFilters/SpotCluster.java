package endrov.unsortedImageFilters;

import java.util.*;

import javax.vecmath.Vector3d;

import endrov.imageset.EvPixels;
import endrov.util.Vector3i;

/**
 * Find clusters in groups
 * @author Johan Henriksson
 *
 */
public class SpotCluster
	{

	/**
	 * Calculate center (first moment) from a group of vectors
	 */
	public static Vector3d calculateCenter(Collection<Vector3i> list)
		{
		Vector3d sum=new Vector3d();
		int count=0;
		for(Vector3i v:list)
			{
			sum.x+=v.x;
			sum.y+=v.y;
			sum.z+=v.z;
			count++;
			}
		sum.scale(1.0/count);
		return sum;
		}
	
	//Volume is just size of collection
	
	
	
	/**
	 * Partition all areas in the image
	 * 
	 * go sideways too?
	 */
	public static Partitioning<Vector3i> exec2d(EvPixels in, int z)
		{
		in=in.convertTo(EvPixels.TYPE_INT, true);
		int w=in.getWidth();
		int h=in.getHeight();
		int[] inPixels=in.getArrayInt();
		
		Partitioning<Vector3i> part=new Partitioning<Vector3i>();
		
		//Need only test in one direction since the relation is symmetric.
		for(int ay=0;ay<h-1;ay++)
			for(int ax=0;ax<w-1;ax++)
				{
				if(inPixels[in.getPixelIndex(ax, ay)]!=0)
					{
					Vector3i tv=new Vector3i(ax,ay,z);
					part.createElement(tv);
					if(inPixels[in.getPixelIndex(ax+1, ay)]!=0)
						{
						Vector3i ov=new Vector3i(ax+1,ay,z);
						part.createSpecifyEquivalent(tv, ov);
						}
					if(inPixels[in.getPixelIndex(ax, ay+1)]!=0)
						{
						Vector3i ov=new Vector3i(ax,ay+1,z);
						part.createSpecifyEquivalent(tv, ov);
						}
					}
				}
		
		return part;
		}
	
	
	/*public static Partitioning merge3d()
		{
		
		}*/
	
	public static void main(String[] args)
		{
		EvPixels p=new EvPixels(EvPixels.TYPE_INT,50,50);
		
		for(int x=10;x<15;x++)
			p.getArrayInt()[20*50+x]=1;

		for(int x=10;x<15;x++)
			p.getArrayInt()[22*50+x]=1;

		/*for(int y=20;y<25;y++)
			p.getArrayInt()[y*50+20]=1;*/
		
		System.out.println(exec2d(p, 0).getPartitions());
		
		}
	
	}
