/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Vector3d;


/**
 * Various equations used for geometry
 * 
 * @author Johan Henriksson
 *
 */
public class EvGeomUtil
	{
	
	/**
	 * Calculate area of a convex polygon
	 */
	public static double polygonArea(Vector3d[] vv)
		{
		double area=0;
		Vector3d vA=vv[0];
		for(int i=1;i<vv.length-1;i++)
			{
			Vector3d vAB=new Vector3d(vv[i]);
			Vector3d vAC=new Vector3d(vv[i+1]);
			vAB.sub(vA);
			vAC.sub(vA);
			double x=vAB.dot(vAC);
			area+=Math.sqrt(vAB.lengthSquared()*vAC.lengthSquared()-x*x);
			}
		return area*0.5;
		}
	
	/**
	 * Calculate area of triangle given lengths of sides.
	 * Uses Heron's formula.
	 */
	public static double triangleAreaUsingSides(double a, double b, double c)
		{
		double s=(a+b+c)/2;
		return Math.sqrt(s*(s-a)*(s-b)*(s-c));
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
	
	
	/**
	 * Linear interpolation: Given (x1,y1) and (x2,y2), find y in (x,y)
	 */
	public static double interpolateLinear(double x1, double y1, double x2, double y2, double x)
		{
		double s=(x-x1)/(x2-x1);
		return s*y2+(1.0-s)*y1;
		}
	
	
	/**
	 * Fit y=ax^2+bx through (0,0), (x1,y1), (x2,y2). 
	 * @return a,b
	 */
	public static Tuple<Double,Double> fitQuadratic(double x1, double y1, double x2, double y2)
		{
		double xSQ=x1*x1;
		double dSQ=x2*x2;
		double det=xSQ*x2-dSQ*x1;
		
		double a=(y1*x2-y2*x1)/det;
		double b=(y2*xSQ-y1*dSQ)/det;
		return Tuple.make(a, b);
		}
	
	
	/**
	 * Internal class used to find special cases of cube cuts
	 */
	private static class UnclassifiedPoint implements Comparable<UnclassifiedPoint>
		{
		int index;
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
				up.index=ap;
				Vector3d v=new Vector3d(points[ap]);
				v.sub(center);
				up.angle=Math.atan2(v.y, v.x); 
				//This *will* explode in some instances. a rotation would solve it.
				ups.add(up);
				}
		Collections.sort(ups);
		
		for(int ap=0;ap<points.length;ap++)
			points[ap]=points[ups.get(ap).index];
		return points;
		}
	
	public static void main(String[] arg)
		{
		System.out.println(interpolateLinear(10, 2, 20, 4, 50));
		
		
		Vector3d[] v=new Vector3d[]{new Vector3d(0,0,0), new Vector3d(1,0,0),
				new Vector3d(4,1,0),new Vector3d(0,1,0)};
		double area=polygonArea(sortConvexPolygon(v));
		System.out.println(area);
		}
	}
