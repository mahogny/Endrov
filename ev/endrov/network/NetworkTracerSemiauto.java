package endrov.network;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.Vector3i;

public class NetworkTracerSemiauto extends NetworkTracerInterface
	{

	public static NetworkTracerFactory factory=new NetworkTracerFactory()
		{
		public NetworkTracerInterface create()
			{
			return new NetworkTracerSemiauto();
			}

		public String tracerName()
			{
			return "Auto";
			}
		};
	
	
	//This piece of code should become a new flow operator!!!!
	//But it is also useful on a lower level, to allow updating of results
	
	private static class PPixel implements Comparable<PPixel>
		{
		public double cost;
		public short fromX, fromY, fromZ;
		
		public PPixel(double cost, short fromX, short fromY, short fromZ) 
			{
			this.cost = cost;
			this.fromX = fromX;
			this.fromY = fromY;
			this.fromZ = fromZ;
			}

		public int compareTo(PPixel o)
			{
			return Double.compare(cost, o.cost);
			}
		}

	//public static class ProcessedFrame
		//{
		//Arrays to trace 
		//-1 means this is the starting point. 
		private short[][] arrFromX;
		private short[][] arrFromY;
		private short[][] arrFromZ;
		private double[][] leastCost;
		private int w,h,d;
		private double[][] cost;

		
		
		public double[][] getCost(EvStack stack)
			{
			double[][] cost=stack.getReadOnlyArraysDouble(new ProgressHandle());
			
			//Find max value
			double max=Double.MIN_VALUE;
			for(int az=0;az<cost.length;az++)
				for(int ax=0;ax<cost[az].length;ax++)
					{
					double v=cost[az][ax];
					if(v>max)
						max=v;
					}
			
			//Invert the picture, ensure values are positive
			for(int az=0;az<cost.length;az++)
				{
				double[] n=new double[cost[az].length];
				for(int ax=0;ax<cost[az].length;ax++)
					n[ax]=rescale(max-cost[az][ax]);
				cost[az]=n;
				}

			return cost;
			}
		
		private double rescale(double s)
			{
			return s*s;
			}
		
		
		public void preprocess(ProgressHandle progh, EvStack stack, Collection<Vector3i> startingPoints)
			{
			int w=stack.getWidth();
			int h=stack.getHeight();
			int d=stack.getDepth();
			this.w=w;
			this.h=h;
			this.d=d;
			
			double distCost=0;

			//Prepare working arrays. This is only needed the first time, or if seed points are deleted.
			//Speed can be improved by not redoing the entire calculation every time, in particular for 3d data
			if(cost==null)
				{
				cost=getCost(stack); //Use float instead?
				
				leastCost=new double[stack.getDepth()][stack.getWidth()*stack.getHeight()];
				arrFromX=new short[stack.getDepth()][stack.getWidth()*stack.getHeight()];
				arrFromY=new short[stack.getDepth()][stack.getWidth()*stack.getHeight()];
				arrFromZ=new short[stack.getDepth()][stack.getWidth()*stack.getHeight()];
				for(int az=0;az<leastCost.length;az++)
					for(int ax=0;ax<leastCost[az].length;ax++)
						{
						//TODO use System. array fill?
						leastCost[az][ax]=Double.MAX_VALUE;
						arrFromX[az][ax]=-1;
						}
				}
			
			
			//Put in starting points
			PriorityQueue<PPixel> pq=new PriorityQueue<PPixel>();
			for(Vector3i v:startingPoints)
				{
				short x=(short)v.x;
				short y=(short)v.y;
				short z=(short)v.z;
				
				if(x>=0 && x<w && y>=0 && y<h && z>=0 && z<d)
					{
					leastCost[z][y*w+x]=0;
					arrFromX[z][y*w+x]=-1;
					
					PPixel pp=new PPixel(0, x, y, z);
					pq.add(pp);
					}
				else
					System.out.println("Point out of reach: "+x+" "+y+" "+z);
				}
			

			double resX=stack.resX;
			double resY=stack.resY;
			double resZ=stack.resZ;
			
			int tries=0;
			//Run through everything
			while(!pq.isEmpty())
				{
				PPixel pp=pq.poll();
				
				short fromX=pp.fromX;
				short fromY=pp.fromY;
				short fromZ=pp.fromZ;
				
				//Try to go in every direction
				for(int az=fromZ-1;az<=fromZ+1;az++)
					if(az>=0 && az<d)
						for(int ay=fromY-1;ay<=fromY+1;ay++)
							if(ay>=0 && ay<h)
								for(int ax=fromX-1;ax<=fromX+1;ax++)
									if(ax>=0 && ax<w)
										if(ax!=fromX || ay!=fromY || az!=fromZ)
											{
											tries++;
											tryOther(
													distCost,
													resX, resY, resZ,
													pq, leastCost, cost[az][ay*w+ax], pp, 
													(short)ax, (short)ay, (short)az, ay*w+ax, 
													fromX,fromY,fromZ);
											}
				}
			
			System.out.println("====== total tries: "+tries);
			
			
			}
		
		
		private void tryOther(
				double distCost,
				double resX, double resY, double resZ,
				PriorityQueue<PPixel> pq, double[][] leastCost, double costPixel, PPixel pp, 
				short x, short y, short z, int indexXY, 
				short fromX, short fromY, short fromZ)
			{
			double dx=fromX-x;
			double dy=fromY-y;
			double dz=fromZ-z;
			double len=Math.sqrt(
					dx*dx*resX*resX+
					dy*dy*resY*resY+
					dz*dz*resZ*resZ);
			double costHere=pp.cost+(costPixel+distCost)*len;
			if(costHere<leastCost[z][indexXY])
				{
				arrFromX[z][indexXY]=fromX;
				arrFromY[z][indexXY]=fromY;
				arrFromZ[z][indexXY]=fromZ;
				leastCost[z][indexXY]=costHere;
				pq.add(new PPixel(costHere, x, y, z));
				}
			}
			
		/**
		 * Find the least-cost path to the given point
		 */
		public List<Vector3i> findPathTo(int x, int y, int z)
			{
			LinkedList<Vector3i> points=new LinkedList<Vector3i>();
			if(x>=0 && x<w && y>=0 && y<h && z>=0 && z<d)
				{
				do
					{
					int xy=w*y+x;
					int nextX=arrFromX[z][xy];
					int nextY=arrFromY[z][xy];
					int nextZ=arrFromZ[z][xy];
					points.add(new Vector3i(x, y, z));
					x=nextX;
					y=nextY;
					z=nextZ;
					} while(x!=-1);
				return points;
				}
			else
				return null;
			
		
			}
		
		
	

	}
