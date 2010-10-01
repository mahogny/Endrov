package endrov.worms.fit;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.Hashtable;

import com.graphbuilder.curve.CardinalSpline;

import endrov.imageset.EvPixels;
import javax.vecmath.Vector3d;

import endrov.util.curves.EvCardinalSpline;
import endrov.worms.WormDescriptor;
import endrov.worms.WormPixelMatcher;
import endrov.worms.WormProfile;
import endrov.worms.WormShape;
import endrov.worms.paths.WormPathGuessing;
import endrov.worms.skeleton.NotWormException;
import endrov.worms.skeleton.SkeletonTransform;
import endrov.worms.skeleton.WormClusterSkeleton;
import endrov.worms.skeleton.WormSkeleton;

/**
 * Class defining a series of methods to fit the shape of worms 
 * in digital images, once having the skeleton conformation
 * and the distribution of the worms in the image. 
 * 
 * @author Javier Fernandez
 *
 */
public class WormShapeFitting
	{

	/**
	 * Given a skeleton of an isolated worm, the corresponding contour
	 * is traced.
	 */
	public static ArrayList<Integer> fitIsolatedWorm(WormClusterSkeleton wc,
			WormPixelMatcher wpm, WormProfile wprof, int[] dtArray,
			int minSkeletonLength)
		{
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

		ArrayList<Integer> wormContour = SkeletonTransform.getShapeContour(wc,
				minSkeletonLength);
		wormContour = WormShape.ensureCounterClockwise(wormContour, wprof, ws,
				dtArray);
		return wormContour;
		}
/**
 * Calculates the set of possible worm conformations that can be detected
 * in the worm cluster 'wc', and returns a matching dictionary that assigns
 * to every worm endpoint belonging to the cluster, the list of feasible worm
 * conformations starting from the corresponding endpoint.
 * 
 * A worm conformation is represented as a 3-tuple of double values, this way:
 * <other-endpoint, opt-energy-value, conformation-index>. Where other-endpoint is
 * the opposite worm endpoint. Opt-energy-value is the value of the objective 
 * function that corresponds to the conformation. And, conformation-index is the
 * index of the conformation in the list of accumulated conformations. 
 */
	public static Hashtable<Integer, ArrayList<Vector3d>> fitWormCluster(
			WormShapeOptimization optMethod, WormClusterSkeleton wc,
			WormProfile wprof, int[] dtArray, EvPixels inputImage, int wormLength,
			ArrayList<WormShape> matchedShapes, double guessPower)
		{

		//ArrayList<ArrayList<Integer>> shapeList = new ArrayList<ArrayList<Integer>>();
		WormPixelMatcher wpm = wprof.wpm;

		// Guessing worm paths
		ArrayList<WormSkeleton> skList = WormPathGuessing.wormsFromPaths(
				inputImage, dtArray, wpm, WormPathGuessing.guessWormPaths(wc,
						(int) (wormLength*0.15), wpm, WormSkeleton
								.getMinMaxLength(wormLength)));

		// Calculate all possible paths (currently unable to improve speed)

		// skList = new ArrayList<WormSkeleton>();
		/*
		 * ArrayList<WormSkeleton> allList = WormPathGuessing.wormsFromPaths(
		 * inputImage, dtArray, wpm,
		 * WormPathTracking.getAllPaths(wc,wormLength,true));
		 */
		ArrayList<WormSkeleton> allList = new ArrayList<WormSkeleton>();
		Hashtable<Integer, ArrayList<WormSkeleton>> allPathDic = WormShapeFitting
				.buildPathDictionary(allList);

		Iterator<WormSkeleton> wit = skList.iterator();
		int count = 0;
		int index = matchedShapes.size(); 
		
		// For every base a list containing receiving base, optimization value and
		// matched array position is stored
		Hashtable<Integer, ArrayList<Vector3d>> matchDic = new Hashtable<Integer, ArrayList<Vector3d>>();
		boolean result;
		while (wit.hasNext())
			{
			// Optimize WormSkeleton shape
			count += 1;
			WormSkeleton ws = wit.next();
			result = skeletonMatchingOpt(optMethod, ws, wprof, dtArray, wpm,
					matchedShapes, matchDic, index, true, guessPower);
			if (result==true)
				index++;
			}
		// Find the base points that have no paths and all the possible paths
		ArrayList<Integer> basePoints = wc.getBasePoints();
		Iterator<Integer> bit = basePoints.iterator();
		int base;
		ArrayList<WormSkeleton> forgottenPaths = new ArrayList<WormSkeleton>();
		while (bit.hasNext())
			{
			base = bit.next();
			if (!matchDic.contains(base))
				{
				ArrayList<WormSkeleton> allBasePath = allPathDic.get(base);
				if (allBasePath!=null)
					{
					forgottenPaths.addAll(allBasePath);
					}
				}
			}
		// Match the shape for the paths of forgotten base points
		wit = forgottenPaths.iterator();
		count = 0;
		while (wit.hasNext())
			{
			// Optimize WormSkeleton shape
			// System.out.println();
			count += 1;
			WormSkeleton ws = wit.next();
			result = skeletonMatchingOpt(optMethod, ws, wprof, dtArray, wpm,
					matchedShapes, matchDic, index, false, guessPower);
			if (result==true)
				index++;
			}

		return matchDic;
		// return matchedShapes;
		};

	/**
	 * Calculates the best conformation corresponding to 'ws' and 'wprof' following
	 * the bendingOptimization(...) method, and adds it to the 
	 * dictionary of matches.
	 */
		private static boolean skeletonMatchingOpt(WormShapeOptimization optMethod,
			WormSkeleton ws, WormProfile wprof, int[] dtArray, WormPixelMatcher wpm,
			ArrayList<WormShape> matchedShapes,
			Hashtable<Integer, ArrayList<Vector3d>> matchDic, int index,
			boolean isGuessedPath, double guessPower)
		{

		// Optimize WormSkeleton shape
		double[] optValue =
			{ -1 };
		// System.out.println("Starting bending");
		ArrayList<Integer> rastShape = bendingOptimization(optMethod, ws, wprof,
				dtArray, optValue);
		if (rastShape==null)
			return false;

		// Preference is given to guessed paths to improve their matching value
		if (isGuessedPath)
			{
			optValue[0] = (optValue[0]*guessPower);
			}

		WormShape worm = new WormShape(rastShape, wpm, false, dtArray);
		matchedShapes.add(worm);
		// System.out.println("Finish bending");

		int base1 = ws.getSkPoints().get(0);
		int base2 = ws.getSkPoints().get(ws.getSkPoints().size()-1);

		Vector3d newMatch = new Vector3d((double) base2, optValue[0],
				(double) index);
		Iterator<Vector3d> vit;
		Vector3d next;
		ArrayList<Vector3d> list = (ArrayList<Vector3d>) matchDic.get(base1);
		if (list!=null)
			{
			boolean isNew = true;
			vit = list.iterator();
			while (vit.hasNext())
				{
				next = vit.next();
				if (next.x==newMatch.x)
					{
					isNew = false;
					if (newMatch.y<next.y)
						{
						// list.remove((Vector3d) next);
						vit.remove();
						list.add(newMatch);
						break;
						}
					}
				}
			if (isNew)
				{
				list.add(newMatch);
				}

			}
		else
			{
			ArrayList<Vector3d> newList = new ArrayList<Vector3d>();
			newList.add(newMatch);
			matchDic.put(base1, newList);
			}

		newMatch = new Vector3d((double) base1, optValue[0], (double) index);
		list = (ArrayList<Vector3d>) matchDic.get(base2);

		if (list!=null)
			{

			boolean isNew = true;
			vit = list.iterator();
			while (vit.hasNext())
				{
				next = vit.next();
				if (next.x==newMatch.x)
					{
					isNew = false;
					if (newMatch.y<next.y)
						{
						vit.remove();
						list.add(newMatch);
						break;

						}
					}
				}
			if (isNew)
				{
				list.add(newMatch);
				}

			}
		else
			{
			ArrayList<Vector3d> newList = new ArrayList<Vector3d>();
			newList.add(newMatch);
			matchDic.put(base2, newList);
			}
		return true;
		}
		/**
		 * Calculates the best worm conformation that can be obtained by deforming the worm 
		 * shape constructed through the worm skeleton 'ws' and the profile 'wprof', following 
		 * the optimization method defined by 'optMethod'
		 */
	private static ArrayList<Integer> bendingOptimization(
			WormShapeOptimization optMethod, WormSkeleton ws, WormProfile wprof,
			int[] dtArray, double[] optValue)
		{

		ArrayList<Integer> rastShape = new ArrayList<Integer>();
		CardinalSpline cs;
		cs = WormDescriptor.getShapeSpline(ws, 0.5, 0.09);
		if (cs!=null)
			{
			int[] profPts = ws.getPixelMatcher().pointListToPixel(
					EvCardinalSpline.getCardinalPoints(cs, wprof.thickness.length));
			}
		else
			{
			// System.out.println("  ---> Output: Path is not a skeleton. Skeleton Spline not created");
			return null;
			}

		// System.out.print("  --> Building Descriptor");
		WormDescriptor wd = new WormDescriptor(wprof, ws, dtArray,
				wprof.thickness.length, 8.0);
		// System.out.println(": Succesfully built");
		// System.out.print("  --> Matching shape: Best Neighbor Optimization");
		optValue[0] = optMethod.run(wd);
		// System.out.println(": Match succesfully found");

		// If rasterization fails then it will be null
		rastShape = wd.fitAndRasterizeWorm();

		// System.out.println("  -->Worm succesfully rasterized");
		return rastShape;

		}

	public static Hashtable<Integer, ArrayList<WormSkeleton>> buildPathDictionary(
			ArrayList<WormSkeleton> wormSkeletons)
		{
		Hashtable<Integer, ArrayList<WormSkeleton>> pathDic = new Hashtable<Integer, ArrayList<WormSkeleton>>();
		Iterator<WormSkeleton> wit = wormSkeletons.iterator();
		WormSkeleton ws;
		int base1;
		int base2;
		while (wit.hasNext())
			{
			ws = wit.next();
			base1 = ws.getSkPoints().get(0);
			base2 = ws.getSkPoints().get(ws.getSkPoints().size()-1);

			ArrayList<WormSkeleton> list1 = (ArrayList<WormSkeleton>) pathDic
					.get(base1);
			if (list1!=null)
				{
				list1.add(ws);
				}
			else
				{
				ArrayList<WormSkeleton> newList = new ArrayList<WormSkeleton>();
				newList.add(ws);
				pathDic.put(base1, newList);
				}

			ArrayList<WormSkeleton> list2 = (ArrayList<WormSkeleton>) pathDic
					.get(base2);
			if (list2!=null)
				{
				list2.add(ws);
				}
			else
				{
				ArrayList<WormSkeleton> newList = new ArrayList<WormSkeleton>();
				newList.add(ws);
				pathDic.put(base2, newList);
				}
			}

		return pathDic;
		}

	}
