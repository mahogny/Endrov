package endrov.worms.fit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

//import endrov.quickhull3d.Vector3d;
import javax.vecmath.Vector3d;
import endrov.util.Vector2i;
import endrov.worms.WormPixelMatcher;
import endrov.worms.WormShape;
import endrov.worms.skeleton.WormClusterSkeleton;
import endrov.worms.utils.greedyNonBipartiteAssignment;


/**
 * Utils for worm fitting methods
 * @author Javier Fernandez
 *
 */
public class WormFitUtils
	{

	/**
	 * Filter the table of possible matchings finding the lowest cost assignment
	 * the maximizes the number of base points taken. Follows a greedy algorithm
	 * for lowest total cost assignment
	 */

	public static ArrayList<WormShape> filterFittingDictionary(
			Hashtable<Integer, ArrayList<Vector3d>> fitDic,
			ArrayList<WormShape> matchedShapes, ArrayList<Integer> matchedIndex,
			WormClusterSkeleton wc, WormPixelMatcher wpm, int wormLength,
			ArrayList<Integer> notAssignedBases)
		{

		ArrayList<WormShape> clusterBestMatch = new ArrayList<WormShape>();
		ArrayList<Integer> basePoints = wc.getBasePoints();

		HashSet<Integer> nonConflictingShapes = new HashSet<Integer>();
		HashSet<Integer> nonConflictingBases = new HashSet<Integer>();
		ArrayList<Integer> conflictingBases = detectConflictingBases(fitDic,
				basePoints, nonConflictingShapes, nonConflictingBases);

		HashSet<Integer> assignedBases = new HashSet<Integer>();
		ArrayList<Integer> shapeList = resolveConflict(conflictingBases, fitDic,
				basePoints, assignedBases);
		assignedBases.addAll(nonConflictingBases);

		notAssignedBases
				.addAll(identifyNotAssignedBases(assignedBases, basePoints));

		// Add conflict resolved shapes to the final match list
		Iterator<Integer> shapeIt = shapeList.iterator();
		int shapeIndex;
		while (shapeIt.hasNext())
			{
			shapeIndex = shapeIt.next();
			matchedIndex.add(shapeIndex);
			clusterBestMatch.add(matchedShapes.get(shapeIndex));
			}

		// Add non conflicting shapes to the final match list
		shapeIt = nonConflictingShapes.iterator();
		while (shapeIt.hasNext())
			{
			shapeIndex = shapeIt.next();
			matchedIndex.add(shapeIndex);
			clusterBestMatch.add(matchedShapes.get(shapeIndex));
			}

		return new ArrayList<WormShape>(clusterBestMatch);
		}

	private static ArrayList<Integer> resolveConflict(
			ArrayList<Integer> conflictingBases,
			Hashtable<Integer, ArrayList<Vector3d>> fitDic,
			ArrayList<Integer> basePoints, HashSet<Integer> assignedBases)
		{

		ArrayList<Integer> shapeList = new ArrayList<Integer>();

		// create base point index array
		Hashtable<Integer, Integer> baseHash = new Hashtable<Integer, Integer>();
		Hashtable<Integer, Integer> baseReverse = new Hashtable<Integer, Integer>();
		for (int bi = 0; bi<conflictingBases.size(); bi++)
			{
			baseHash.put(conflictingBases.get(bi), bi);
			baseReverse.put(bi, conflictingBases.get(bi));
			}
		// Create matching matrix
		double[][] matchMatrix = new double[conflictingBases.size()][conflictingBases
				.size()];
		for (int i = 0; i<matchMatrix.length; i++)
			{
			for (int j = 0; j<matchMatrix.length; j++)
				{
				matchMatrix[i][j] = -1;
				}
			}

		ArrayList<Vector3d> matches;
		Iterator<Vector3d> it3;
		Vector3d match;
		Integer secondBase;
		int base;
		int shapeIndex;
		for (int i = 0; i<conflictingBases.size(); i++)
			{
			base = conflictingBases.get(i);
			matches = fitDic.get(base);
			if (matches==null)
				{
				continue;
				}
			int firstBaseIndex = baseHash.get(base);
			it3 = matches.iterator();
			while (it3.hasNext())
				{
				match = it3.next();
				shapeIndex = (int) match.z;
				secondBase = baseHash.get((int) match.x);
				// Discard paths that reach non conflicting bases
				if (secondBase==null)
					continue;

				matchMatrix[firstBaseIndex][secondBase] = match.y;
				matchMatrix[secondBase][firstBaseIndex] = match.y;
				}
			}

		ArrayList<Vector2i> bestAssignment = greedyNonBipartiteAssignment
				.findBestAssignment(matchMatrix);
		// System.out.println("Best Assignment WAS)");
		for (Vector2i v : bestAssignment)
			{
			// System.out.println(v);
			}

		Iterator<Vector2i> bit = bestAssignment.iterator();
		Vector2i assignment;
		while (bit.hasNext())
			{
			assignment = bit.next();
			matches = fitDic.get(baseReverse.get(assignment.x));
			int otherBase;
			if (matches==null)
				{
				matches = fitDic.get(baseReverse.get(assignment.y));
				if (matches==null)
					{
					continue;
					}
				otherBase = baseReverse.get(assignment.x);
				}
			else
				{
				otherBase = baseReverse.get(assignment.y);
				}
			it3 = matches.iterator();
			while (it3.hasNext())
				{
				match = it3.next();
				if (((int) match.x)==otherBase)
					{
					assignedBases.add(baseReverse.get(assignment.x));
					assignedBases.add(baseReverse.get(assignment.y));
					shapeList.add((int) match.z);
					// System.out.println("ASSIGNING SHAPE MATCHING NUMBER: "+(int)match.z);
					break;
					}
				}

			}
		return shapeList;
		}

	/**
	 * Calculates the bases whose best match conflicts with another best match
	 * base.
	 */
	private static ArrayList<Integer> detectConflictingBases(
			Hashtable<Integer, ArrayList<Vector3d>> fitDic, ArrayList<Integer> bases,
			HashSet<Integer> nonConflictingShapes,
			HashSet<Integer> nonConflictingBases)
		{

		Iterator<Integer> bit = bases.iterator();
		int base;
		ArrayList<Vector3d> matches;
		Iterator<Vector3d> it3;
		Vector3d match;
		Vector3d bestMatch = null;
		double min;
		// int bestMatchIndex;
		// int bestShapeIndex;
		HashSet<Integer> conflictingBases = new HashSet<Integer>();
		Hashtable<Integer, HashSet<Integer>> pathsPerBase = new Hashtable<Integer, HashSet<Integer>>();

		// Indexes of shape per base. Just an integer required not list
		Hashtable<Integer, Integer> indexPerBase = new Hashtable<Integer, Integer>();

		while (bit.hasNext())
			{
			base = bit.next();
			matches = fitDic.get(base);
			if (matches!=null)
				{
				min = Double.MAX_VALUE;
				// bestMatchIndex = -1;
				// bestShapeIndex = -1;
				it3 = matches.iterator();
				while (it3.hasNext())
					{
					match = it3.next();
					if (min>match.y)
						{
						// bestMatchIndex = (int) match.x;
						min = match.y;
						// bestShapeIndex = (int)match.z;
						bestMatch = match;
						}
					}
				HashSet<Integer> basePaths = pathsPerBase.get(base);
				if (basePaths==null)
					{
					basePaths = new HashSet<Integer>();
					pathsPerBase.put(base, basePaths);
					}
				basePaths.add((int) bestMatch.x);
				indexPerBase.put(base, (int) bestMatch.z);

				basePaths = pathsPerBase.get((int) bestMatch.x);
				if (basePaths==null)
					{
					basePaths = new HashSet<Integer>();
					pathsPerBase.put((int) bestMatch.x, basePaths);
					}
				basePaths.add(base);
				indexPerBase.put((int) bestMatch.x, (int) bestMatch.z);
				}
			}

		bit = bases.iterator();
		HashSet basePaths = new HashSet<Integer>();
		while (bit.hasNext())
			{
			base = bit.next();
			basePaths = pathsPerBase.get(base);
			if (basePaths==null)
				continue;
			// If there are more than 1 path for a base point add all the involved
			// bases
			if (basePaths.size()>1)
				{

				Iterator<Integer> pit = basePaths.iterator();
				while (pit.hasNext())
					{
					conflictingBases.add(pit.next());
					}
				conflictingBases.add(base);
				}
			}
		bit = bases.iterator();
		while (bit.hasNext())
			{
			base = bit.next();
			if (!conflictingBases.contains(base))
				{
				Integer shapeIndex = indexPerBase.get(base);
				if (shapeIndex!=null)
					{
					nonConflictingShapes.add(shapeIndex);
					nonConflictingBases.add(base);
					}
				}
			}

		return new ArrayList<Integer>(conflictingBases);

		}

	/**
	 *Identifies the bases that do not have a detected path on the fitting
	 * dictionary
	 */
	public static ArrayList<Integer> identifyNonPathBases(
			Hashtable<Integer, ArrayList<Vector3d>> fitDic, ArrayList<Integer> bases)
		{

		ArrayList<Integer> baseList = new ArrayList<Integer>();
		Iterator<Integer> bit = baseList.iterator();
		int base;
		while (bit.hasNext())
			{
			base = bit.next();
			if (!fitDic.containsKey(base))
				{
				baseList.add(base);
				}
			}
		return baseList;
		}

	/**
	 *Identifies the bases that do not have a detected path on the fitting
	 * dictionary
	 */
	public static ArrayList<Integer> identifyNotAssignedBases(
			HashSet<Integer> assignedBases, ArrayList<Integer> baseList)
		{
		ArrayList<Integer> notAssignedBases = new ArrayList<Integer>();
		Iterator<Integer> it = baseList.iterator();
		int base;
		while (it.hasNext())
			{
			base = it.next();
			if (!assignedBases.contains(base))
				{
				notAssignedBases.add(base);
				}
			}
		return notAssignedBases;
		}

	/**
	 * Prints to standard output the fitting dictionary
	 */
	public static void printFitDic(
			Hashtable<Integer, ArrayList<Vector3d>> fittingDic, WormClusterSkeleton wc)
		{
		Iterator<Integer> bit = wc.getBasePoints().iterator();
		Iterator<Vector3d> lit;
		ArrayList<Vector3d> mlist;
		Vector3d rec;
		int base;
		System.out.println("PRINTING MATCH DICTIONARY");
		while (bit.hasNext())
			{
			base = bit.next();
			System.out.println("For Base: "+base);
			mlist = fittingDic.get(base);
			if (mlist==null)
				continue;

			lit = mlist.iterator();
			while (lit.hasNext())
				{
				rec = lit.next();
				System.out.println("       --> "+rec);
				}
			}
		}

	}
