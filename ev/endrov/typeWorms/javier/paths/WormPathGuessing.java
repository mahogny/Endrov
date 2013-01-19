package endrov.typeWorms.javier.paths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import endrov.typeImageset.EvPixels;
import endrov.typeWorms.javier.WormPixelMatcher;
import endrov.typeWorms.javier.skeleton.NotWormException;
import endrov.typeWorms.javier.skeleton.SkeletonUtils;
import endrov.typeWorms.javier.skeleton.WormClusterSkeleton;
import endrov.typeWorms.javier.skeleton.WormSkeleton;
import endrov.util.math.Vector2i;

/**
 * Set of tools for heuristical guessing of most likely worm paths
 * 
 * @author Javier Fernandez
 *
 */
public class WormPathGuessing
	{

	/**
	 * Calculates the best path to follow starting from extreme or base points on
	 * the worm cluster wc and reaching another extreme or base point
	 * 
	 * @param wc
	 *          Worm cluster skeleton
	 * @param nSteps
	 *          Number of last steps stored from previous path
	 */

	public static ArrayList<ArrayList<Integer>> guessWormPaths(
			WormClusterSkeleton wc, int nSteps, WormPixelMatcher wpm,
			Vector2i minMaxWormLength)
		{
		ArrayList<ArrayList<Integer>> wormPaths = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> checkedBase = new ArrayList<Integer>();
		Iterator<Integer> it = wc.basePoints.iterator();
		// System.out.println("Number of bases"+ wc.basePoints.size());
		int base;
		boolean foundPath;
		ArrayList<Integer> noPathBases = new ArrayList<Integer>();
		HashSet<String> pathPairs = new HashSet<String>();
		while (it.hasNext())
			{
			base = it.next();
			if (!checkedBase.contains((Integer) base))
				{
				checkedBase.add(base);
				foundPath = traceBestPath(wc, base, nSteps, wormPaths, checkedBase,
						wc.basePoints, wpm, pathPairs, minMaxWormLength.y);
				if (!foundPath)
					{
					noPathBases.add(base);
					checkedBase.remove((Integer) base);
					}
				}
			else
				{
				// System.out.println("Skip base");
				}
			}
		/*
		 * Iterator<Integer> nit = noPathBases.iterator(); while(nit.hasNext()){
		 * base = nit.next(); if(!checkedBase.contains(base)){ ArrayList<Integer>
		 * newPath = tracePathAndCreateExtreme(wc, base, wc.basePoints, wpm,
		 * (int)(minMaxWormLength.y*0.8)); wormPaths.add(newPath);
		 * wc.basePoints.add(newPath.get(newPath.size()-1)); } }
		 */
		// Discard too short paths
		Iterator<ArrayList<Integer>> wit = wormPaths.iterator();
		int wormLength;
		while (wit.hasNext())
			{
			wormLength = SkeletonUtils.calculatePathLength(wit.next(), wc
					.getPixelMatcher());
			if (wormLength<minMaxWormLength.x)
				{
				wit.remove();
				}
			}

		return wormPaths;
		}

	/**
	 * Calculates the best path starting from an extreme pixel (base) of the worm
	 * cluster wc according to the next pixel heuristic implemented on
	 * getBestNeighbor.
	 * 
	 * @param wc
	 *          Worm cluster skeleton
	 * @param base
	 *          Base or extreme point of the worm cluster wc
	 * @param nSteps
	 *          Number of last steps stored from previous path
	 * @param wormPaths
	 * @param checkedBase
	 */

	public static boolean traceBestPath(WormClusterSkeleton wc, int base,
			int nSteps, ArrayList<ArrayList<Integer>> wormPaths,
			ArrayList<Integer> checkedBase, ArrayList<Integer> basePoints,
			WormPixelMatcher wpm, HashSet<String> pathPairs, int maxPathLength)
		{
		// System.out.println("Starting tracking: "+wpm.getPixelPos(base));
		ArrayList<Integer> currentPath = new ArrayList<Integer>();
		int[] directions = new int[4];
		int[] diagonalDirections = new int[4];
		int neigh[];
		int previous;
		int lastNCounter = nSteps-1;
		int[] lastMove =
			{ -1 };

		Queue<Vector2i> lastNSteps = new LinkedList<Vector2i>();
		Vector2i bestN = new Vector2i(base, -1);
		Vector2i aux;
		boolean reachedBase = false;
		boolean[] isPath = new boolean[wc.h*wc.w];

		// wormPaths.add(currentPath);
		currentPath.add(base);
		isPath[base] = true;
		previous = -1;
		int numSteps = 0;
		while (bestN.x!=-1&&!reachedBase&&numSteps<maxPathLength)
			{
			numSteps += 1;
			neigh = SkeletonUtils.getCrossNeighbors(bestN.x, wc.w);
			// aux =
			// bestHeuristicNeighbor(wc,neigh,directions,diagonalDirections,previous);
			aux = getBestNeighbor(wc, neigh, directions, previous, nSteps);
			previous = bestN.x;
			bestN = aux;

			if (bestN.x==-1||isPath[bestN.x])
				break;

			currentPath.add(bestN.x);
			isPath[bestN.x] = true;
			lastNCounter = updateDirections(directions, diagonalDirections,
					lastNSteps, lastMove, bestN, lastNCounter);
			reachedBase = wc.getIsBasePoint()[bestN.x];
			}
		if (numSteps==maxPathLength)
			return false;
		// If a base was reached then is marked and added to the path
		if (reachedBase = true)
			{
			// System.out.println("STOP AT: "+wpm.getPixelPos(bestN.x));
			// if(!checkedBase.contains((Integer)bestN.x)){
			// checkedBase.add(bestN.x);

			// Check member to avoid duplicate paths
			String pPair = WormPathUtils.pathToString(currentPath);
			if (!pathPairs.contains(pPair))
				{
				wormPaths.add(currentPath);
				pathPairs.add(pPair);
				// System.out.println("Added");
				return true;
				}
			// }
			}
		// If a base is not reached then the closest base (euclidean distance) to
		// the
		// last pixel is marked
		else
			{
			// System.out.println("Not base reached");
			int closest = -1;
			double bestD = Integer.MAX_VALUE;
			double d;
			int pbase;
			Iterator<Integer> bit = basePoints.iterator();
			while (bit.hasNext())
				{
				pbase = bit.next();
				// if (checkedBase.contains((Integer) pbase))
				// continue;
				d = WormPixelMatcher.calculatePixelDistance(previous, pbase, wpm);
				if (d<bestD)
					{
					bestD = d;
					closest = pbase;
					}
				}
			if (closest!=-1)
				{
				String pPair = WormPathUtils.pathToString(currentPath);
				if (!pathPairs.contains(pPair))
					{
					// checkedBase.add(closest);
					pathPairs.add(pPair);
					currentPath.add(closest);
					wormPaths.add(currentPath);
					return true;
					}
				}
			}
		return false;
		}

	/**
	 * Calculates the best path starting from an extreme pixel (base) of the worm
	 * cluster wc according to the next pixel heuristic implemented on
	 * getBestNeighbor, and stops after wormLength pixels are added to the path
	 * 
	 * @param wc
	 *          Worm cluster skeleton
	 * @param base
	 *          Base or extreme point of the worm cluster wc
	 * @param nSteps
	 *          Number of last steps stored from previous path
	 * @param wormPaths
	 * @param checkedBase
	 */

	public static ArrayList<Integer> tracePathAndCreateExtreme(
			WormClusterSkeleton wc, int base, ArrayList<Integer> basePoints,
			WormPixelMatcher wpm, int wormLength)
		{
		ArrayList<Integer> currentPath = new ArrayList<Integer>();
		int nSteps = (int) (wormLength*0.15);
		int[] directions = new int[4];
		int[] diagonalDirections = new int[4];
		int neigh[];
		int previous;
		int lastNCounter = nSteps-1;
		int[] lastMove =
			{ -1 };

		Queue<Vector2i> lastNSteps = new LinkedList<Vector2i>();
		Vector2i bestN = new Vector2i(base, -1);
		Vector2i aux;
		boolean[] isPath = new boolean[wpm.getH()*wpm.getW()];

		currentPath.add(base);
		isPath[base] = true;
		previous = -1;
		int numSteps = 0;
		while (numSteps<wormLength)
			{
			numSteps += 1;
			neigh = SkeletonUtils.getCrossNeighbors(bestN.x, wpm.getW());
			// aux =
			// bestHeuristicNeighbor(wc,neigh,directions,diagonalDirections,previous);
			aux = getBestNeighbor(wc, neigh, directions, previous, nSteps);
			previous = bestN.x;
			bestN = aux;

			if (bestN.x==-1||isPath[bestN.x])
				break;

			currentPath.add(bestN.x);
			isPath[bestN.x] = true;
			lastNCounter = updateDirections(directions, diagonalDirections,
					lastNSteps, lastMove, bestN, lastNCounter);
			}

		return currentPath;

		}

	public static Vector2i bestHeuristicNeighbor(WormClusterSkeleton wc,
			int[] neigh, int[] directions, int[] diagonal, int previous, int nSteps)
		{
		int[] neigh2;
		ArrayList<Vector2i> neighDirections = new ArrayList<Vector2i>();
		ArrayList<Vector2i> bestNeighbors = new ArrayList<Vector2i>();

		for (int n = 0; n<neigh.length; n++)
			{
			if (!wc.getIsSkPoint()[neigh[n]]||neigh[n]==previous)
				continue;

			neigh2 = SkeletonUtils.getCrossNeighbors(neigh[n], wc.w);
			Vector2i secondBest = getBestNeighbor(wc, neigh2, directions, neigh[n],
					nSteps);
			if (secondBest.x==-1)
				continue;

			neigh2 = SkeletonUtils.getCrossNeighbors(secondBest.x, wc.w);
			Vector2i thirdBest = getBestNeighbor(wc, neigh2, directions,
					secondBest.x, nSteps);

			switch (secondBest.y)
				{
				case 0:
					switch (thirdBest.y)
						{
						case 1:
							neighDirections.add(new Vector2i(0, -1));
							break;
						case 3:
							neighDirections.add(new Vector2i(3, -1));
							break;
						// case 0: neighDirections.add(new Vector2i(3,0));break;
						default:
							neighDirections.add(new Vector2i(-1, -1));
							break;
						}
					break;
				case 1:
					switch (thirdBest.y)
						{
						case 0:
							neighDirections.add(new Vector2i(0, -1));
							break;
						case 2:
							neighDirections.add(new Vector2i(1, -1));
							break;
						// case 1: neighDirections.add(new Vector2i(0,1));break;
						default:
							neighDirections.add(new Vector2i(-1, -1));
							break;
						}
					break;
				case 2:
					switch (thirdBest.y)
						{
						case 1:
							neighDirections.add(new Vector2i(1, -1));
							break;
						case 3:
							neighDirections.add(new Vector2i(2, -1));
							break;
						// case 2: neighDirections.add(new Vector2i(2,1));break;
						default:
							neighDirections.add(new Vector2i(-1, -1));
							break;
						}
					break;
				case 3:
					switch (thirdBest.y)
						{
						case 0:
							neighDirections.add(new Vector2i(3, -1));
							break;
						case 2:
							neighDirections.add(new Vector2i(2, -1));
							break;
						// case 3: neighDirections.add(new Vector2i(3,2));break;
						default:
							neighDirections.add(new Vector2i(-1, -1));
							break;
						}
					break;
				default:
					neighDirections.add(new Vector2i(-1, -1));
					break;
				}
			bestNeighbors.add(new Vector2i(neigh[n], n));
			}

		// Get max diagonal
		int maxD = Integer.MIN_VALUE;
		int maxI = -1;
		for (int d = 0; d<diagonal.length; d++)
			{
			if (maxD<diagonal[d])
				{
				maxD = diagonal[d];
				maxI = d;
				}
			}

		// Find neighbor which matches diagonal direction
		Iterator<Vector2i> it = neighDirections.iterator();
		int index = 0;
		Vector2i nextN;
		while (it.hasNext())
			{
			nextN = it.next();
			if (nextN.x==maxI)
				{

				if (directions[bestNeighbors.get(index).y]<maxD)
					{
					return bestNeighbors.get(index);
					}

				}
			else if (nextN.y==maxI)
				{
				if (directions[bestNeighbors.get(index).y]<maxD)
					{
					return bestNeighbors.get(index);
					}
				}
			index++;
			}
		// default value
		if (bestNeighbors.size()>0)
			{
			/*
			 * Iterator<Vector2i> it2 = bestNeighbors.iterator(); Vector2i bn = new
			 * Vector2i(-1,-1); Vector2i nb; while(it2.hasNext()){ nb = it2.next(); if
			 * (nb.x > bn.x){ bn = nb; } } return bn;
			 */

			}
		return getBestNeighbor(wc, neigh, directions, previous, nSteps);
		// return new Vector2i(-1,-1);
		}

	/**
	 * Calculates the next path pixel, finding the neighbor of the current pixel,
	 * preceded by previous, that maximizes the cost function
	 * directions[p]+dt[p]*heuristic, where directions contains the amount of
	 * times that any of the cross points have been chosen in the last nSteps
	 * steps in the currently build path. The cost function consist of the
	 * direction reliability value and the strength of the distance value for the
	 * next pixel. This will make the path tend to reach crossing points before
	 * turning.
	 * 
	 * @param wc
	 *          Worm cluster skeleton
	 * @param neigh
	 *          Neighbors of the current pixel
	 * @param directions
	 *          Accumulated directions in last nSteps steps
	 * @param previous
	 *          Previous pixel
	 * @param nSteps
	 *          Number of last steps stored from previous path
	 * @return best following neighbor
	 */

	public static Vector2i getBestNeighbor(WormClusterSkeleton wc, int[] neigh,
			int[] directions, int previous, int nSteps)
		{
		Vector2i best = new Vector2i(-1, -1);
		double max = Double.MIN_VALUE;
		double heuristic = 2;
		for (int i = 0; i<neigh.length; i++)
			{
			if (!wc.getIsSkPoint()[neigh[i]]||neigh[i]==previous)
				continue;
			if (max<(directions[i]+(wc.dt[neigh[i]])*heuristic))
				{
				max = directions[i]+(wc.dt[neigh[i]]*heuristic);
				best.x = neigh[i];
				best.y = i;
				}
			}
		return best;
		}

	/**
	 * Calculates and updates the values for the directions vector adding the
	 * lastMove direction and removing the first of the path if lastNSteps is
	 * filled, to maintain the recently walked path size
	 */

	public static int updateDirections(int[] directions, int[] diagonal,
			Queue<Vector2i> lastNSteps, int[] lastMove, Vector2i bestNeighbor,
			int lastNCounter)
		{

		if (lastNCounter>=0)
			{
			if (!lastNSteps.isEmpty())
				{
				updateDiagonal(diagonal, bestNeighbor.y, 1, lastMove[0]);
				lastMove[0] = bestNeighbor.y;
				}

			directions[bestNeighbor.y] += 1;
			lastNSteps.add(bestNeighbor);
			lastNCounter--;
			}
		else
			{
			directions[bestNeighbor.y] += 1;
			directions[lastNSteps.peek().y] -= 1;
			lastNSteps.poll(); // Remove Nsteps'th element

			updateDiagonal(diagonal, bestNeighbor.y, 1, lastMove[0]);
			lastMove[0] = bestNeighbor.y;

			lastNSteps.add(bestNeighbor);
			}
		return lastNCounter;
		}

	public static void updateDiagonal(int[] diagonal, int direction, int value,
			int lastStep)
		{
		switch (lastStep)
			{
			case 0:
				switch (direction)
					{
					case 1:
						diagonal[0] += value;
						break;
					case 3:
						diagonal[3] += value;
						break;
					}
				break;
			case 1:
				switch (direction)
					{
					case 0:
						diagonal[0] += value;
						break;
					case 2:
						diagonal[1] += value;
						break;
					}
				break;
			case 2:
				switch (direction)
					{
					case 1:
						diagonal[1] += value;
						break;
					case 3:
						diagonal[2] += value;
						break;
					}
				break;
			case 3:
				switch (direction)
					{
					case 0:
						diagonal[3] += value;
						break;
					case 2:
						diagonal[2] += value;
						break;
					}
				break;
			}
		}

	public static ArrayList<WormSkeleton> wormsFromPaths(EvPixels image,
			int[] dt, WormPixelMatcher wpm, ArrayList<ArrayList<Integer>> guessPaths)
		{
		ArrayList<WormSkeleton> wlist = new ArrayList<WormSkeleton>();
		Iterator<ArrayList<Integer>> wPaths = guessPaths.iterator();
		while (wPaths.hasNext())
			{
			ArrayList<Integer> wormPath = wPaths.next();
			ArrayList<Integer> baseP = new ArrayList<Integer>(2);
			baseP.add(wormPath.get(0));
			baseP.add(wormPath.get(wormPath.size()-1));

			WormSkeleton ws = null;
			try
				{
				ws = new WormSkeleton(image, dt, wpm.getW(), wpm.getH(), baseP,
						wormPath, wpm);
				}
			catch (NotWormException e)
				{
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			wlist.add(ws);
			}
		return wlist;
		}

	}
