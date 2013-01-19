package endrov.typeWorms.javier.tesselation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Vector2d;

import endrov.roi.util.FlipCodeTessellate;
import endrov.roi.util.TriangulationException;
import endrov.util.math.Vector2i;

/**
 * Rasterization of polygons, by triangulating and then rasterizing individual
 * triangles
 * 
 * @author Javier Fernandez
 */
public abstract class PolygonRasterizer
	{
	
	/**
	 * Generates a list of all the points inside the area of the 
	 * given polygon. The points values are in the image range (width,height)
	 */
	public static ArrayList<Integer> rasterize(int width, int height, List<Vector2d> polygonPoints){		
		// final raster shape
		ArrayList<Integer> rasterShape = new ArrayList<Integer>();	

		//triangulate the polygon
		ArrayList<int[]> triangles = new ArrayList<int[]>();
		try
			{
			triangles = (ArrayList<int[]>) FlipCodeTessellate.process(polygonPoints);
			}
		catch (TriangulationException e)
			{
			//Triangulation Error. Probably bad polygon
			return null;
			}
		Iterator<int[]> tIt = triangles.iterator();
		
		//Rasterize triangles and inner points to rasterShape
		int[] triangle;
		Vector2i[] vertices = new Vector2i[3];
		Vector2d temp;
		while(tIt.hasNext()){
			//Take image indices from triangle
			triangle = tIt.next();
			for(int i=0;i<3;i++){
				temp = polygonPoints.get(triangle[i]);
				vertices[i] = new Vector2i((int)temp.x,(int)temp.y);
			}
			TriangleRasterizer.rasterizeTriangle(width,height,vertices,rasterShape);
		}		
		return rasterShape;
	} 
	
	}
