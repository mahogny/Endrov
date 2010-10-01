/**
 * Taken and converted from
 * http://www.flipcode.com/archives/Efficient_Polygon_Triangulation.shtml
 * No idea about license but since it's posted on a forum where code is supposed
 * to be free, assume fair citation or something.
 */
package endrov.roi.newer;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2d;

/**
 * Tessellate a triangle with the ear-snipping method
 * http://www.flipcode.com/archives/Efficient_Polygon_Triangulation.shtml
 * Code translated to java
 * 
 * TODO avoid making a fan
 * 
 * @author Johan Henriksson
 * 
 */
public class FlipCodeTessellate
	{
	
	private static final double EPSILON=0.0000000001f;


	/**
	 * Calculate area of simple triangle
	 */
	private static double calcArea(List<Vector2d> contour)
		{
	  int n = contour.size();
	  double A=0;
	  for(int p=n-1,q=0; q<n; p=q++)
	    A+= contour.get(p).x*contour.get(q).y - contour.get(q).x*contour.get(q).y;
	  return A/2;
		}
	
	/**
	 * Check if the point p is inside the triangle
	 */
	private static boolean isInsideTriangle(double Ax, double Ay,
      double Bx, double By,
      double Cx, double Cy,
      double Px, double Py)

		{
		double ax, ay, bx, by, cx, cy, apx, apy, bpx, bpy, cpx, cpy;
		double cCROSSap, bCROSScp, aCROSSbp;
		
		ax = Cx - Bx;  ay = Cy - By;
		bx = Ax - Cx;  by = Ay - Cy;
		cx = Bx - Ax;  cy = By - Ay;
		apx= Px - Ax;  apy= Py - Ay;
		bpx= Px - Bx;  bpy= Py - By;
		cpx= Px - Cx;  cpy= Py - Cy;
		
		aCROSSbp = ax*bpy - ay*bpx;
		cCROSSap = cx*apy - cy*apx;
		bCROSScp = bx*cpy - by*cpx;
		
		return ((aCROSSbp >= 0.0f) && (bCROSScp >= 0.0f) && (cCROSSap >= 0.0f));
		}

			
	/**
	 * Try snip off an ear
	 */
	private static boolean snip(List<Vector2d> contour,int u,int v,int w,int n,int[] V)
		{
		double Ax = contour.get(V[u]).x;
		double Ay = contour.get(V[u]).y;
		double Bx = contour.get(V[v]).x;
		double By = contour.get(V[v]).y;
		double Cx = contour.get(V[w]).x;
		double Cy = contour.get(V[w]).y;
		
		if ( EPSILON > (((Bx-Ax)*(Cy-Ay)) - ((By-Ay)*(Cx-Ax))) )
			return false;
		
		for (int p=0;p<n;p++)
			{
			if( (p == u) || (p == v) || (p == w) )
				continue;
			double Px = contour.get(V[p]).x;
			double Py = contour.get(V[p]).y;
			if (isInsideTriangle(Ax,Ay,Bx,By,Cx,Cy,Px,Py)) 
				return false;
			}
		
		return true;
		}
		
	/**
	 * Tessellate. O(n^2) or O(n^3) for this simple implementation I believe.
	 * Linear time algorithms exist.
	 * @throws TriangulationException 
	 */
	public static List<int[]> process(List<Vector2d> contour) throws TriangulationException
		{
		// allocate and initialize list of Vertices in polygon
		int numVertices = contour.size();
		if ( numVertices < 3 ) 
			throw new RuntimeException("Too few vertices");
		int V[] = new int[numVertices];
		
		
		// we want a counter-clockwise polygon in V
		//TODO enforce from outside?
		if ( 0 < calcArea(contour) )
			for (int v=0; v<numVertices; v++)
				V[v] = v;
		else
			for(int v=0; v<numVertices; v++) 
				V[v] = (numVertices-1)-v;
		
		int nv = numVertices;
		
		/*  remove nv-2 Vertices, creating 1 triangle every time */
		int count = 2*nv;   // error detection 
		List<int[]> result=new ArrayList<int[]>();
		for(int m=0, v=nv-1; nv>2; )
			{
			/* if we loop, it is probably a non-simple polygon */
			if (0 >= (count--))
				throw new TriangulationException("Triangulate: ERROR - probable bad polygon!");
				//throw new RuntimeException("Triangulate: ERROR - probable bad polygon!");
			
			/* three consecutive vertices in current polygon, <u,v,w> */
			int u = v  ; 
			if (nv <= u) 
				u = 0;     /* previous */
			v = u+1; 
			if (nv <= v) 
				v = 0;     /* new v    */
			int w = v+1; 
			if (nv <= w) 
				w = 0;     /* next     */
			
			//Try to take a piece
			if ( snip(contour,u,v,w,nv,V) )
				{
				int a,b,c;//,s,t;
				
				/* true names of the vertices */
				a = V[u]; 
				b = V[v]; 
				c = V[w];
				
				// output Triangle 
				result.add(new int[]{a,b,c});
				/*
				result.push_back( contour[a] );
				result.push_back( contour[b] );
				result.push_back( contour[c] );*/
				
				m++;
				
				/* remove v from remaining polygon */
				for(int s=v,t=v+1;t<nv;s++,t++)
					V[s] = V[t]; 
				nv--;
				
				/* reset error detection counter */
				count = 2*nv;
				}
			
			}
		
		return result;
		}
		
			
			
	
	}
