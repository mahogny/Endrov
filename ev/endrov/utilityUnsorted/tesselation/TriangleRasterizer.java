package endrov.utilityUnsorted.tesselation;

import java.util.ArrayList;
import endrov.util.Vector2i;
import endrov.utilityUnsorted.tesselation.utils.Line;

/**
 * Rasterization of individual triangles
 * 
 * @author Javier Fernandez
 */
public class TriangleRasterizer
	{
	final int BG_COLOR = 0;

	/**
	 * Raterizes the triangle defined by 'vertices' and stores
	 * the pixels belonging to the triangle area in 'rasterArea' 
	 */
	public static void rasterizeTriangle(int width, int height,
			Vector2i vertices[], ArrayList<Integer> rasterArea)
		{
		rasterize(width, height, vertices, rasterArea);
		}

	/**
	 * Raterizes the triangle defined by 'vertices', returning
	 * the list of pixels belonging to the triangle. 
	 */
	public static ArrayList<Integer> rasterizeTriangle(int width, int height,
			Vector2i vertices[])
		{
		ArrayList<Integer> rasterArea = new ArrayList<Integer>(20);
		rasterize(width, height, vertices, rasterArea);

		return rasterArea;
		}

	/**
	 * Adds to 'rasterArea' the list of the indices of the image array imageArray
	 * of size width-height that are contained in the triangle area defined by the
	 * three points ((x,y) coordinates) on vertices.
	 * 
	 * @param imageArray
	 *          The array representing the image
	 * @param width
	 *          The width of the input image
	 * @param height
	 *          The height of the input image
	 * @param vertices
	 *          Contain the three vertex of the triangle. Just the first three are
	 *          taken into account
	 * @return Indices of imageArray that belong to the triangle defined by
	 *         vertices
	 */
	private static void rasterize(int width, int height, Vector2i vertices[],
			ArrayList<Integer> rasterArea)
		{
		// ArrayList<Integer> rasterArea = new ArrayList<Integer>(20);

		// Set edges
		Line[] edges = new Line[3];
		edges[0] = new Line(vertices[0], vertices[1]);
		edges[1] = new Line(vertices[0], vertices[2]);
		edges[2] = new Line(vertices[1], vertices[2]);
		int shortEdge1 = -1;
		int shortEdge2 = -1;

		// Divide the triangle area in two sub areas. Find longest side
		int maxLength = edges[0].yLength;
		int longerEdge = 0;
		for (int i = 1; i<3; i++)
			{
			if (maxLength<edges[i].yLength)
				{
				maxLength = edges[i].yLength;
				longerEdge = i;
				}
			}

		// Other two edges indexes
		shortEdge1 = (longerEdge+1)%3;
		shortEdge2 = (longerEdge+2)%3;

		rasterizeArea(edges[longerEdge], edges[shortEdge1], width, height,
				rasterArea);
		rasterizeArea(edges[longerEdge], edges[shortEdge2], width, height,
				rasterArea);

		}

	/**
	 * Add to rasterArea the pixels of array that are within the inner area
	 * between the lines longSide and shortSisde tracing horizontal lines
	 */
	private static void rasterizeArea(Line longSide, Line shortSide, int width,
			int height, ArrayList<Integer> rasterArea)
		{

		int min_y;
		int max_y;
		if ((shortSide.p1.y-shortSide.p2.y)<0)
			{ // This may be faster inline
			min_y = shortSide.p1.y;
			max_y = shortSide.p2.y;
			}
		else
			{
			min_y = shortSide.p2.y;
			max_y = shortSide.p1.y;
			}

		// loop covering the whole height which is common to both
		for (int i = min_y; i<=max_y; i++)
			{ // This can be faster asking if the current line has been covered
			// Finding horizontal line

			// Avoid slope-0 curves
			if (shortSide.yLength==0)
				{
				int init = (shortSide.p1.x<shortSide.p2.x) ? shortSide.p1.x
						: shortSide.p2.x;
				int end = (shortSide.p1.x>shortSide.p2.x) ? shortSide.p1.x
						: shortSide.p2.x;
				for (int hx = init; hx<=end; hx++)
					{
					rasterArea.add(i*width+hx);
					}
				break;
				}

			int hx = shortSide.getXGivenY(i);
			int longSideX = longSide.getXGivenY(i);
			int init = (longSideX<hx) ? longSideX : hx;
			int end = (longSideX<hx) ? hx : longSideX;

			// Horizontal line loop
			for (hx = init; hx<=end; hx++)
				{
				rasterArea.add((i*width)+hx);
				}
			}

		}
	}
