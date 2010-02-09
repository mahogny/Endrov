/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.unsortedImageFilters;

import java.util.*;

import javax.vecmath.Vector2d;


/**
 * Convex hulls
 * @author Johan Henriksson
 *
 */
public class ConvexHull
	{
	private static class AnglePoint implements Comparable<AnglePoint>
		{
		Vector2d v;
		double angle;
		public AnglePoint(Vector2d v, Vector2d ref)
			{
			this.v=v;
			angle=Math.atan2(v.y-ref.y, v.x-ref.x);
			}
		public int compareTo(AnglePoint o)
			{
			return Double.compare(angle, o.angle);
			}
		public boolean equals(Object obj)
			{
			if(obj instanceof AnglePoint)
				{
				AnglePoint a=(AnglePoint)obj;
				return angle==a.angle && v.equals(a.v);
				}
			else
				return false;
			}
		
		
		}
	
	/**
	 * Convex hull. General case, allowing repetition of points. O(n log n). graham scan.
	 * 
	 * For pixels I suspect one can write a special O(n) taking into account that pixels are on a grid, they are presorted.
	 * All Atan2 would disappear and the special class would disappear. Most inner points could be ignored by only looking for the outermost
	 * pixels. it would be *a lot* faster.
	 * 
	 * atan2 can be replaced by comparing slope, which can be done in integer with rewrites to multiplication
	 * 
	 * 
	 * 
	 * UNTESTED
	 */
	public static List<Vector2d> convexHull(List<Vector2d> points)
		{
		TreeSet<AnglePoint> sortedpoints=new TreeSet<AnglePoint>();
		
		ArrayList<Vector2d> output=new ArrayList<Vector2d>();
		
		Iterator<Vector2d> itold=points.iterator();

		//Sort points. remove duplicates
		Vector2d refpoint=itold.next();
		output.add(refpoint);
		while(itold.hasNext())
			{
			Vector2d v=itold.next();
			if(!v.equals(refpoint))
				sortedpoints.add(new AnglePoint(v,refpoint));
			}
		
		//Note: this implementation might generate one too many outer points (the ref point)
		
		//Wrap
		//Iterator<AnglePoint> insorted=sortedpoints.iterator();
		
		for(AnglePoint s:sortedpoints)
			{
			output.add(s.v);
			
			while(output.size()>2)
				{
				int size=output.size();
				Vector2d p0=output.get(size-1);
				Vector2d p1=output.get(size-2);
				Vector2d p2=output.get(size-3);
				
				Vector2d v0=new Vector2d(p0);
				v0.sub(p1);
				Vector2d v1=new Vector2d(p1);
				v0.sub(p2);
				double a0=Math.atan2(v0.y, v0.x);
				double a1=Math.atan2(v1.y, v1.x);

				//Potential bug here when angle wraps around
				if(a0>=a1)
					break;
				else
					output.remove(size-1);
				}
			}
		return output;
		}
	
	}
