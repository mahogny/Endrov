package endrov.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Vector3d;


public class EvGeomUtil
	{
	/**
	 * Calculate area of a convex polygon
	 */
	public static double polygonArea(Vector3d[] vv)
		{
		int n=vv.length;
		double area=0;
		Vector3d vA=vv[0];
		for(int i=1;i<n-1;i++)
			{
			Vector3d vAB=vv[i];
			Vector3d vAC=vv[i+1];
			vAB.sub(vA);
			vAC.sub(vA);
			double x=vAB.dot(vAC);
			area+=Math.sqrt(vAB.lengthSquared()*vAC.lengthSquared()-x*x);
			}
		return area*0.5;
		}
	
	
	/**
	 * Calculate angle at B, given 3 positions
	 */
	public static double midAngle(Vector3d posA, Vector3d posB, Vector3d posC)
		{
		Vector3d ba=new Vector3d(posA);
		ba.sub(posB);
		Vector3d bc=new Vector3d(posC);
		bc.sub(posB);
		return Math.acos(ba.dot(bc)/(ba.length()*bc.length()));
		}
	
	
	public static double interpolate(double x1, double y1, double x2, double y2, double x)
		{
		double s=(x-x1)/(x2-x1);
		return s*y2+(1.0-s)*y1;
		}
	
	
	
	/**
	 * Internal class used to find special cases of cube cuts
	 */
	private static class UnclassifiedPoint implements Comparable<UnclassifiedPoint>
		{
		public Vector3d v;
		public double angle;
		public int compareTo(UnclassifiedPoint o)
			{
			if(angle<o.angle)				return -1;
			else if(angle>o.angle)	return 1;
			else										return 0;
			}
		}

	/**
	 * Sorts vertices in a convex polygon
	 */
	public static Vector3d[] sortConvexPolygon(Vector3d[] points)
		{
		Vector3d center=new Vector3d();
		for(Vector3d v:points)
			center.add(v);
		center.scale(1.0/points.length);

		//Project points
		List<UnclassifiedPoint> ups=new ArrayList<UnclassifiedPoint>();
		for(int ap=0;ap<points.length;ap++)
			if(points[ap]!=null)
				{
				UnclassifiedPoint up=new UnclassifiedPoint();
				up.v=points[ap];
				Vector3d v=new Vector3d(points[ap]);
				v.sub(center);
				up.angle=Math.atan2(v.y, v.x); 
				//This *will* explode in some instances. a rotation would solve it.
				ups.add(up);
				}
		Collections.sort(ups);
		
		for(int ap=0;ap<points.length;ap++)
			points[ap]=ups.get(ap).v;
		return points;
		}
	
	public static void main(String[] arg)
		{
		Vector3d[] v=new Vector3d[]{new Vector3d(0,0,0), new Vector3d(1,0,0),
				new Vector3d(4,1,0),new Vector3d(0,1,0)};
		double area=polygonArea(sortConvexPolygon(v));
		System.out.println(area);
		}
	}
