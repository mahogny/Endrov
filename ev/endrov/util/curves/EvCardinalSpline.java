package endrov.util.curves;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import endrov.util.curves.WrongParameterSplineException;
import endrov.util.math.Vector2i;

import com.graphbuilder.curve.CardinalSpline;
import com.graphbuilder.curve.ControlPath;
import com.graphbuilder.curve.GroupIterator;
import com.graphbuilder.curve.Point;
import com.graphbuilder.curve.ShapeMultiPath;

/**
 * Cardinal Spline for Endrov, based on Curve Api library.
 * 
 * @author Javier Fernandez
 *  
 */
public class EvCardinalSpline
	{
	CardinalSpline cs;
	ArrayList<Point> extremePoints;
	ArrayList<Point> controlPoints;

	/**
	 * Constructs a cardinal spline that starts in one base point and ends in the
	 * other one, passing through the points defined at points,storing the
	 * transformation values
	 * 
	 * @param basePoints
	 *          List of size 2 that contains the starting and ending point of the
	 *          curve
	 * @param points
	 *          the tentative cardinal spline control points
	 * @param alpha
	 *          slack value
	 * @param numPointsPercentage
	 *          Number of points from 'points' that want to be consider as control
	 *          points
	 */
	public EvCardinalSpline(ArrayList<Point> base,
			ArrayList<Point> controlPoints, double alpha, double numPointsPercentage)
			throws WrongParameterSplineException
		{

		if (base.size()!=2)
			throw new WrongParameterSplineException("Base size must be exactly 2");
		if (numPointsPercentage>1.0||numPointsPercentage<0.0)
			throw new WrongParameterSplineException(
					"Percentage must be between 0.0 and 1.0");

		this.cs = getShapeSpline(base, controlPoints, alpha, numPointsPercentage);
		this.extremePoints = new ArrayList<Point>(base);
		this.controlPoints = new ArrayList<Point>(controlPoints);

		}

	/**
	 * Calculates a cardinal spline that starts in one base point and ends in the
	 * other one, passing through the points defined at points
	 * 
	 * @param basePoints
	 *          List of size 2 that contains the starting and ending point of the
	 *          curve
	 * @param points
	 *          the tentative cardinal spline control points
	 * @param alpha
	 *          slack value
	 * @param numPointsPercentage
	 *          Number of points from 'points' that want to be consider as control
	 *          points
	 * @return
	 */
	public static CardinalSpline getShapeSpline(ArrayList<Point> basePoints,
			ArrayList<Point> points, double alpha, double numPointsPercentage)
		{

		int length = points.size();
		int numPoints = (int) (((double) length)*numPointsPercentage);
		if (numPoints<2)
			return null;
		ControlPath cp = new ControlPath();

		ArrayList<Point> controlPathPoints = takeNPoints(basePoints, points,
				numPointsPercentage);
		Iterator<Point> it = controlPathPoints.iterator();
		while (it.hasNext())
			{
			cp.addPoint(it.next());
			}

		CardinalSpline cs = new CardinalSpline(cp, new GroupIterator("0:n-1", cp
				.numPoints()));
		cs.setAlpha(alpha);

		return cs;
		}

	public static ArrayList<Point> takeNPoints(ArrayList<Point> basePoints,
			ArrayList<Point> points, double numPointsPercentage)
		{
		int length = points.size();
		int numPoints = (int) (((double) length)*numPointsPercentage);
		if (numPoints<2)
			return null;
		int step = length/(numPoints-1);
		int stepCount;
		Iterator<Point> it = points.iterator();
		ArrayList<Point> cp = new ArrayList<Point>();

		Point nextPixel = null;

		// Adding skeleton points to ControlPath. Note that
		// the base points are added twice, manually and belonging
		// to points. This to make them count in spline curve
		cp.add(basePoints.get(0));
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
		if (nextPixel!=basePoints.get(1))
			cp.add(basePoints.get(1));
		cp.add(basePoints.get(1));

		return cp;
		}

	/**
	 * Returns a list containing numPoints points evenly separated that belong to
	 * the cs cardinal spline path. If numPoints is bigger than the number of
	 * points generated or if equals 0, then all the points are returned
	 */
	public ArrayList<Point> getCardinalPoints(int numPoints)
		{
		return getCardinalPoints(this.cs, numPoints);
		}

	/**
	 * Returns a list containing numPoints points evenly separated that belong to
	 * the cs cardinal spline path. If numPoints is bigger than the number of
	 * points generated or if equals 0, all the points are returned
	 * 
	 * @param cs
	 *          A cardinal spline object
	 * @param numPoints
	 *          the number of points evenly separated to return. The two extremes
	 *          are always included.
	 */
	public static ArrayList<Point> getCardinalPoints(CardinalSpline cs,
			int numPoints)
		{
		ShapeMultiPath mp = new ShapeMultiPath();
		cs.appendTo(mp); // Computing and adding to multipath

		// Vector2i[] points = new Vector2i[mp.getCapacity()];
		HashSet<Vector2i> hashPoints = new HashSet<Vector2i>();
		ArrayList<Vector2i> pointsList = new ArrayList<Vector2i>();

		PathIterator pi = mp.getPathIterator(null);
		float coords[] = new float[2];
		boolean add;
		Vector2i ni;
		int index = 0;
		while (!pi.isDone())
			{
			pi.currentSegment(coords);
			ni = new Vector2i((int) Math.round(coords[0]), (int) Math
					.round(coords[1]));
			add = hashPoints.add(ni);
			if (add)
				{ // needed to preserve order
				pointsList.add(ni);
				index++;
				}
			pi.next();
			}

		Vector2i[] points = pointsList.toArray(new Vector2i[0]);
		if (index<1)
			return new ArrayList<Point>();
		if (numPoints>index||numPoints==0)
			numPoints = index;
		if (numPoints<0)
			return null;

		double realStep;
		if (numPoints==index)
			realStep = 1.0;
		else
			realStep = ((double) index/(double) (numPoints-1));

		double count = 0;

		Vector2i temp;
		ArrayList<Point> cardinalPoints = new ArrayList<Point>(numPoints);

		// Adding inner points
		int addCount = 0;
		while (count<(double) index&&addCount<numPoints)
			{
			temp = points[(int) Math.floor(count)];
			cardinalPoints.add(new PointFactory().createPoint(temp.x, temp.y));
			count += realStep;
			addCount += 1;
			}
		if (addCount<numPoints)
			{ // guarantee second extreme
			// Adding second extreme
			temp = points[index-1]; // second extreme (base)
			cardinalPoints.add(new PointFactory().createPoint(temp.x, temp.y));
			addCount++;
			}

		return cardinalPoints;
		}

	/**
	 * Sets imageArray to true in every spline points position contained in points
	 * 
	 * @param points
	 * @param imageArray
	 */
	public static void drawCardinalSpline(int[] points, int[] imageArray)
		{
		int count = 0;
		while (count<points.length)
			{
			imageArray[points[count]] = 1;
			count++;
			}
		}

	}
