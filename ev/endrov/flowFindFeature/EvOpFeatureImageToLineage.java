package endrov.flowFindFeature;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import endrov.imageset.EvChannel;
import endrov.imageset.EvStack;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.Vector3i;

/**
 * Given an image with detected features, create a lineage 
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpFeatureImageToLineage
	{


	private static class WPoint implements Comparable<WPoint>
		{
		public double w;
		public Vector3i point;
		
		public WPoint(double w, Vector3i point)
			{
			this.w = w;
			this.point = point;
			}

		public int compareTo(WPoint a)
			{
			if(w>a.w)
				return -1;
			else if(w<a.w)
				return +1;
			else
				return 0;
			}
		}
	
	/**
	 * Given an image with detected features, create a lineage. The threshold radius prevents points too close from being picked.
	 * Points are then picked according to priority. The priority channel can be null, in which case points are taken in "random" order.
	 * 
	 * @param chFeatures
	 * @param chPriority
	 * @param outputRadius
	 * @param thresholdRadius
	 * @return
	 */
	public static NucLineage featureChannelToLineage(EvChannel chFeatures, EvChannel chPriority, double outputRadius, double thresholdRadius)
		{
		NucLineage lin=new NucLineage();
		for(EvDecimal frame:chFeatures.getFrames())
			{
			System.out.println("Getting feature stack");
			EvStack stFeatures=chFeatures.getStack(frame);
			//int[][] featurePixels=stFeatures.getReadOnlyArraysInt();
			
			int w=stFeatures.getWidth();
			double resX=stFeatures.resX;
			double resY=stFeatures.resY;
			double resZ=stFeatures.resZ;

			System.out.println("Finding features");
			//Find list of points
			List<Vector3i> points=new LinkedList<Vector3i>();
			for(int az=0;az<stFeatures.getDepth();az++)
				{
				int[] p=stFeatures.getInt(az).getPixels().convertToInt(true).getArrayInt();//featurePixels[az];
				for(int i=0;i<p.length;i++)
					if(p[i]!=0)
						points.add(new Vector3i(i%w, i/w, az));
				}
			System.out.println("#Features: "+points.size());
			
			//Generate a list of prioritized points
			Iterator<WPoint> pointi;
			if(chPriority!=null)
				{
				System.out.println("Weighting by priority");
				EvStack stPriority=chPriority.getStack(frame);
				int[][] priorityPixels=stPriority.getReadOnlyArraysInt();
				
				PriorityQueue<WPoint> pq=new PriorityQueue<WPoint>();
				for(Vector3i point:points)
					pq.add(new WPoint(priorityPixels[point.z][point.x*w+point.y],point));
				pointi=pq.iterator();
				}
			else
				{
				LinkedList<WPoint> newlist=new LinkedList<WPoint>();
				for(Vector3i point:points)
					newlist.add(new WPoint(0,point));
				pointi=newlist.iterator();
				}
			System.out.println("Generated list of prioritized points");
			
			//Move through list, threshold away points 
			LinkedList<Vector3i> acceptedPoints=new LinkedList<Vector3i>();
			double thresholdDiameter2=2*2*thresholdRadius*thresholdRadius;
			nextp: while(pointi.hasNext())
				{
				WPoint wp=pointi.next();
				if(thresholdRadius>0)
					{
					for(Vector3i lastp:acceptedPoints)
						{
						double dx=(wp.point.x-lastp.x)*resX;
						double dy=(wp.point.y-lastp.y)*resY;
						double dz=(wp.point.z-lastp.z)*resZ;
						if(thresholdDiameter2>dx*dx+dy*dy+dz*dz)
							continue nextp;
						}
					acceptedPoints.add(wp.point);
					}
				else
					acceptedPoints.add(wp.point);
				}
			System.out.println("#Accepted features: "+acceptedPoints.size());
			
			//Generate lineage
			int nucId=0;
			for(Vector3i v:acceptedPoints)
				{
				NucLineage.Nuc nuc=lin.getCreateNuc(":"+nucId);
				NucLineage.NucPos p=new NucLineage.NucPos();//.pos.get(frame);
				p.x=v.x*resX;
				p.y=v.y*resY;
				p.z=v.z*resZ;
				p.r=outputRadius;
				nuc.pos.put(frame, p);
				nucId++;
				}
			}
		
		return lin;
		}

	}
