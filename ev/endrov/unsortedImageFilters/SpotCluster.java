package endrov.unsortedImageFilters;

import java.util.*;

import javax.vecmath.Vector3d;

import endrov.imageset.EvPixels;
import endrov.util.Partitioning;
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
				int thisi=in.getPixelIndex(ax, ay);
				if(inPixels[thisi]!=0)
					{
					Vector3i tv=new Vector3i(ax,ay,z);
					part.createElement(tv);
					
					//Try to join it
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
				//Minor bug here: not -1 on both, need to add two strips here.
				}
		
		return part;
		}
	
	
	/*
	public static Partitioning<Vector3i> exec3d(TreeMap<EvDecimal, EvImage> in)
		{
		LinkedList<EvPixels> p=new LinkedList<EvPixels>();
		for(EvImage evim:in.values())
			p.add(evim.getPixels());
		return exec3d(p);
		}*/
	
	
	/**
	 * Partition all areas in the volume. Planes must be same size and aligned.
	 * 
	 * go sideways too?
	 */
	public static Partitioning<Vector3i> exec3d(List<EvPixels> in)
		{
		int w=in.get(0).getWidth();
		int h=in.get(0).getHeight();
		int d=in.size();
		int[][] inPixels=new int[d][];
		for(int az=0;az<in.size();az++)
			inPixels[az]=in.get(az).convertTo(EvPixels.TYPE_INT, true).getArrayInt();
		
		Partitioning<Vector3i> part=new Partitioning<Vector3i>();
		
		//Need only test in one direction since the relation is symmetric.
		for(int az=0;az<d;az++)
			{
			for(int ay=0;ay<h-1;ay++)
				for(int ax=0;ax<w-1;ax++)
					{
					int thisi=ax+ay*w;
					if(inPixels[az][thisi]!=0)
						{
						Vector3i tv=new Vector3i(ax,ay,az);
						part.createElement(tv);
						if(inPixels[az][thisi+1]!=0)
							{
							Vector3i ov=new Vector3i(ax+1,ay,az);
							part.createSpecifyEquivalent(tv, ov);
							}
						if(inPixels[az][thisi+w]!=0)
							{
							Vector3i ov=new Vector3i(ax,ay+1,az);
							part.createSpecifyEquivalent(tv, ov);
							}
						if(az!=d-1) //Could be moved out for speed
							if(inPixels[az+1][thisi]!=0)
								{
								Vector3i ov=new Vector3i(ax,ay,az+1);
								part.createSpecifyEquivalent(tv, ov);
								}
						}
					//Minor bug here: not -1 on both xy, need to add two strips here.
					}
			}
		
		return part;
		}
	
	
	/**
	 * Partition all areas in the volume. Planes must be same size and aligned.
	 */
	/*
	public static List<Set<Vector3d>> exec3d(EvStack stack)
		{

		//Returning a set of points is not optimal. Should return ROIs, which would be a lot easier to work with
		//(resolution independent)
		
		Map<Integer,EvDecimal> imap=new HashMap<Integer, EvDecimal>();
		double dV=0;
		List<EvPixels> plist=new LinkedList<EvPixels>();
		
		Iterator<EvDecimal> zit=imageset.getChannel(channelName).imageLoader.get(frame).keySet().iterator();
		EvDecimal z0=zit.next();
		EvDecimal z1=zit.next();
		double dz=z1.subtract(z0).doubleValue();
		
		int i=0;
		for(Map.Entry<EvDecimal,EvImage> e:stack.entrySet())
			{
			imap.put(i,e.getKey());
			//This ignores shift etc totally
			EvImage evim=e.getValue();
			plist.add(evim.getPixels());
			dV=dz*evim.binning*evim.binning*(evim.resX*evim.resY);
			i++;
			}
		
		
		
		
		Partitioning<Vector3i> p=exec3d(plist);
		
		
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
