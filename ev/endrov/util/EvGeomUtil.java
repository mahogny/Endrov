package endrov.util;

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
	}
