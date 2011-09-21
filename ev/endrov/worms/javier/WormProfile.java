package endrov.worms.javier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import com.graphbuilder.curve.CardinalSpline;
import com.graphbuilder.curve.Point;

import endrov.tesselation.utils.Line;
import endrov.util.ImVector2;
import endrov.util.Vector2i;
import endrov.util.curves.EvCardinalSpline;
import endrov.worms.javier.skeleton.SkeletonUtils;
import endrov.worms.javier.skeleton.WormSkeleton;

/**
 * Defines a numerical profile of a worm shape
 * 
 * @author Javier Fernandez
 */


public class WormProfile
	{

	public WormPixelMatcher wpm;
	public double[] thickness;

	/**
	 * Creates a worm profile given the thickness set and a WormPixelMatcher 'wpm'
	 */
	public WormProfile(WormPixelMatcher wpm, double[] thickness)
		{
		this.thickness = thickness.clone();
		this.wpm = wpm;
		}

	/**
	 * Calculates the profile of a single worm
	 * @param ws Skeleton of the profiling worm
	 * @param consectPts indicates whether the given skeleton stores consecutive points
	 * @param numPoints number of control points
	 * @param dtArray distance transform array
	 */
	public WormProfile(WormSkeleton ws, boolean consecPts, int numPoints,
			int[] dtArray)
		{
		this.wpm = ws.getPixelMatcher();
		double[] thickness = getThickness(dtArray, ws, consecPts, numPoints);
		this.thickness = thickness;
		}
	
	/**
	 * Calculates a generic worm profile by calculating the individual profiles of a set of
	 * worm skeletons given by 'worms', and finding an average thickness distribution.
	 * @param ws Skeleton of the profiling worm
	 * @param consectPts indicates whether the given skeleton stores consecutive points
	 * @param numPoints number of control points
	 * @param dtArray distance transform array 
	 */
	public WormProfile(ArrayList<WormSkeleton> worms, boolean consecPts,
			int numPoints, int[] dtArray)
		{
		WormProfile tempProf;
		int numWorms = worms.size();
		double[][] thicknessList = new double[numWorms][numPoints];
		Iterator<WormSkeleton> wit = worms.iterator();
		int count = 0;
		while (wit.hasNext())
			{
			tempProf = new WormProfile(wit.next(), consecPts, numPoints, dtArray);
			thicknessList[count] = tempProf.thickness;
			count++;
			}
		wpm = worms.get(0).getPixelMatcher();
		thickness = new double[numPoints];

		// Set the average thickness for each control point
		for (int i = 1; i<numPoints-1; i++)
			{
			int average = 0;
			for (int j = 0; j<numWorms; j++)
				{
				average += thicknessList[j][i];
				}
			thickness[i] = (average/(double) numWorms);
			if (thickness[i]<1.0)
				thickness[i] = 1.0;
			}
		}

	
	
	/**
	 * Calculates the thickness associated to the consecutive skeleton points of
	 * the isolated worm given by skPoints.
	 * 
	 * @param wormDT
	 *          Distance transformation of a single worm that has the size of the
	 *          original image
	 * @param wpm
	 *          Worm image matcher
	 * @param skPoints
	 *          Consecutive points belonging to the skeleton
	 * @param consecPts
	 *          The points in the skeleton points array are consecutive
	 */
	public static double[] getThickness(int[] wormDT, WormSkeleton ws,
			boolean consecPts, int numPoints)
		{

		if (!consecPts)
			{
			SkeletonUtils.makeConsecutive(ws);
			}

		WormPixelMatcher wpm = ws.getPixelMatcher();

		// create skeleton spline and take numPoints
		ArrayList<Point> base = wpm.baseToPoint(ws.getBasePoints());
		ArrayList<Point> points = wpm.pixelListToPoint(ws.getSkPoints());
		CardinalSpline skSpline = EvCardinalSpline.getShapeSpline(base, points,
				0.5, 0.2);
		int[] profPts = wpm.pointListToPixel(EvCardinalSpline.getCardinalPoints(
				skSpline, numPoints));
		Line l1;
		ArrayList<Integer> contourPoints = new ArrayList<Integer>();
		double d1, d2;

		double[] thickness = new double[numPoints];

		// System.out.println("Calculating average points");
		// Calculate average distance to contour points and add to thickness
		for (int i = 1; i<profPts.length-1; i++)
			{
			Vector2i[] extremes = getExtremes(wormDT, wpm, wpm
					.getPixelPos(profPts[i-1]), wpm.getPixelPos(profPts[i]), wpm
					.getPixelPos(profPts[i+1]), wormDT[profPts[i]]);
			l1 = new Line(wpm.getPixelPos(profPts[i]), extremes[0]);
			d1 = distBestLinePoint(wormDT, wpm, l1, contourPoints);
			l1 = new Line(wpm.getPixelPos(profPts[i]), extremes[1]);
			d2 = distBestLinePoint(wormDT, wpm, l1, contourPoints);
			thickness[i] = (d1+d2)/2;
			}
		// System.out.println("1-profile generated");
		return thickness;
		}

	/**
	 * Calculates the worm shape main contour points over the given control points
	 * based on the calling object thickness. The size of controlPoints must be
	 * the same as the thickness variable, and must be ordered consecutively.
	 */
	public static ArrayList<ArrayList<Integer>> getContourPoints(
			int[] controlPoints, WormProfile wp, int[] wormDT)
		{
		ArrayList<Integer> shapePointsNorth = new ArrayList<Integer>(
				controlPoints.length);
		ArrayList<Integer> shapePointsSouth = new ArrayList<Integer>(
				controlPoints.length);
		ArrayList<ArrayList<Integer>> spoints = new ArrayList<ArrayList<Integer>>();
		spoints.add(shapePointsNorth);
		spoints.add(shapePointsSouth);
		int[][] extremePixels = new int[controlPoints.length*2][2];

		// calculate extremes pixels
		for (int i = 1; i<controlPoints.length-1; i++)
			{
			Vector2i[] extremes = getExtremes(wormDT, wp.wpm, wp.wpm
					.getPixelPos(controlPoints[i-1]), wp.wpm
					.getPixelPos(controlPoints[i]), wp.wpm
					.getPixelPos(controlPoints[i+1]), wp.thickness[i]);
			extremePixels[i][0] = wp.wpm.posToPixel(extremes[0]);
			extremePixels[i][1] = wp.wpm.posToPixel(extremes[1]);
			}
		// Add shape contour in counter-clockwise order
		shapePointsNorth.add(controlPoints[0]);
		for (int i = 1; i<controlPoints.length-1; i++)
			{
			shapePointsNorth.add(extremePixels[i][0]);
			}
		shapePointsNorth.add(controlPoints[controlPoints.length-1]);
		shapePointsSouth.add(controlPoints[controlPoints.length-1]);
		for (int i = controlPoints.length-2; i>0; i--)
			{
			shapePointsSouth.add(extremePixels[i][1]);
			}
		shapePointsSouth.add(controlPoints[0]);
		return spoints;
		}

	/**
	 * Calculates the worm shape main contour points over the given control points
	 * based on the calling object thickness, and expanding the thickness contour
	 * to match distance map edges. The size of controlPoints must be the same as
	 * the thickness variable, and must be ordered consecutively.
	 */
	public static ArrayList<ArrayList<Integer>> getExpandedContourPoints(
			int[] controlPoints, WormProfile wp, int[] wormDT)
		{
		ArrayList<Integer> shapePointsNorth = new ArrayList<Integer>(
				controlPoints.length);
		ArrayList<Integer> shapePointsSouth = new ArrayList<Integer>(
				controlPoints.length);
		ArrayList<ArrayList<Integer>> spoints = new ArrayList<ArrayList<Integer>>();
		spoints.add(shapePointsNorth);
		spoints.add(shapePointsSouth);
		int[][] extremePixels = new int[controlPoints.length*2][2];

		// calculate extremes pixels
		for (int i = 1; i<controlPoints.length-1; i++)
			{
			Vector2i[] extremes = getExpandedExtremes(wormDT, wp.wpm, wp.wpm
					.getPixelPos(controlPoints[i-1]), wp.wpm
					.getPixelPos(controlPoints[i]), wp.wpm
					.getPixelPos(controlPoints[i+1]), wp.thickness[i], wormDT);

			extremePixels[i][0] = wp.wpm.posToPixel(extremes[0]);
			extremePixels[i][1] = wp.wpm.posToPixel(extremes[1]);

			}
		// Add shape contour in counter-clockwise order
		shapePointsNorth.add(controlPoints[0]);
		for (int i = 1; i<controlPoints.length-1; i++)
			{
			shapePointsNorth.add(extremePixels[i][0]);
			}
		shapePointsNorth.add(controlPoints[controlPoints.length-1]);
		shapePointsSouth.add(controlPoints[controlPoints.length-1]);
		for (int i = controlPoints.length-2; i>0; i--)
			{
			shapePointsSouth.add(extremePixels[i][1]);
			}
		shapePointsSouth.add(controlPoints[0]);
		return spoints;
		}

	/**
	 * Calculates the worm shape contour points over the given control points
	 * based on the worm profile thickness. The size of controlPoints must be the
	 * same as the thickness variable, and must be ordered consecutively.
	 */
	public static ArrayList<Integer> constructShape(int[] controlPoints,
			WormProfile wp, int numPoints, int[] wormDT)
		{

		ArrayList<Point> base;
		int[] baseA = new int[2];
		ArrayList<ArrayList<Integer>> contourP = getContourPoints(controlPoints,
				wp, wormDT);
		ArrayList<Integer> shapePoints = new ArrayList<Integer>();
		ArrayList<Integer> sidePoints;
		CardinalSpline skSpline;
		HashSet<Integer> hash = new HashSet<Integer>();
		Iterator<Integer> it;
		int next;
		boolean newAdded;

		// North Spline
		baseA[0] = controlPoints[0];
		baseA[1] = controlPoints[controlPoints.length-1];
		base = wp.wpm.baseToPoint(baseA);
		skSpline = EvCardinalSpline.getShapeSpline(base, wp.wpm
				.pixelListToPoint(contourP.get(0)), 0.5, 0.9);
		sidePoints = wp.wpm.pointListToPixelList(EvCardinalSpline
				.getCardinalPoints(skSpline, numPoints));

		it = sidePoints.iterator();
		// it=contourP.get(0).iterator();
		while (it.hasNext())
			{
			next = it.next();
			newAdded = hash.add(next);
			if (newAdded)
				{
				shapePoints.add(next);
				}
			}

		// South Spline
		baseA[0] = controlPoints[controlPoints.length-1];
		baseA[1] = controlPoints[0];
		base = wp.wpm.baseToPoint(baseA);
		skSpline = EvCardinalSpline.getShapeSpline(base, wp.wpm
				.pixelListToPoint(contourP.get(1)), 0.5, 0.9);
		sidePoints = wp.wpm.pointListToPixelList(EvCardinalSpline
				.getCardinalPoints(skSpline, numPoints));

		it = sidePoints.iterator();
		// it=contourP.get(1).iterator();
		while (it.hasNext())
			{
			next = it.next();
			newAdded = hash.add(next);
			if (newAdded)
				{
				shapePoints.add(next);
				}
			}
		return shapePoints;
		}

	
	/**
	 * Calculates the worm shape contour points over the given control points
	 * based on the worm profile thickness. The size of controlPoints must be the
	 * same as the thickness variable, and must be ordered consecutively. The
	 * contour points are expanded or contracted slightly to meet the contour
	 * points defined by the distance transformation 'wormDT'
	 */
	public static ArrayList<Integer> expandingConstructShape(int[] controlPoints,
			WormProfile wp, int numPoints, int[] wormDT)
		{

		ArrayList<Point> base;
		int[] baseA = new int[2];
		ArrayList<ArrayList<Integer>> contourP = getExpandedContourPoints(
				controlPoints, wp, wormDT);
		ArrayList<Integer> shapePoints = new ArrayList<Integer>();
		ArrayList<Integer> sidePoints;
		CardinalSpline skSpline;
		HashSet<Integer> hash = new HashSet<Integer>();
		Iterator<Integer> it;
		int next;
		boolean newAdded;

		// North Spline
		baseA[0] = controlPoints[0];
		baseA[1] = controlPoints[controlPoints.length-1];
		base = wp.wpm.baseToPoint(baseA);
		skSpline = EvCardinalSpline.getShapeSpline(base, wp.wpm
				.pixelListToPoint(contourP.get(0)), 0.5, 0.9);
		sidePoints = wp.wpm.pointListToPixelList(EvCardinalSpline
				.getCardinalPoints(skSpline, numPoints));

		it = sidePoints.iterator();
		// it=contourP.get(0).iterator();
		while (it.hasNext())
			{
			next = it.next();
			newAdded = hash.add(next);
			if (newAdded)
				{
				shapePoints.add(next);
				}
			}

		// South Spline
		baseA[0] = controlPoints[controlPoints.length-1];
		baseA[1] = controlPoints[0];
		base = wp.wpm.baseToPoint(baseA);
		skSpline = EvCardinalSpline.getShapeSpline(base, wp.wpm
				.pixelListToPoint(contourP.get(1)), 0.5, 0.9);
		sidePoints = wp.wpm.pointListToPixelList(EvCardinalSpline
				.getCardinalPoints(skSpline, numPoints));

		it = sidePoints.iterator();
		// it=contourP.get(1).iterator();
		while (it.hasNext())
			{
			next = it.next();
			newAdded = hash.add(next);
			if (newAdded)
				{
				shapePoints.add(next);
				}
			}
		return shapePoints;
		}


	/**
	 * Returns the extreme points defined by the bisector placed at p2 of length
	 * 'length'
	 */
	public static Vector2i[] getExtremes(int[] dtArray, WormPixelMatcher wpm,
			Vector2i p1, Vector2i p2, Vector2i p3, double length)
		{
		ImVector2[] vec = bisectVector(p1, p2, p3);
		Vector2i[] bisection = contourFromBisectVectors(vec[0], vec[1], p2, length);

		return bisection;
		}

	/**
	 * Returns the extreme points defined by the bisector placed at p2 of initial
	 * length 'length', that is expanded or contracted to meet the contour points
	 * defined by the distance transformation array 'dtArray'
	 */
	public static Vector2i[] getExpandedExtremes(int[] dtArray,
			WormPixelMatcher wpm, Vector2i p1, Vector2i p2, Vector2i p3,
			double length, int[] dt)
		{
		ImVector2[] vec = bisectVector(p1, p2, p3);
		Vector2i[] bisection = contourFromBisectVectorsExpanded(vec[0], vec[1], p2,
				length, dt, wpm);

		return bisection;
		}

	/**
	 * Receives three image points, creates 2 vectors and calculates the resulting
	 * vector that bisects the angle between them. Returns the the two opposite
	 * extreme points of the bisection vector that starts on p2
	 */
	private static ImVector2[] bisectVector(Vector2i p1, Vector2i p2, Vector2i p3)
		{
		// Vector p2->p1
		ImVector2 v1 = new ImVector2((double) (p1.x-p2.x), (double) (p1.y-p2.y));
		// Vector p2->p3
		ImVector2 v2 = new ImVector2((double) (p3.x-p2.x), (double) (p3.y-p2.y));
		// Calculate bisection vector angle
		ImVector2 v2Copy = new ImVector2(v2.x, v2.y);

		v1 = v1.normalize();
		v2 = v2.normalize();

		double acosParam = (v1.dot(v2));// /(v1.length()*v2.length());
		if (acosParam<-1.0)
			acosParam = -1.0;
		else if (acosParam>1.0)
			acosParam = 1.0;
		double angle = Math.acos(acosParam);
		angle = angle/2;

		v2 = v1.rotate(-angle);
		v1 = v1.rotate(angle);

		// find close angle rotation
		double dist1 = Math.sqrt(Math.pow((v1.x-v2Copy.x), 2)
				+Math.pow((v1.y-v2Copy.y), 2));
		double dist2 = Math.sqrt(Math.pow((v2.x-v2Copy.x), 2)
				+Math.pow((v2.y-v2Copy.y), 2));

		if (dist1<dist2)
			{
			v2 = v1.rotate(Math.toRadians(180));
			}
		else
			{
			v1 = v2.rotate(Math.toRadians(180));
			}

		v1 = v1.normalize();// .mul((double)length);
		v2 = v2.normalize();// .mul((double)length);

		ImVector2[] vectors = new ImVector2[2];
		vectors[0] = v1;
		vectors[1] = v2;
		return vectors;
		}

	/**
	 * Returns the pair of contour pixels found by at the opposite extremes of the
	 * bisector of length 'length'
	 */
	private static Vector2i[] contourFromBisectVectors(ImVector2 v1,
			ImVector2 v2, Vector2i controlPoint, double length)
		{
		// Return the bisection extreme point translating to original
		Vector2i[] extremes = new Vector2i[2];
		v1 = v1.mul((double) length);
		v2 = v2.mul((double) length);
		extremes[0] = new Vector2i((int) Math.round(v1.x+controlPoint.x),
				(int) Math.round(v1.y+controlPoint.y));
		extremes[1] = new Vector2i((int) Math.round(v2.x+controlPoint.x),
				(int) Math.round(v2.y+controlPoint.y));

		return extremes;
		}

	/**
	 * Returns the pair of contour pixels found by at the opposite extremes of the
	 * bisector of length 'length'. The extremes of the bisector are expanded or
	 * contracted in order to meet contour points defined by the distance
	 * transformation given by 'dtArray'
	 */
	private static Vector2i[] contourFromBisectVectorsExpanded(ImVector2 v1,
			ImVector2 v2, Vector2i controlPoint, double length, int[] dtArray,
			WormPixelMatcher wpm)
		{
		// Return the bisection extreme point translating to original
		Vector2i[] extremes = new Vector2i[2];

		ImVector2 v1copy = new ImVector2(v1.x, v1.y);
		ImVector2 v2copy = new ImVector2(v2.x, v2.y);
		v1 = v1.mul((double) length);
		v2 = v2.mul((double) length);
		extremes[0] = new Vector2i((int) Math.round(v1.x+controlPoint.x),
				(int) Math.round(v1.y+controlPoint.y));
		extremes[1] = new Vector2i((int) Math.round(v2.x+controlPoint.x),
				(int) Math.round(v2.y+controlPoint.y));
		// return extremes;

		Vector2i next = new Vector2i();
		int dtvalue = 0;
		int bestDT = dtArray[wpm.posToPixel(extremes[0])];
		Vector2i bestPos = extremes[0];

		// Contract while pixels with less dt value are found in the same direction
		for (int i = 1; i<3; i++)
			{
			v1 = v1copy.mul((double) (length-i));
			next = new Vector2i((int) Math.round(v1.x+controlPoint.x), (int) Math
					.round(v1.y+controlPoint.y));
			dtvalue = dtArray[wpm.posToPixel(next)];
			if (dtvalue!=0&&dtvalue<bestDT)
				{
				bestDT = dtvalue;
				bestPos = next;
				}
			else
				{
				break;
				}
			}
		// Same procedure but expanding
		for (int i = 1; i<3; i++)
			{
			v1 = v1copy.mul((double) (length+i));
			next = new Vector2i((int) Math.round(v1.x+controlPoint.x), (int) Math
					.round(v1.y+controlPoint.y));
			dtvalue = dtArray[wpm.posToPixel(next)];
			if (dtvalue!=0&&dtvalue<bestDT)
				{
				bestDT = dtvalue;
				bestPos = next;
				}
			else
				{
				break;
				}
			}
		extremes[0] = bestPos;

		next = new Vector2i();
		dtvalue = 0;
		bestDT = dtArray[wpm.posToPixel(extremes[1])];
		bestPos = extremes[1];

		// Contract while pixels with less dt value are found in the same direction
		for (int i = 1; i<3; i++)
			{
			v2 = v2copy.mul((double) (length-i));
			next = new Vector2i((int) Math.round(v2.x+controlPoint.x), (int) Math
					.round(v2.y+controlPoint.y));
			dtvalue = dtArray[wpm.posToPixel(next)];
			if (dtvalue!=0&&dtvalue<bestDT)
				{
				bestDT = dtvalue;
				bestPos = next;
				}
			else
				{
				break;
				}
			}
		// Same procedure but expanding
		for (int i = 1; i<3; i++)
			{
			v2 = v2copy.mul((double) (length+i));
			next = new Vector2i((int) Math.round(v2.x+controlPoint.x), (int) Math
					.round(v2.y+controlPoint.y));
			dtvalue = dtArray[wpm.posToPixel(next)];
			if (dtvalue!=0&&dtvalue<bestDT)
				{
				bestDT = dtvalue;
				bestPos = next;
				}
			else
				{
				break;
				}
			}
		extremes[1] = bestPos;

		return extremes;
		}

	/**
	 * Calculate the average length for the WormSkeleton objects in the list. The
	 * length of a WormSkeleton is the number of pixels that composes the
	 * skeleton.
	 * 
	 * @param singleWormList
	 *          List of isolated worms.
	 */
	public static int calculateWormListLength(
			ArrayList<WormSkeleton> singleWormList, WormPixelMatcher wpm)
		{
		int average = 0;
		Iterator<WormSkeleton> it = singleWormList.iterator();
		ArrayList<Integer> lengthList = new ArrayList<Integer>();
		// System.out.println("Calculating worm length");
		while (it.hasNext())
			{
			lengthList.add(it.next().getSkPoints().size());
			// lengthList.add(SkeletonUtils.calculatePathLength(it.next().getSkPoints(),
			// wpm));
			}
		// sort and delete the 20% of higher and lower values
		Collections.sort(lengthList);
		int numDelete = (int) ((lengthList.size()*0.35)/2);

		for (int index = 0; index<numDelete; index++)
			{
			lengthList.remove(0);
			lengthList.remove(lengthList.size()-1);
			}

		Iterator<Integer> lit = lengthList.iterator();
		while (lit.hasNext())
			{
			int l = lit.next();
			average += l;
			}

		return (average/lengthList.size());
		}
	

	private static double distBestLinePoint(int[] wormDT, WormPixelMatcher wpm,
			Line l1, ArrayList<Integer> contourPoints)
		{
		ArrayList<Integer> linePoints = l1.getLinePoints(wpm.w);
		Iterator<Integer> lit = linePoints.iterator();
		int bestPixel = -1;
		int bestDT = Integer.MAX_VALUE;
		int next;
		lit.next();// Avoid skPoint
		while (lit.hasNext())
			{
			next = lit.next();
			if (wormDT[next]>1&&wormDT[next]<=bestDT)
				{
				bestPixel = next;
				bestDT = wormDT[next];
				contourPoints.add(next);
				}
			}
		// assign last pixel if not dt found (case for non-steep slopes)
		if (bestPixel==-1)
			{
			bestPixel = linePoints.get(linePoints.size()-1);
			if (wormDT[bestPixel]==0)
				bestDT = Integer.MAX_VALUE;
			else
				bestDT = wormDT[bestPixel];
			}
		// Check if there is a better pixel around
		boolean newpixel = true;
		while (newpixel)
			{
			int[] neigh = SkeletonUtils.getCircularNeighbors(bestPixel, wpm.w);
			newpixel = false;
			for (int i = 0; i<neigh.length; i++)
				{
				if (wormDT[neigh[i]]<bestDT&&wormDT[neigh[i]]!=0)
					{
					bestDT = wormDT[neigh[i]];
					bestPixel = neigh[i];
					newpixel = true;
					}
				}
			}
		contourPoints.add(bestPixel);
		Vector2i bp = wpm.getPixelPos(bestPixel);
		// Calculate distance
		return Math.sqrt(Math.pow(bp.x-l1.p1.x, 2)+Math.pow(bp.y-l1.p1.y, 2));

		}


	}
