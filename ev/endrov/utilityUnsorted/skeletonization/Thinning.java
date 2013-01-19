package endrov.utilityUnsorted.skeletonization;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Thinning algorithm for skeletonization. Based on T.Y. Zhang and C.Y. Suen 
 * thinning algorithm
 * 
 * @author Javier Fernandez
 */

public class Thinning
	{

	/**
	 * Gets the 8 circular neighbors taken clockwise starting from the up neighbor
	 * until the up-left one.
	 */
	public static int[] getCircularNeighbors(int position, int w)
		{
		int neighbors[] = new int[8];
		neighbors[0] = position-w; // Up
		neighbors[1] = neighbors[0]+1; // up-right
		neighbors[2] = position+1; // right;
		neighbors[3] = position+w+1; // down-right
		neighbors[4] = neighbors[3]-1; // down
		neighbors[5] = neighbors[4]-1; // down-left
		neighbors[6] = position-1; // left
		neighbors[7] = neighbors[0]-1; // up-left

		return neighbors;
		}

	/**
	 * Returns the number of neighbor pixels that belong to the shape represented
	 * as a boolean array in isShape
	 * 
	 * @param isShape
	 *          binary array that is true for the pixels that belong to the shape
	 * @param neighbors
	 *          neighbors 8-neighborhood of a given pixel
	 * @return number of shape-pixels
	 */
	public static int nonZeroNeighbors(boolean[] isShape, int[] neighbors)
		{
		int count = 0;
		for (int i = 0; i<neighbors.length; i++)
			{
			if (isShape[neighbors[i]])
				count++;
			}
		return count;
		}

	/**
	 * Count the number of false-true (01) patterns clockwise. The neighbors array
	 * must be clockwise starting from up,just as the getCircularNeighbors of this
	 * class returns.
	 * 
	 * @param isShape
	 *          isShape binary array that is true for the pixels that belong to
	 *          the shape
	 * @param neighbors
	 *          8-neighborhood of a given pixel
	 * @return number of false-true (01) patterns, counted clockwise
	 */
	public static int circular01Patterns(boolean[] isShape, int[] neighbors)
		{
		int count = 0;
		for (int i = 0; i<7; i++)
			{
			if (!isShape[neighbors[i]]&isShape[neighbors[i+1]])
				count++;
			}
		if (!isShape[neighbors[7]]&isShape[neighbors[0]])
			count++;

		return count;
		}

	/**
	 * Implements T.Y. Zhang and C.Y. Suen thinning algorithm. Process the image
	 * represented as a distance transformation in dtImage, its corresponding
	 * boolean array isShape and their points list skeletonPoints, removing the
	 * contour pixels that do not belong to the shape and that correspond to the
	 * distance transform index contourIndex. If any pixel is removed returns
	 * true, otherwise returns false. isShape and shapePoints are modified,
	 * removing the selected points.
	 * 
	 * @param dtImage
	 *          distance transformation image
	 * @param isShape
	 *          binary array that is true for the pixels that belong to the shape
	 * @param w
	 *          Width of the image from which the distance transformation was done
	 * @param h
	 *          Height of the image from which the distance transformation was
	 *          done
	 * @param shapePoints
	 *          list containing the positions for the pixels that belong to the
	 *          distance transformed shape
	 * @param contourIndex
	 *          integer index of the currently processed contour
	 */

	public static boolean thinContour(int[] dtImage, boolean[] isShape, int w,
			int h, ArrayList<Integer> shapePoints, int contourIndex)
		{

		boolean isThinner = false;
		int count;
		int n[];
		int a, b;
		Iterator<Integer> it = shapePoints.iterator();
		//double init;
		//double total=0;
		
		// First iteration: remove south-east boundary points and the north-west
		// corner point
		while (it.hasNext())
			{
			count = it.next();			
			if (dtImage[count]> contourIndex)				
				continue;
			// Check the four conditions
			n = Thinning.getCircularNeighbors(count, w);
			b = Thinning.nonZeroNeighbors(isShape, n);
			a = Thinning.circular01Patterns(isShape, n);
			
			if (b==0)
				{ // Remove isolated pixel
				isShape[count] = false;
				it.remove();
				continue;
				}

			if (!((b>=2&&b<=6)&&a==1))
				continue;
			if (isShape[n[0]]&&isShape[n[2]]&&isShape[n[4]])
				continue;
			if (isShape[n[2]]&&isShape[n[4]]&&isShape[n[6]])
				continue;
     			
			isShape[count] = false;
			it.remove();
			isThinner = true;
			}
		it = shapePoints.iterator();
		// Second iteration: remove the north-west boundary points and the
		// south-east corner points
		while (it.hasNext())
			{
			count = it.next();
			if (dtImage[count]>contourIndex)
				continue;
			// Check the four conditions
			n = Thinning.getCircularNeighbors(count, w);
			b = Thinning.nonZeroNeighbors(isShape, n);
			a = Thinning.circular01Patterns(isShape, n);
			if (!((b>=2&&b<=6)&a==1))
				continue;
			if (isShape[n[0]]&&isShape[n[2]]&&isShape[n[6]])
				continue;
			if (isShape[n[0]]&&isShape[n[4]]&&isShape[n[6]])
				continue;

			isShape[count] = false;
			it.remove();
			isThinner = true;
			}
		return isThinner;
		}

	/**
	 * Applies T.Y. Zhang and C.Y. Suen thinning-skeletonization algorithm iteratively calling the
	 * thinContour method,until reaching a 1-pixel width image.
	 * 
	 * @param dtImage
	 *          distance transformation image
	 * @param isShape
	 *          binary array that is true for the pixels that belong to the shape
	 * @param w
	 *          Width of the image from which the distance transformation was done
	 * @param h
	 *          Height of the image from which the distance transformation was
	 *          done
	 * @param shapePoints
	 *          list containing the positions for the pixels that belong to the
	 *          distance transformed shape
	 */
	public static void thinToSkeleton(int[] dtImage, boolean[] isShape, int w,
			int h, ArrayList<Integer> shapePoints)
		{
		boolean makeThinner = true;
		int contourIndex = 1;
		while (makeThinner)
			{
			makeThinner = Thinning.thinContour(dtImage, isShape, w, h, shapePoints,
					contourIndex);
			contourIndex++;
			}
		contourIndex = Integer.MAX_VALUE;
		
		Thinning.thinContour(dtImage, isShape, w, h, shapePoints,
				contourIndex);
		}

	}
