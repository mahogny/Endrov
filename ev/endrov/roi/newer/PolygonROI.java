package endrov.roi.newer;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2d;

/**
 * A ROI defined by a polygon
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class PolygonROI
	{
	/**
	 * Points. Should be in counter-clock-wise order
	 */
	public ArrayList<Vector2d> contour=new ArrayList<Vector2d>(); 
	
	public static class Tessellation
		{
		int[][] tris;
		//Array of triples	
		}
	
	private Tessellation userTessellation=null;
	private Tessellation cachedTessellation=null;
	
	public boolean hintConvex=false;
	
	
	/**
	 * Tesselate a convex polygon, O(n)
	 * 
	 * TODO test
	 * 
	 */
	private Tessellation tessellateConvex()
		{
		int numPoly=contour.size()-2;
		
		Tessellation tes=new Tessellation();
		tes.tris=new int[numPoly][3];
		
		for(int i=0;i<numPoly;i++)
			{
			tes.tris[i][0]=0;
			tes.tris[i][1]=i+1;
			tes.tris[i][2]=i+2;
			}

		return tes;
		}

	/**
	 * Get tesselation, calculate if needed
	 * @throws TriangulationException 
	 */
	public Tessellation getTessellation() throws TriangulationException
		{
		if(userTessellation!=null)
			return userTessellation;
		else
			{
			if(cachedTessellation==null)
				{
				if(hintConvex)
					cachedTessellation=tessellateConvex();
				else
					{
					List<int[]> ret=FlipCodeTessellate.process(contour);
					Tessellation t=new Tessellation();
					t.tris=ret.toArray(new int[][]{});
					cachedTessellation=t; 
					}
				
				
				
				}
			
			
			
			
			return cachedTessellation;
			}
		}

	/**
	 * Set user-defined tesselation
	 */
	public void setUserTessellation(Tessellation tes)
		{
		userTessellation=tes;
		cachedTessellation=null;
		}
	
	}
