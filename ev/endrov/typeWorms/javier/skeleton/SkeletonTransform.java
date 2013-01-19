package endrov.typeWorms.javier.skeleton;

import java.util.ArrayList;
import java.util.Iterator;

import endrov.typeImageset.EvPixels;
import endrov.typeWorms.javier.WormPixelMatcher;
import endrov.typeWorms.javier.paths.WormPathTracking;
import endrov.util.math.Vector2i;
import endrov.utilityUnsorted.skeletonization.Thinning;

/**
 * Abstract definition for a morphological skeleton based on distance
 * transform, and associated tools for distance-transform-based 
 * skeleton manipulations
 * 
 * @author Javier Fernandez
 *
 */
public abstract class SkeletonTransform
	{
	final static int[] diagDic =
		{ -1, 0, -1, 4, -1, 4, -1, 0 };// complete cross

	abstract int[] getNeighbors(int pixelPosition, int w);

	/**
	 * Returns the neighbor that corresponds to the maximum directional movement
	 * from previousPixel to currentPixel, performing the movement
	 * neighborMovement.
	 */
	abstract Vector2i getMaxDirectionalNeighbor(int[] imageArray, int w,
			int currentPixel, int neighborMovement);

	/**
	 * Returns all the neighbors obtained performing the movement neighborMovement
	 * from previousPixel to currentPixel
	 */
	abstract ArrayList<Vector2i> getDirectionalNeighbors(int[] imageArray, int w,
			int currentPixel, int neighborMovement);

	/**
	 * Checks whether pixel is a connected pixel in skeleton.
	 */
	abstract public boolean nonConnectedPixel(boolean[] skeleton, int w, int pixel);

	/**
	 * Calculates the skeleton associated with the input distance transform image
	 * and returns the skeleton image
	 * 
	 * @param input
	 *          Distance transformed image where background pixels are 0's
	 * @return Skeleton calculated from the input distance transform image
	 */
	public EvPixels getSkeleton(EvPixels input)
		{
		int w = input.getWidth();
		int h = input.getHeight();
		int[] imageArray = input.getArrayInt();
		ArrayList<Integer> skPoints = new ArrayList<Integer>();
		boolean isSkeleton[] = new boolean[w*h];

		for (int i = 0; i<imageArray.length; i++)
			{
			if (imageArray[i]>0)
				{
				skPoints.add(i);
				isSkeleton[i] = true;
				}
			else
				isSkeleton[i] = false;
			}

		Thinning.thinToSkeleton(imageArray, isSkeleton, w, h, skPoints);
		EvPixels skImage = SkeletonUtils.buildImage(w, h, isSkeleton);

		return skImage;
		}

	
	/**
	 * Calculates the general skeleton associated with distance transform image
	 * (dt) taken from image and returns a list containing the isolated Worm
	 * skeletons that appear clustered or overlapped. This function calls the
	 * getWormClusterSkeletons(EvPixels,int[],int) given the input parameters and
	 * setting minPixels as 60
	 * 
	 * @param image
	 *          The initial image from which the distance transformation is taken
	 * @param dt
	 *          The distance transformation of the initial image
	 */

	public ArrayList<WormClusterSkeleton> getWormClusterSkeletons(EvPixels image,
			int[] dtArray, WormPixelMatcher wpm)
		{
		return getWormClusterSkeletons(image, dtArray, wpm, 60);
		}

	/**
	 * Calculates the general skeleton associated with distance transform image
	 * (dt) taken from image and returns a list containing the isolated Worm
	 * skeletons that appear clustered or overlapped. Any skeleton that contains
	 * less than minPixels number of pixels will be discarded.
	 * 
	 * @param image
	 *          The initial image from which the distance transformation is taken
	 * @param dt
	 *          The distance transformation of the initial image
	 * @param minPixels
	 *          The minimum number of pixels to be considered worm skeleton
	 */

	public ArrayList<WormClusterSkeleton> getWormClusterSkeletons(EvPixels image,
			int[] dtArray, WormPixelMatcher wpm, int minPixels)
		{
		int w = wpm.getW();
		int h = wpm.getH();
		// int[] dtArray = dt.getArrayInt();
		ArrayList<Integer> skPoints = new ArrayList<Integer>();
		ArrayList<Integer> basePoints;
		ArrayList<ArrayList<Integer>> isolatedPoints = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> isolatedBases = new ArrayList<ArrayList<Integer>>();
		ArrayList<WormClusterSkeleton> wcList = new ArrayList<WormClusterSkeleton>();
		boolean isSkeleton[] = new boolean[w*h];

		for (int i = 0; i<dtArray.length; i++)
			{
			if (dtArray[i]>0)
				{
				skPoints.add(i);
				isSkeleton[i] = true;
				}
			else
				isSkeleton[i] = false;
			}

		// reduce, detect base points and isolate
		Thinning.thinToSkeleton(dtArray, isSkeleton, w, h, skPoints);
		basePoints = detectBasePoints(isSkeleton, w, skPoints);
		expandToWormBase(dtArray, w, isSkeleton, skPoints, basePoints);
		isolate(basePoints, isSkeleton, w, h, isolatedPoints, isolatedBases);

		// Create a worm cluster skeleton using the obtained isolated points and
		// their corresponding bases
		Iterator<ArrayList<Integer>> ip = isolatedPoints.iterator();
		Iterator<ArrayList<Integer>> ib = isolatedBases.iterator();
		ArrayList<Integer> currentPointList;
		ArrayList<Integer> currentBaseList;
		while (ip.hasNext())
			{
			currentPointList = ip.next();
			currentBaseList = ib.next();
			if (currentPointList.size()+currentBaseList.size()<minPixels)
				continue;
			WormClusterSkeleton wcs = new WormClusterSkeleton(image, dtArray, w, h,
					currentBaseList, currentPointList, wpm);
			wcList.add(wcs);
			}

		return wcList;
		}

	/**
	 * Calculates the shape of the worm skeleton if the number of skeleton points
	 * are equal or bigger than minLength
	 * 
	 * @param minLength
	 *          minimum number of points for the worm skeleton if equals 0 then
	 *          the skeleton can have any length
	 */
	public static ArrayList<Integer> getShapeContour(WormClusterSkeleton wc,
			int minLength)
		{
		if ((minLength>0&&minLength<=SkeletonUtils.calculatePathLength(wc.skPoints,
				wc.wpm))
				||minLength==0)
			{
			return getShapeContour(wc);
			}
		return null;
		}

	/**
	 * Calculates the contour of a worm given its skeleton. The method finds the
	 * closest contour pixel and follows it until no more contour pixel exist
	 */
	private static ArrayList<Integer> getShapeContour(WormClusterSkeleton wc)
		{
		// Just for fixed Skeletons
		if (wc.basePoints.size()!=2)
			return null;
		// System.out.println("Tracing contour of isolated worm");
		ArrayList<Integer> contour = new ArrayList<Integer>();
		int init = wc.basePoints.get(0);
		int firstContour = -1;
		int[] neigh;
		int min = Integer.MAX_VALUE;
		int minI = -1;

		if (wc.dt[init]==1)
			firstContour = init;
		else
			{
			neigh = SkeletonUtils.getCircularNeighbors(init, wc.w);
			for (int i = 0; i<neigh.length; i++)
				{
				if (wc.dt[neigh[i]]==1)
					{
					firstContour = neigh[i];
					break;
					}
				else if (wc.dt[neigh[i]]<min)
					{
					min = wc.dt[neigh[i]];
					minI = i;
					}
				}
			}
		// find the closest contour (Should always work)
		if (firstContour==-1)
			{
			ArrayList<Vector2i> dir = SkeletonUtils.getDirectionalNeighbors(null,
					wc.w, min, minI);
			Iterator<Vector2i> it = dir.iterator();
			Vector2i v;
			while (it.hasNext())
				{
				v = it.next();
				if (wc.dt[v.x]==1)
					{
					firstContour = v.x;
					break;
					}
				}
			}

		// Follow contour
		boolean[] isContour = new boolean[wc.w*wc.h];
		followContourPath(wc, firstContour, isContour, contour);

		return contour;
		}

	/**
	 * Follows recursively the contour of the given worm skeleton 'wc'. First
	 * contour is supposed as contour element. The next non-contour neighbor is
	 * found until no more contour pixel exists
	 */
	private static void followContourPath(WormClusterSkeleton wc,
			int firstContour, boolean[] isContour, ArrayList<Integer> contour)
		{
		int neigh[];
		contour.add(firstContour);
		isContour[firstContour] = true;

		neigh = SkeletonUtils.getCircularNeighbors(firstContour, wc.w);
		for (int i = 0; i<neigh.length; i++)
			{
			if (wc.dt[neigh[i]]==1&&!isContour[neigh[i]])
				{
				firstContour = neigh[i];
				followContourPath(wc, firstContour, isContour, contour);
				}
			}
		}

	/**
	 * Finds all the connected paths starting and ending in base points and
	 * indexes them to isolate them. Returns then a matrix-like array of width h
	 * and height h that contains the isolation index for each pixel, so pixels
	 * with same number belong to the same connected skeleton. Every shape-pixel
	 * receives and isolation index.
	 * 
	 * @param basePoints
	 *          List of base (or extreme) points of the shape figure
	 * @param isSkeleton
	 *          Matrix-like array, true for every position that belongs to the
	 *          skeleton
	 * @param w
	 *          Width of the image matrix represented in isSkeleton
	 * @param h
	 *          Height of the image matrix represented in isSkeleton
	 */
	private int[] isolate(ArrayList<Integer> basePoints, boolean[] isSkeleton,
			int w, int h, ArrayList<ArrayList<Integer>> isolatedPoints,
			ArrayList<ArrayList<Integer>> isolatedBases)
		{
		int[] matching = new int[w*h];
		boolean[] isBase = SkeletonUtils.listToMatrix(w*h, basePoints);
		int isoCount = 1;
		int base;
		Iterator<Integer> bIt = basePoints.iterator();
		while (bIt.hasNext())
			{
			base = bIt.next();
			if (matching[base]==0)
				{
				ArrayList<Integer> currentSkPoints = new ArrayList<Integer>();
				ArrayList<Integer> currentBasePoints = new ArrayList<Integer>();
				isolatedPoints.add(currentSkPoints);
				isolatedBases.add(currentBasePoints);

				WormPathTracking.pathToBase(matching, isBase, isSkeleton, w, base,
						isoCount, currentSkPoints, currentBasePoints);
				isoCount += 1;
				}
			}
		return matching;
		}


	/**
	 * Finds all the base or extreme points given the binary representation
	 * isShape. A base point is such that, in a 1-pixel width skeleton, has only
	 * one neighbor or has two neighbors in the same direction
	 * 
	 * @param isShape
	 *          Matrix-like array, true for every position that belongs to the
	 *          shape
	 * @param w
	 *          Width of the image matrix represented in isSkeleton
	 * @param shapePoints
	 *          List of the pixels that belong to the shape represented in isShape
	 * @return
	 */
	private static ArrayList<Integer> detectBasePoints(boolean[] isShape, int w,
			ArrayList<Integer> shapePoints)
		{
		ArrayList<Integer> basePoints = new ArrayList<Integer>();
		Iterator<Integer> it = shapePoints.iterator();
		int pixel;
		int neigh[];
		int totalNeigh;
		int totalHitAreas;

		while (it.hasNext())
			{
			pixel = it.next();
			neigh = SkeletonUtils.getCircularNeighbors(pixel, w);
			totalNeigh = 0;
			totalHitAreas = 0;

			for (int i = 0; i<7; i++)
				{
				if (isShape[neigh[i]]&&isShape[neigh[i+1]])
					totalHitAreas++;
				if (isShape[neigh[i]])
					totalNeigh++;
				}
			if (isShape[neigh[7]]&&isShape[neigh[0]])
				totalHitAreas++;
			if (isShape[neigh[7]])
				totalNeigh++;

			if (totalNeigh==1||(totalHitAreas==1&&totalNeigh==2))
				{
				basePoints.add(pixel);
				}
			}
		return basePoints;
		}

	/**
	 * Expands the skeleton extremes (which are not the worm extreme points) to
	 * match the worm extremes
	 */
	private static void expandToWormBase(int[] dtArray, int w,
			boolean[] isSkPoint, ArrayList<Integer> skPoints,
			ArrayList<Integer> basePoints)
		{
		int current;
		int previous;
		int move;
		Vector2i next;
		int[] neigh;
		int pIndex = -1;
		ArrayList<Integer> newBases = new ArrayList<Integer>(basePoints.size());
		int[] dic =
			{ 4, 6, 0, 2 };// opposite direction
		int extra;

		// Procedure: Follow Max directional neighbor until a
		// 1-value pixel is found or until the neighbor is out of the
		// shape (picking the last one)

		Iterator<Integer> bIt = basePoints.iterator();
		previous = -1;
		while (bIt.hasNext())
			{
			current = bIt.next();
			// Already border pixel
			if (dtArray[current]==1)
				{
				newBases.add(current);
				continue;
				}
			// Find previous skeleton pixel
			neigh = SkeletonUtils.getCrossNeighbors(current, w);
			for (int i = 0; i<4; i++)
				{
				if (isSkPoint[neigh[i]])
					{
					previous = neigh[i];
					pIndex = i;
					}
				}
			if (previous==-1)
				{
				newBases.add(current);
				continue;
				}
			move = dic[pIndex];

			// Follow best neighbor until background or border pixel is found
			while (true)
				{
				next = SkeletonUtils.getMaxDirectionalNeighbor(dtArray, w, current,
						move);
				previous = current;
				current = next.x;
				move = next.y;

				if (dtArray[current]==1)
					{
					newBases.add(current);
					skPoints.add(current);
					isSkPoint[current] = true;

					if (diagDic[move]!=-1)
						{
						extra = SkeletonUtils.getNeighbor(previous, diagDic[move], w);
						skPoints.add(extra);
						isSkPoint[extra] = true;
						}
					break;
					}
				else if (dtArray[current]==0)
					{
					newBases.add(previous);
					skPoints.add(previous);
					isSkPoint[previous] = true;
					break;
					}
				// Add Path
				skPoints.add(current);
				isSkPoint[current] = true;

				if (diagDic[move]!=-1)
					{
					extra = SkeletonUtils.getNeighbor(previous, diagDic[move], w);
					skPoints.add(extra);
					isSkPoint[extra] = true;
					}
				}
			}
		// Set new bases points
		int b = 0;
		int base;
		bIt = newBases.iterator();
		while (bIt.hasNext())
			{
			base = bIt.next();
			isSkPoint[base] = true;
			basePoints.set(b, base);
			b++;
			}
		}

	
	
	}
