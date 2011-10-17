package endrov.network;

import java.util.LinkedList;

import javax.vecmath.Vector3d;

import endrov.imageset.EvStack;

/**
 * Calculates intensity(r) as a histogram
 *  
 * @author Johan Henriksson
 *
 */
public class NetworkIntensityRadiusHistogram
	{
	public double[] hist;
	public double histdr;

	private static class Line
		{
		private double x1, y1, z1;
		private double x2, y2, z2;
		private double dx, dy, dz;
		
		private double dist2;
		
		public Line(Network.Point p1, Network.Point p2)
			{
			x1=p1.x;
			y1=p1.y;
			z1=p1.z;

			x2=p2.x;
			y2=p2.y;
			z2=p2.z;

			dx=p2.x-p1.x;
			dy=p2.y-p1.y;
			dz=p2.z-p1.z;
			
			dist2=dx*dx + dy*dy + dz*dz;
			}
		
		public double getU(Vector3d p)
			{
			// http://paulbourke.net/geometry/pointline/
			//double u=((p.x-x1)*(x2-x1) + (p.y-y1)*(y2-y1) + (p.z-z1)*(z2-z1))/dist12;
			return ((p.x-x1)*dx + (p.y-y1)*dy + (p.z-z1)*dz)/dist2;
			}
		
		public double getStraightDist2(Vector3d p, double u)
			{
			double x=x1 + u*dx;
			double y=y1 + u*dy;
			double z=z1 + u*dz;
			return dist2(p, x,y,z);
			}
		
		public double getDist2Endpoints(Vector3d p)
			{
			return Math.min(
				dist2(p, x1, y1, z1),
				dist2(p, x2, y2, z2)
			);
			}
		
		private Double dist2(Vector3d p, double x, double y, double z)
			{
			double diffx=p.x-x;
			double diffy=p.y-y;
			double diffz=p.z-z;
			return diffx*diffx + diffy*diffy + diffz*diffz;
			}
		
		
		}
	
	
	public static double maxRadius(Network.NetworkFrame nf)
		{
		//What is the radius we have to consider?
		double maxr=0;
		for(Network.Point p:nf.points.values())
			{
			if(p.r!=null)
				if(p.r>maxr)
					maxr=p.r;
			}
		return maxr;
		}
	
	
	public NetworkIntensityRadiusHistogram(Network.NetworkFrame nf, EvStack stack, double maxRadius, int histBins)
		{
		//The fastest way is to use levelsets
		
		//Can get rid of a lot of volume using normal dijkstra
		
		
		int w=stack.getWidth();
		int h=stack.getHeight();
		int d=stack.getDepth();

		//Precalculate lines to test against
		LinkedList<Line> lines=new LinkedList<Line>();
		for(Network.Segment s:nf.segments)
			for(int num=0;num<s.points.length-1;num++)
				{
				Network.Point base=nf.points.get(s.points[num]);
				Network.Point nextp=nf.points.get(s.points[num+1]);
				lines.add(new Line(base, nextp));
				}
		
		//How to discretize?
		double maxr2=maxRadius*maxRadius;
		double[] histSum=new double[histBins];
		int[] histCount=new int[histBins];
		double histdr=maxr2/histBins;
		
		//For all pixels
		for(int az=0;az<d;az++)
			{
			double[] arr=stack.getInt(az).getPixels(null).convertToDouble(true).getArrayDouble();
			
			for(int ay=0;ay<h;ay++)
				for(int ax=0;ax<w;ax++)
					{
					Vector3d pos=stack.transformImageWorld(new Vector3d(ax,ay,az));
					
					//Find closest distance to any line
					double closestDist2=Double.MAX_VALUE;
					for(Line line:lines)
						{
						double u=line.getU(pos);
						double dist2;
						if(u>=0 && u<=1)
							dist2=line.getStraightDist2(pos, u);
						else
							dist2=line.getDist2Endpoints(pos);   //TODO maybe not do this if it is not an interior point?
						
						
						
						if(dist2<closestDist2)
							closestDist2=dist2;
						}
					
					//Sum up histogram
					if(closestDist2<maxr2)
						{
						double dist=Math.sqrt(closestDist2);
						int bin=(int)(dist/histdr);
						histCount[bin]++;
						histSum[bin]+=arr[ay*w+ax];
						}
					}
			}
		
		//Create histogram
		hist=new double[histBins];
		for(int i=0;i<histBins;i++)
			hist[i]=histSum[i]/histCount[i];
		this.histdr=histdr;
		}
	
	}
