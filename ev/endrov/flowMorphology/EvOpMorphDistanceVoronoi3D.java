package endrov.flowMorphology;

import java.util.*;

import endrov.flow.EvOpStack;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;

/**
 * Calculate distance to nearest non-zero pixel. Also keeps track of the intensity of the
 * closest pixel, which can be used to calculate the Voronoi regions for arbitrary geometry.
 * In this case, first assign each blob a unique color before invoking.
 * <p>
 * Tests 6 directions, if also moving along diagonals then 26 directions
 * <p>
 * Returns 2 channels, first distance, then closest intensity
 * <p>
 * Worst case up to O(whd log(whd)). More realistically closer to O(whd).
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpMorphDistanceVoronoi3D extends EvOpStack
	{
	private final boolean alsoDiagonal;
	
	public EvOpMorphDistanceVoronoi3D(boolean alsoDiagonal)
		{
		this.alsoDiagonal = alsoDiagonal;
		}

	@Override
	public EvStack[] exec(EvStack... p)
		{
		return apply(alsoDiagonal, p[0]);
		}
	
	@Override
	public int getNumberChannels()
		{
		return 2;
		}


	/**
	 * Queue element, euclidian distance
	 */
	private static class QEeuclidian implements Comparable<QEeuclidian>
		{
		public int x,y,z;
		public double dist;
		public double group;
		
		public QEeuclidian(int x, int y, int z, double dist, double group)
			{
			this.x = x;
			this.y = y;
			this.z = z;
			this.dist = dist;
			this.group = group;
			}

		public int compareTo(QEeuclidian o)
			{
			if(dist<o.dist)
				return -1;
			else if(dist>o.dist)
				return 1;
			else
				return 0;
			}
		}
	
	public static EvStack[] apply(boolean alsoDiagonal, EvStack stack)
		{
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();
		
		EvStack stackGroup=new EvStack();
		stackGroup.getMetaFrom(stack);
		stackGroup.allocate(w, h, d, EvPixelsType.DOUBLE, stack);

		EvStack stackDist=new EvStack();
		stackDist.getMetaFrom(stack);
		stackDist.allocate(w, h, d, EvPixelsType.DOUBLE, stack);

		double[][] inarr=stack.getReadOnlyArraysDouble();
		double[][] distArr=stackDist.getOrigArraysDouble();
		double[][] groupArr=stackGroup.getOrigArraysDouble();
		PriorityQueue<QEeuclidian> q=new PriorityQueue<QEeuclidian>();
		
		double sqrt2=Math.sqrt(2);
		double sqrt3=Math.sqrt(3);
		
		//Initialize
		for(int az=0;az<d;az++)
			{
			double[] distarrz=distArr[az];
			double[] grouparrz=groupArr[az];
			double[] inarrz=inarr[az];
			
			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w;ax++)
					{
					int i=ay*w+ax;
					if(inarrz[i]!=0)
						{
						q.add(new QEeuclidian(ax,ay,az,0,inarrz[i]));
						distarrz[i]=0;
						grouparrz[i]=inarrz[i];
						}
					else
						distarrz[i]=Double.MAX_VALUE;
					}
			
			}
		
		//Go through all pixels
		while(!q.isEmpty())
			{
			//Take the next pixel off queue
			QEeuclidian p=q.poll();
			
			//Make sure the compiler can assume values to be static
			int x=p.x;
			int y=p.y;
			int z=p.z;
			double group=p.group;

			double nextDistStraight=p.dist+1;
			doNeighEuclidian(x-1, y, z, group, w, h, d, nextDistStraight,	distArr, groupArr, q);
			doNeighEuclidian(x+1, y, z, group, w, h, d, nextDistStraight,	distArr, groupArr, q);
			doNeighEuclidian(x, y-1, z, group, w, h, d, nextDistStraight,	distArr, groupArr, q);
			doNeighEuclidian(x, y+1, z, group, w, h, d, nextDistStraight,	distArr, groupArr, q);
			doNeighEuclidian(x, y, z-1, group, w, h, d, nextDistStraight,	distArr, groupArr, q);
			doNeighEuclidian(x, y, z+1, group, w, h, d, nextDistStraight,	distArr, groupArr, q);

			//Moving along diagonals adds many operations, hence optional
			if(alsoDiagonal)
				{
				double nextDist2=p.dist+sqrt2;
				doNeighEuclidian(x-1, y-1, z, group, w, h, d, nextDist2,	distArr, groupArr, q);
				doNeighEuclidian(x-1, y+1, z, group, w, h, d, nextDist2,	distArr, groupArr, q);
				doNeighEuclidian(x+1, y-1, z, group, w, h, d, nextDist2,	distArr, groupArr, q);
				doNeighEuclidian(x+1, y+1, z, group, w, h, d, nextDist2,	distArr, groupArr, q);
				doNeighEuclidian(x+1, y, z-1, group, w, h, d, nextDist2,	distArr, groupArr, q);
				doNeighEuclidian(x+1, y, z+1, group, w, h, d, nextDist2,	distArr, groupArr, q);
				doNeighEuclidian(x-1, y, z-1, group, w, h, d, nextDist2,	distArr, groupArr, q);
				doNeighEuclidian(x-1, y, z+1, group, w, h, d, nextDist2,	distArr, groupArr, q);
				doNeighEuclidian(x, y+1, z-1, group, w, h, d, nextDist2,	distArr, groupArr, q);
				doNeighEuclidian(x, y+1, z+1, group, w, h, d, nextDist2,	distArr, groupArr, q);
				doNeighEuclidian(x, y-1, z-1, group, w, h, d, nextDist2,	distArr, groupArr, q);
				doNeighEuclidian(x, y-1, z+1, group, w, h, d, nextDist2,	distArr, groupArr, q);
	
				double nextDist3=p.dist+sqrt3;
				doNeighEuclidian(x-1, y-1, z-1, group, w, h, d, nextDist3,	distArr, groupArr, q);
				doNeighEuclidian(x+1, y-1, z-1, group, w, h, d, nextDist3,	distArr, groupArr, q);
				doNeighEuclidian(x-1, y+1, z-1, group, w, h, d, nextDist3,	distArr, groupArr, q);
				doNeighEuclidian(x+1, y+1, z-1, group, w, h, d, nextDist3,	distArr, groupArr, q);
				doNeighEuclidian(x-1, y-1, z+1, group, w, h, d, nextDist3,	distArr, groupArr, q);
				doNeighEuclidian(x+1, y-1, z+1, group, w, h, d, nextDist3,	distArr, groupArr, q);
				doNeighEuclidian(x-1, y+1, z+1, group, w, h, d, nextDist3,	distArr, groupArr, q);
				doNeighEuclidian(x+1, y+1, z+1, group, w, h, d, nextDist3,	distArr, groupArr, q);
				}
			
			}
		
		return new EvStack[]{stackDist, stackGroup};
		}
	
	private static void doNeighEuclidian(int nx, int ny, int nz, double group, int w, int h, int d, double nextDist, 
			double[][] distArr, double[][] groupArr, PriorityQueue<QEeuclidian> q)
		{

		if(nx>=0 && nx<w && ny>=0 && ny<h && nz>=0 && nz<d)
			{ //Will try to go into itself, but cannot recurse infinitely. Saves one branch point.
			int ni=ny*w+nx;
			double lastDist=distArr[nz][ni];
			if(nextDist<lastDist)
				{
				distArr[nz][ni]=nextDist;
				groupArr[nz][ni]=group;
				q.add(new QEeuclidian(nx,ny,nz,nextDist,group));
				}
			}

		}
	
	
	
	
	
	
	}
