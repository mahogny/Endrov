package endrov.worms.javier.skeleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import endrov.imageset.EvPixels;
import endrov.util.Vector2i;
import endrov.worms.javier.WormPixelMatcher;

/**
 * Class defining a morphological skeleton of a worm cluster
 * 
 * @author stormeagle
 *
 */

public final class WormClusterSkeleton extends Skeleton
	{
	public ArrayList<Integer> basePoints;
	ArrayList<Integer> skPoints;
	boolean[] isBasePoint; // added for efficient check
	boolean[] isSkPoint; // added for efficient check
	int numWorms;
	WormPixelMatcher wpm;

	/**
	 * Creates a instance of worm cluster skeleton, that is a skeleton of an image
	 * that could contain 1 or more overlapping worms. The number of worms are
	 * calculated as the half of the number of base points.
	 * 
	 * @param image
	 *          the image from which the skeleton is taken
	 * @param dt
	 *          a distance transformation of image
	 * @param w
	 *          the width of image
	 * @param h
	 *          the height of image
	 * @param basePoints
	 *          list of the base (extreme) points of the worms skeleton
	 * @param skPoints
	 *          list of the skeleton points
	 * @param isBasePoint
	 *          boolean matrix-like array that checks if a point is base
	 * @param isSkPoint
	 *          boolean matrix-like array that checks if a point is skeleton
	 */
	public WormClusterSkeleton(EvPixels image, int[] dt, int w, int h,
			ArrayList<Integer> basePoints, ArrayList<Integer> skPoints,
			boolean[] isBasePoint, boolean[] isSkPoint, WormPixelMatcher wpm)
		{

		super(image, dt, w, h);
		this.basePoints = new ArrayList<Integer>(basePoints);
		this.skPoints = new ArrayList<Integer>(skPoints);

		this.isBasePoint = new boolean[isBasePoint.length]; // could be unnecessary
		for (int i = 0; i<this.isBasePoint.length; i++)
			this.isBasePoint[i] = isBasePoint[i];

		this.isSkPoint = new boolean[isSkPoint.length];
		for (int i = 0; i<this.isSkPoint.length; i++)
			this.isSkPoint[i] = isSkPoint[i];
		numWorms = basePoints.size()/2;
		if (basePoints.size()%2!=0)
			{
			numWorms += 1;
			}
		this.wpm = wpm;
		}

	/**
	 * Creates a instance of worm cluster skeleton, that is a skeleton of an image
	 * that could contain 1 or more overlapping worms. The number of worms are
	 * calculated as the half of the number of base points.
	 * 
	 * @param image
	 *          the image from which the skeleton is taken
	 * @param dt
	 *          a distance transformation of image
	 * @param w
	 *          the width of image
	 * @param h
	 *          the height of image
	 * @param basePoints
	 *          list of the base (extreme) points of the worms skeleton
	 * @param skPoints
	 *          list of the skeleton points
	 */

	public WormClusterSkeleton(EvPixels image, int[] dt, int w, int h,
			ArrayList<Integer> basePoints, ArrayList<Integer> skPoints,
			WormPixelMatcher wpm)
		{

		super(image, dt, w, h);
		this.basePoints = new ArrayList<Integer>(basePoints);
		this.skPoints = new ArrayList<Integer>(skPoints);
		this.isBasePoint = SkeletonUtils.listToMatrix(w*h, basePoints); // could be
		// unnecessary
		this.isSkPoint = SkeletonUtils.listToMatrix(w*h, skPoints);
		numWorms = basePoints.size()/2;
		if (basePoints.size()%2!=0)
			{
			numWorms += 1;
			}
		this.wpm = wpm;
		}

	public ArrayList<Integer> getBasePoints()
		{
		return basePoints;
		}

	public ArrayList<Integer> getSkPoints()
		{
		return skPoints;
		}

	public boolean[] getIsBasePoint()
		{
		return isBasePoint;
		}

	public boolean[] getIsSkPoint()
		{
		return isSkPoint;
		}

	public int getNumWorms()
		{
		return numWorms;
		}

	public WormPixelMatcher getPixelMatcher()
		{
		return wpm;
		}

	/**
	 * Add each extra base to base list if is not contained previously
	 */
	public void addExtraBases(ArrayList<Integer> extraBases)
		{
		Iterator<Integer> it = extraBases.iterator();
		int base;
		while (it.hasNext())
			{
			base = it.next();
			if ((!basePoints.contains((Integer) base)))
				{
				basePoints.add(base);
				isBasePoint[base] = true;
				if (!isSkPoint[base])
					{
					isSkPoint[base] = true;
					skPoints.add(base);
					}
				}
			}
		// recalculate number of worms
		numWorms = basePoints.size()/2;
		if (basePoints.size()%2!=0)
			{
			numWorms += 1;
			}
		}

	/**
	 * Add each extra skeleton points
	 */
	public void addExtraSk(ArrayList<Integer> extraSk)
		{
		for (int skp : extraSk)
			{
			isSkPoint[skp] = true;
			skPoints.add(skp);
			}
		}

	/**
	 * Delete each base from base list if is not contained previously
	 */
	public void deleteExtraBases(ArrayList<Integer> extraBases)
		{
		Iterator<Integer> it = extraBases.iterator();
		int pixel;
		while (it.hasNext())
			{
			pixel = it.next();
			if (isSkPoint[pixel])
				{
				isSkPoint[pixel] = false;
				skPoints.remove((Integer) pixel);
				if (isBasePoint[pixel])
					{
					isBasePoint[pixel] = false;
					basePoints.remove((Integer) pixel);
					}
				}
			}
		}

	/**
	 * Returns the paths that most likely describe the worms of the calling worm
	 * cluster following the directional neighbors starting from the given base
	 * points.
	 */
	private ArrayList<ArrayList<Integer>> getAppWormPaths()
		{
		ArrayList<Integer> baseCopy = new ArrayList<Integer>(basePoints);
		Iterator<Integer> bIt = baseCopy.iterator();
		ArrayList<ArrayList<Integer>> wormPaths = new ArrayList<ArrayList<Integer>>(
				0);
		ArrayList<Integer> markedBases = new ArrayList<Integer>();
		int[] imageArray = image.getArrayInt();

		int base;
		int next = -1;
		int move = -1;
		int[] neigh;
		int prev;
		int auxPrev;
		int crossPixel;
		Vector2i[] crossN;
		Vector2i max;

		while (bIt.hasNext())
			{
			base = bIt.next();
			if (markedBases.contains((Integer) base))
				continue;

			ArrayList<Integer> newPath = new ArrayList<Integer>();
			newPath.add(base);
			neigh = SkeletonUtils.getCircularNeighbors(base, w); // Thinning gives
			// cross path only
			for (int n = 0; n<8; n++)
				{
				if (isSkPoint[neigh[n]])
					{
					next = neigh[n];
					move = n;
					break;
					}
				}
			// Start walking to base point
			prev = base;
			while (next!=-1&&!isBasePoint[next])
				{
				// System.out.println("Looping");
				newPath.add(next);

				max = SkeletonUtils.getMaxDirectionalNeighbor(imageArray, isSkPoint, w,
						next, move);
				auxPrev = next;

				// REFINATE THIS STEP
				if (max.x==-1)
					{// Find best cross Neighbor, not in return way
					crossN = SkeletonUtils.getCrossNeighborsDir(next, w);
					int maxCross = -1;
					for (int i = 0; i<4; i++)
						{
						crossPixel = crossN[i].x;
						if (isSkPoint[crossPixel]&&crossPixel!=prev
								&&(imageArray[crossPixel])>maxCross)
							{
							maxCross = imageArray[crossPixel];
							next = crossPixel;
							move = crossN[i].y;
							}
						}
					prev = auxPrev;
					if (maxCross==-1)
						next = -1;
					}
				else
					{
					prev = next;
					next = max.x;
					move = max.y;
					}
				}
			if (next!=-1)
				{
				newPath.add(next);
				markedBases.add(next);
				wormPaths.add(newPath);
				}
			}
		return wormPaths;
		}

	public static ArrayList<WormSkeleton> getIsolatedWorms(
			ArrayList<WormClusterSkeleton> warray, WormPixelMatcher wpm)
		{

		ArrayList<WormSkeleton> wormList = new ArrayList<WormSkeleton>();
		ArrayList<WormSkeleton> isolatedWormList = new ArrayList<WormSkeleton>();
		for (int j = 0; j<warray.size(); j++)
			{
			WormClusterSkeleton wc = warray.get(j);
			if (wc.getNumWorms()!=1)
				continue;
			WormSkeleton ws = null;
			try
				{
				ws = new WormSkeleton(wc, wpm);
				}
			catch (NotWormException e)
				{
				System.out.println("EXCEPTION ");
				e.printStackTrace();
				}
			wormList.add(ws);
			}

		// Calculate average length and get only those who are between the 30% under
		// average
		// and 30% above average values

		Iterator<WormSkeleton> wIt = wormList.iterator();
		ArrayList<Integer> lengthList = new ArrayList<Integer>();
		
		while (wIt.hasNext())
			{
			lengthList.add(wIt.next().getSkPoints().size());
			}
		Collections.sort(lengthList);
		// Take out the smallest and biggest value
		int lsize = lengthList.size();
		if (lsize>2)
			{
			lengthList.remove(0);
			lengthList.remove(lsize-2);
			}
		else
			return wormList;

		int average = 0;
		Iterator<Integer> lit = lengthList.iterator();
		while (lit.hasNext())
			{
			average += lit.next();
			}
		average = average/(lsize-2);

		System.out.println("AVG: "+average);
		wIt = wormList.iterator();
		WormSkeleton ws = null;
		double wsSize = -1;
		while (wIt.hasNext())
			{
			ws = wIt.next();
			wsSize = ws.getSkPoints().size();
			System.out.println("WSIZE: "+wsSize);
			if (wsSize<=(double) 1.30*average&&wsSize>=(double) 0.70*average)
				{
				isolatedWormList.add(ws);
				}
			}
		return isolatedWormList;
		}

	}
