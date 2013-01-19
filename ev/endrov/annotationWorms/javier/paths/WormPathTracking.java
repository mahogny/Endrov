package endrov.annotationWorms.javier.paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import endrov.annotationWorms.javier.WormPixelMatcher;
import endrov.annotationWorms.javier.skeleton.SkeletonUtils;
import endrov.annotationWorms.javier.skeleton.WormClusterSkeleton;
import endrov.annotationWorms.javier.skeleton.WormSkeleton;
import endrov.util.Vector2i;

/**
 * Set of tools for tracking and analysis of paths belonging to worm skeletons.
 * 
 * @author Javier Fernandez
 *
 */
public class WormPathTracking
	{

	/**
	 * Explores the connected path starting from pixel and following every
	 * neighbor indexing the matching with isoCount.
	 * 
	 * @param matching
	 *          matrix-like array that contains the isolation index for each
	 *          pixel. The current pixel will be modified once in each call
	 * @param isBase
	 *          Matrix-like array, true for every pixel that is base point
	 * @param isSkeleton
	 *          Matrix-like array, true for every pixel that belongs to the
	 *          skeleton
	 * @param w
	 *          Width of the image matrix represented in isSkeleton
	 * @param pixel
	 *          current expanding pixel
	 * @param isoCount
	 *          isolation index corresponding to pixel
	 */
	public static void pathToBase(int[] matching, boolean[] isBase,
			boolean[] isSkeleton, int w, int pixel, int isoCount,
			ArrayList<Integer> currentSkPoints, ArrayList<Integer> currentBasePoints)
		{
		int[] neighbors;
		if (matching[pixel]!=0) // the pixel has already been checked
			return;
		matching[pixel] = isoCount;
		currentSkPoints.add(pixel);
		if (isBase[pixel])
			currentBasePoints.add(pixel);

		neighbors = SkeletonUtils.getCircularNeighbors(pixel, w);
		for (int i = 0; i<8; i++)
			{
			if (isSkeleton[neighbors[i]])
				{// Follow every path recursively
				pathToBase(matching, isBase, isSkeleton, w, neighbors[i], isoCount,
						currentSkPoints, currentBasePoints);
				}
			}
		}

	private static void pathBranching(int[] matching, boolean[] isBase,
			boolean[] isSkeleton, int w, int pixel,
			ArrayList<ArrayList<Integer>> paths, ArrayList<Integer> currentSkPoints,
			int maxWormLength, int currentLength)
		{
		if (currentLength>=maxWormLength)
			{
			paths.remove(currentSkPoints);
			return;
			}

		int[] neighbors;
		// Circular path
		if (matching[pixel]!=0)
			{ // the pixel has already been checked
			paths.remove(currentSkPoints);
			return;
			}
		matching[pixel] = 1;
		currentSkPoints.add(pixel);
		// Created because currentSkPoints can be modified recursively
		ArrayList<Integer> callingPath = new ArrayList<Integer>(currentSkPoints);

		boolean createBranch = false;
		// neighbors = SkeletonUtils.getCircularNeighbors(pixel, w);
		neighbors = SkeletonUtils.getCrossNeighbors(pixel, w);
		for (int i = 0; i<4; i++)
			{
			if (isSkeleton[neighbors[i]]&&matching[neighbors[i]]==0)
				{// Follow every path recursively
				if (createBranch)
					{
					ArrayList<Integer> newPath = new ArrayList<Integer>(callingPath);
					paths.add(newPath);
					int[] newMatching = matching.clone();

					pathBranching(newMatching, isBase, isSkeleton, w, neighbors[i],
							paths, newPath, maxWormLength, currentLength+1);
					}
				else
					{
					pathBranching(matching, isBase, isSkeleton, w, neighbors[i], paths,
							currentSkPoints, maxWormLength, currentLength+1);
					createBranch = true;

					}
				}
			}
		}

	/**
	 * Retrieves all the paths that can be follow starting from a base point in a
	 * worm cluster. If complete true then more than 1 different path between two
	 * base points are allowed. Otherwise just the first found path is returned
	 * 
	 * @param wc
	 * @param wormLength
	 * @param complete
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> getAllPaths(
			WormClusterSkeleton wc, int wormLength, boolean complete)
		{
		ArrayList<ArrayList<Integer>> allPaths = new ArrayList<ArrayList<Integer>>();
		Vector2i minMaxWormLength = WormSkeleton.getMinMaxLength(wormLength, 0.70,
				1.5);

		// Calculate all paths from every base point
		Iterator<Integer> bit = wc.basePoints.iterator();
		int base;
		while (bit.hasNext())
			{
			base = bit.next();
			int[] matching = new int[wc.h*wc.w];
			ArrayList<Integer> newPath = new ArrayList<Integer>();
			allPaths.add(newPath);
			newPath.add(base);
			pathBranching(matching, wc.getIsBasePoint(), wc.getIsSkPoint(), wc.w,
					base, allPaths, newPath, minMaxWormLength.y, 0);
			}

		// Delete repeated paths
		Iterator<ArrayList<Integer>> pit;
		ArrayList<Integer> auxPath = new ArrayList<Integer>();
		Hashtable<String, ArrayList<ArrayList<Integer>>> pairPaths = new Hashtable<String, ArrayList<ArrayList<Integer>>>();
		HashSet<String> pathPairs = new HashSet<String>();

		if (complete)
			{
			pit = allPaths.iterator();
			while (pit.hasNext())
				{
				auxPath = pit.next();
				String pPair = WormPathUtils.pathToString(auxPath);
				if (!pathPairs.contains(pPair))
					{
					ArrayList<ArrayList<Integer>> newPathList = new ArrayList<ArrayList<Integer>>();
					newPathList.add(auxPath);
					pairPaths.put(pPair, newPathList);
					pathPairs.add(pPair);
					}
				else
					{
					ArrayList<ArrayList<Integer>> pathList = new ArrayList<ArrayList<Integer>>();
					pathList = pairPaths.get((String) pPair);
					boolean equal = false;
					for (ArrayList<Integer> p : pathList)
						{
						if (equal==true)
							break;
						if (p.get(0)!=auxPath.get(0))
							{
							Collections.reverse(p);
							}
						Iterator<Integer> pit2 = p.iterator();
						Iterator<Integer> ait = auxPath.iterator();
						equal = true;
						if (p.size()!=auxPath.size())
							equal = false;
						while (pit2.hasNext()&&equal==true)
							{
							if (pit2.next()!=ait.next())
								{
								equal = false;
								}
							}
						}
					if (equal)
						{
						pit.remove();
						}
					else
						{
						pathList.add(auxPath);
						}
					}
				}
			}
		else
			{
			// Delete repeated paths
			pit = allPaths.iterator();
			auxPath = new ArrayList<Integer>();
			while (pit.hasNext())
				{
				auxPath = pit.next();
				String pPair = WormPathUtils.pathToString(auxPath);
				// System.out.print("PPAR: "+pPair+" ");
				if (!pathPairs.contains(pPair))
					{
					pathPairs.add(pPair);
					// System.out.println(" OK");
					}
				else
					{
					// System.out.println(" REMOVE Repeated");
					pit.remove();
					}
				}

			}

		// Discard too long and short paths, and extend paths that do not reach base
		// points if possible
		pit = allPaths.iterator();
		ArrayList<Integer> path;
		while (pit.hasNext())
			{
			path = pit.next();
			int pathSize = SkeletonUtils.calculatePathLength(path, wc
					.getPixelMatcher());
			if (pathSize<minMaxWormLength.x||pathSize>minMaxWormLength.y)
				{
				// System.out.println("Path to long to short "+pathSize+" "+minMaxWormLength);
				pit.remove();
				}
			// Check if reaches base point
			else
				{
				int lastPoint = path.get(path.size()-1);
				if (wc.basePoints.contains((Integer) lastPoint))
					continue;
				else
					{
					// check if is close to a base point
					int closest = -1;
					double bestD = Integer.MAX_VALUE;
					double d;
					int pbase;
					bit = wc.basePoints.iterator();
					while (bit.hasNext())
						{
						pbase = bit.next();
						// if (checkedBase.contains((Integer) pbase))
						// continue;
						d = WormPixelMatcher.calculatePixelDistance(lastPoint, pbase, wc
								.getPixelMatcher());
						if (d<bestD&&d<3)
							{
							bestD = d;
							closest = pbase;
							}
						}
					if (closest!=-1)
						{
						// The path has not been added previously
						path.add(closest);
						String pPair = WormPathUtils.pathToString(path);
						if (!pathPairs.contains(pPair))
							{
							// checkedBase.add(closest);
							pathPairs.add(pPair);
							}
						else
							{
							pit.remove();
							}
						}
					else
						{
						pit.remove();
						}
					}
				}
			}

		return allPaths;
		}

	}
