package endrov.annotationWorms.javier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.vecmath.Vector2d;

import com.graphbuilder.curve.CardinalSpline;
import com.graphbuilder.curve.Point;

import endrov.annotationWorms.javier.skeleton.SkeletonUtils;
import endrov.annotationWorms.javier.skeleton.WormSkeleton;
import endrov.util.curves.EvCardinalSpline;
import endrov.utilityUnsorted.tesselation.PolygonRasterizer;

/**
 * Defines a worm shape as the contour and area of the shape.
 * 
 * @author Javier Fernandez
 */

public class WormShape
	{
	ArrayList<Integer> wormContour;
	ArrayList<Integer> wormArea;
	WormPixelMatcher wpm;
	boolean[] isContourPoint;
	boolean[] isWormArea;

	public WormShape(ArrayList<Integer> wormContour, ArrayList<Integer> wormArea,	WormPixelMatcher wpm, int[] dtArray)
		{
		if (wormArea==null)
			this.wormArea = new ArrayList<Integer>();
		else
			{
			this.wormArea = new ArrayList<Integer>(wormArea);
			}
		if (wormContour==null)
			this.wormContour = new ArrayList<Integer>();
		else
			{
			this.wormContour = new ArrayList<Integer>(wormContour);
			}

		this.wpm = wpm;
		this.isContourPoint = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(),	wormContour);
		this.isWormArea = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(),	wormArea);
		}

	/**
	 * Constructs a worm shape based on the type of worm information given. If
	 * contourGiven is true then wormPoints must be the list of points belonging
	 * to the worm contour. Otherwise the list must be the list of points
	 * belonging to the worm area, including the worm contour. The missing points
	 * (either contour or area) are calculated based on the input
	 */
	public WormShape(ArrayList<Integer> wormPoints, WormPixelMatcher wpm,
			boolean contourGiven, int[] dtArray)
		{
		this.wpm = wpm;
		if (contourGiven)
			{
			wormContour = new ArrayList<Integer>(wormPoints);
			isContourPoint = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), wormContour);

			double[] srs =
				{ 2.0, 1.5, 1.0 };

			// Rasterize to generate Area. First a two spline contour attempt. This is
			// more accurate
			// but it could not be rasterizable. If it fails a oneSplineContour
			// attempt is done
			wormArea = null;
			double contourPerc = 1;
			int numAtt = 8;
			ArrayList<Integer> contour = new ArrayList<Integer>();
			while (wormArea==null&&numAtt<3)
				{
				contour = twoSplineContour(wormContour, wpm, contourPerc);
				wormArea = WormShape.rasterizeWorm(contour, wpm);
				contourPerc -= 0.1;
				numAtt++;
				}

			// If percentage of the worm did not work try a contour spline generation
			// approach
			double splineRate = 0.2;
			ArrayList<Integer> splineContour = new ArrayList<Integer>();
			numAtt = 0;
			while (wormArea==null&&numAtt<3)
				{
				splineRate = srs[numAtt];
				splineContour = twoSplineContour(wormContour, wpm, splineRate);
				wormArea = WormShape.rasterizeWorm(splineContour, wpm);
				numAtt++;
				}
			if (wormArea==null)
				{
				// Reduce spline rate if shape is not succesfully rasterized
				numAtt = 0;
				splineRate = 2.0;
				while (wormArea==null&&numAtt<3)
					{
					splineRate = srs[numAtt];
					splineContour = oneSplineContour(wormContour, wpm, splineRate);
					wormArea = WormShape.rasterizeWorm(splineContour, wpm);
					numAtt++;
					}
				}

			// if the area can not be rasterized then is empty
			if (wormArea==null)
				{
				wormArea = new ArrayList<Integer>();
				}
			isWormArea = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), wormArea);
			}
		else
			{
			wormArea = new ArrayList<Integer>(wormPoints);
			isWormArea = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), wormArea);
			wormContour = contourFromArea(wormArea, isWormArea, wpm);
			isContourPoint = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), wormContour);
			}

		}

	public ArrayList<Integer> getWormContour()
		{
		return wormContour;
		}

	public ArrayList<Integer> getWormArea()
		{
		return wormArea;
		}

	public WormPixelMatcher getWpm()
		{
		return wpm;
		}

	public boolean[] getIsContourPoint()
		{
		return isContourPoint;
		}

	public boolean[] getIsWormArea()
		{
		return isWormArea;
		}

	/**
	 * Given the pixels belonging to the area of the worm shape, the contour
	 * is calculated
	 */
	private static ArrayList<Integer> contourFromArea(ArrayList<Integer> area,
			boolean isArea[], WormPixelMatcher wpm)
		{
		Iterator<Integer> areaIt = area.iterator();
		ArrayList<Integer> contour = new ArrayList<Integer>();
		int[] neigh;
		int pixel;
		while (areaIt.hasNext())
			{
			pixel = areaIt.next();
			neigh = SkeletonUtils.getCrossNeighbors(pixel, wpm.w);
			// If the area point has a non-area pixel then is a contour point
			for (int i = 0; i<neigh.length; i++)
				{
				if (!isArea[neigh[i]])
					{
					contour.add(pixel);
					break;
					}
				}
			}
		return contour;
		}

	/**
	 * Takes a worm contour and returns another contour that traces the silhouette
	 * for the same worm, ensuring counter clockwise order for contour pixels
	 */
	public static ArrayList<Integer> ensureCounterClockwise(
			ArrayList<Integer> contourPoints, WormProfile wprof, WormSkeleton ws,
			int[] dtArray)
		{
		WormDescriptor wd = new WormDescriptor(wprof, ws, dtArray,
				wprof.thickness.length, 8.0);

		// find first north contour point
		boolean match = false;
		int cp = 1;
		int index = -1;
		int firstIndex;
		int secondIndex;
		while (!match&&cp<wprof.thickness.length-2)
			{
			for (int nl = 1; nl<wd.angleNorthLine[cp].length; nl++)
				{
				index = contourPoints.indexOf(wd.angleNorthLine[cp][nl]);
				if (index!=-1)
					{
					match = true;
					break;
					}
				}
			cp++;
			}
		if (match)
			firstIndex = index;
		else
			{
			return null;
			}

		index = -1;
		match = false;
		while (!match&&cp<wprof.thickness.length-1)
			{
			for (int nl = 1; nl<wd.angleNorthLine[cp].length; nl++)
				{
				index = contourPoints.indexOf(wd.angleNorthLine[cp][nl]);
				if (index!=-1)
					{
					match = true;
					break;
					}
				}
			cp++;
			}
		if (match)
			secondIndex = index;
		else
			{
			return null;
			}

		// If firstIndex is higher than SecondIndex then is not in counter clockwise
		// order
		if (firstIndex>secondIndex)
			{
			Collections.reverse(contourPoints);
			}

		return contourPoints;
		}

	/**
	 * Returns the pixels belonging to the area of the worm defined
	 * by the calling worm descriptor
	 */
	public static ArrayList<Integer> rasterizeWorm(ArrayList<Integer> wormContour, WormPixelMatcher wpm)
		{
		ArrayList<Vector2d> tpv = wpm.pixelListToVector2d(wormContour);
		ArrayList<Integer> area = PolygonRasterizer.rasterize(wpm.w, wpm.h, tpv);
		return area;
		}

	private static ArrayList<Point> getSubList(ArrayList<Point> pointList,	int init, int end)
		{
		Iterator<Point> pit = pointList.iterator();
		ArrayList<Point> sub = new ArrayList<Point>();
		int count = 0;
		while (pit.hasNext()&&count<init)
			{
			pit.next();
			count++;
			}
		while (pit.hasNext()&&count<=end)
			{
			sub.add(pit.next());
			count++;
			}
		return sub;
		}


	public static ArrayList<Integer> contourPercentage(
			ArrayList<Integer> wormContour, WormPixelMatcher wpm, double numPointsPerc)
		{
		return takeNIntegerPoints(wormContour, numPointsPerc);
		}

	public static ArrayList<Integer> twoSplineContour(
			ArrayList<Integer> wormContour, WormPixelMatcher wpm, double numPointsPerc)
		{
		ArrayList<Integer> newContour = new ArrayList<Integer>();

		ArrayList<Point> points = wpm.pixelListToPoint(wormContour);
		// Construct north spline
		ArrayList<Point> base = new ArrayList<Point>();
		ArrayList<Point> northPoints = (ArrayList<Point>) getSubList(points, 0,
				(wormContour.size()/2)-1);
		base.add(wpm.pixelToPoint(wormContour.get(0)));
		base.add(wpm.pixelToPoint(wormContour.get((wormContour.size()/2)-1)));

		CardinalSpline skSpline = EvCardinalSpline.getShapeSpline(base,
				northPoints, 0.5, numPointsPerc);
		ArrayList<Integer> splineContour = wpm
				.pointListToPixelList(EvCardinalSpline.getCardinalPoints(skSpline, 0));
		newContour.addAll(splineContour);

		// Construct South spline
		base = new ArrayList<Point>();
		// +2 and -3 are selected instead of +1 and -1 to look for separation in
		// points near to endpoints
		ArrayList<Point> southPoints = (ArrayList<Point>) getSubList(points,
				(wormContour.size()/2)+2, wormContour.size()-3);
		base.add(wpm.pixelToPoint(wormContour.get(wormContour.size()/2)+2));
		base.add(wpm.pixelToPoint(wormContour.get(wormContour.size()-3)));
		skSpline = EvCardinalSpline.getShapeSpline(base, southPoints, 0.5,
				numPointsPerc);
		splineContour = wpm.pointListToPixelList(EvCardinalSpline
				.getCardinalPoints(skSpline, 0));
		newContour.addAll(splineContour);

		return newContour;
		}

	public static ArrayList<Integer> oneSplineContour(
			ArrayList<Integer> wormContour, WormPixelMatcher wpm, double numPointsPerc)
		{
		ArrayList<Integer> newContour = new ArrayList<Integer>();

		ArrayList<Point> points = wpm.pixelListToPoint(wormContour);

		// Construct north spline
		ArrayList<Point> base = new ArrayList<Point>();
		points = (ArrayList<Point>) getSubList(points, 0, (wormContour.size())-2);
		base.add(wpm.pixelToPoint(wormContour.get(0)));
		base.add(wpm.pixelToPoint(wormContour.get((wormContour.size())-2)));

		CardinalSpline skSpline = EvCardinalSpline.getShapeSpline(base, points,
				0.5, numPointsPerc);
		ArrayList<Integer> splineContour = wpm
				.pointListToPixelList(EvCardinalSpline.getCardinalPoints(skSpline, 0));
		newContour.addAll(splineContour);

		return newContour;
		}

	public static ArrayList<Integer> oneSplineNoBaseContour(
			ArrayList<Integer> wormContour, WormPixelMatcher wpm, double numPointsPerc)
		{
		ArrayList<Integer> newContour = new ArrayList<Integer>();

		ArrayList<Point> points = wpm.pixelListToPoint(wormContour);

		// Construct north spline
		ArrayList<Point> base = new ArrayList<Point>();
		points = (ArrayList<Point>) getSubList(points, 1, (wormContour.size())-2);
		base.add(wpm.pixelToPoint(wormContour.get(1)));
		base.add(wpm.pixelToPoint(wormContour.get((wormContour.size())-2)));

		CardinalSpline skSpline = EvCardinalSpline.getShapeSpline(base, points,
				0.5, numPointsPerc);
		ArrayList<Integer> splineContour = wpm
				.pointListToPixelList(EvCardinalSpline.getCardinalPoints(skSpline, 0));
		newContour.addAll(splineContour);

		return newContour;
		}

	private static ArrayList<Integer> takeNIntegerPoints(
			ArrayList<Integer> points, double numPointsPercentage)
		{
		int length = points.size();
		int numPoints = (int) (((double) length)*numPointsPercentage);
		if (numPoints<2)
			return null;
		int step = length/(numPoints-1);
		int stepCount;
		Iterator<Integer> it = points.iterator();
		ArrayList<Integer> cp = new ArrayList<Integer>();

		Integer nextPixel = null;

		// Adding skeleton points to ControlPath. Note that
		// the base points are added twice, manually and belonging
		// to points. This to make them count in spline curve
		while (it.hasNext()&&numPoints>0)
			{
			nextPixel = it.next();
			cp.add(nextPixel);
			stepCount = 0;
			while (stepCount<step-1&&it.hasNext())
				{
				it.next();
				stepCount++;
				}
			numPoints--;
			}

		return cp;
		}
	}
