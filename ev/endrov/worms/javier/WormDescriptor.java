package endrov.worms.javier;

import endrov.tesselation.PolygonRasterizer;
import endrov.tesselation.utils.Line;
import endrov.util.ImVector2;
import endrov.util.Vector2i;
import endrov.util.curves.EvCardinalSpline;
import endrov.worms.javier.skeleton.WormSkeleton;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector2d;

import com.graphbuilder.curve.CardinalSpline;
import com.graphbuilder.curve.Point;

/**
 * Geometrical descriptor for worm shapes
 * 
 * @author Javier Fernandez
 *
 */

public class WormDescriptor implements Cloneable
	{

	WormPixelMatcher wpm;
	WormProfile wprof;
	public int[] controlPoints;

	int[] dtArray;
	ImVector2[] angleNorthVector;
	ImVector2[] angleSouthVector;
	public int[][] angleNorthLine;
	public int[][] angleSouthLine;
	int numPoints;

	/**
	 * Creates a new worm descriptor given a WormPixelMatcher object that
	 * represents the current worm as an Endrov image, and Worm skeleton from
	 * which the corresponding spline curve is calculated. This constructor uses
	 * the default values 0.09 for the percentage of control points used and 0.5
	 * as alpha value.
	 * 
	 * @param wm
	 *          Representation of the worm as Endrov image
	 * @param ws
	 *          The skeleton of the worm
	 */

	public WormDescriptor(WormProfile wprof, WormSkeleton ws, int dtArray[],
			int numPoints, double bendingLength)
		{
		this.wpm = wprof.wpm;
		this.wprof = wprof;
		this.dtArray = dtArray.clone();
		this.angleNorthVector = new ImVector2[wprof.thickness.length];
		this.angleSouthVector = new ImVector2[wprof.thickness.length];
		this.angleNorthLine = new int[wprof.thickness.length][];
		this.angleSouthLine = new int[wprof.thickness.length][];
		this.numPoints = numPoints;

		CardinalSpline cs = getShapeSpline(ws, 0.5, 0.09);
		ArrayList<Point> cPoints = EvCardinalSpline
				.getCardinalPoints(cs, numPoints);
		this.controlPoints = wprof.wpm.pointListToPixel(cPoints);

		// calculate angle bisection lines
		// Calculate average distance to contour points and add to thickness
		Vector2i pos1;
		Vector2i pos2;
		for (int i = 1; i<this.controlPoints.length-1; i++)
			{
			Vector2i[] extremes = WormProfile
					.getExtremes(dtArray, this.wpm, wpm
							.getPixelPos(this.controlPoints[i-1]), wpm
							.getPixelPos(this.controlPoints[i]), wpm
							.getPixelPos(this.controlPoints[i+1]),
							dtArray[this.controlPoints[i]]);
			// cp->extreme[0]
			pos1 = wpm.getPixelPos(this.controlPoints[i]);
			pos2 = extremes[0];
			ImVector2 v1 = new ImVector2((double) pos2.x-pos1.x, (double) pos2.y
					-pos1.y);
			v1 = v1.normalize();
			this.angleNorthVector[i] = v1;
			v1 = v1.mul(bendingLength);

			// cp->extreme[1]
			pos2 = extremes[1];
			ImVector2 v2 = new ImVector2((double) pos2.x-pos1.x, (double) pos2.y
					-pos1.y);
			v2 = v2.normalize();
			this.angleSouthVector[i] = v2;
			v2 = v2.mul(bendingLength);

			Line l1 = new Line(pos1, new Vector2i((int) Math.round(v1.x+pos1.x),
					(int) Math.round(v1.y+pos1.y)));
			Line l2 = new Line(pos1, new Vector2i((int) Math.round(v2.x+pos1.x),
					(int) Math.round(v2.y+pos1.y)));

			ArrayList<Integer> linePoints = l1.getLinePoints(wpm.w);
			Iterator<Integer> lit = linePoints.iterator();

			int[] intL = new int[linePoints.size()];
			int count = 0;
			while (lit.hasNext())
				{
				intL[count] = lit.next();
				count++;
				}
			this.angleNorthLine[i] = intL;

			linePoints = l2.getLinePoints(wpm.w);
			lit = linePoints.iterator();
			intL = new int[linePoints.size()];
			count = 0;
			while (lit.hasNext())
				{
				intL[count] = lit.next();
				count++;
				}
			this.angleSouthLine[i] = intL;;
			}

		}

	/**
	 * Returns the pixels belonging to the area of the worm defined
	 * by the calling worm descriptor
	 */
	public ArrayList<Integer> rasterizeWorm()
		{
		ArrayList<Integer> skp = WormProfile.constructShape(controlPoints, wprof,
				0, dtArray);
		ArrayList<Vector2d> tpv = wpm.pixelListToVector2d(skp);
		return PolygonRasterizer.rasterize(wpm.w, wpm.h, tpv);
		}

	/**
	 * Returns the pixels belonging to the area of the worm defined
	 * by the calling worm descriptor. The contour of the worm 
	 * is initially generated following the worm descriptor, and then the
	 * control-contour points are expanded or contracted to fit the contours
	 * defined by the distance transform, to obtain a more realistic and less
	 * generic worm shape. 
	 */
	public ArrayList<Integer> fitAndRasterizeWorm()
		{
		ArrayList<Integer> skp = WormProfile.expandingConstructShape(controlPoints,
				wprof, 0, dtArray);
		// return skp;
		ArrayList<Vector2d> tpv = wpm.pixelListToVector2d(skp);
		return PolygonRasterizer.rasterize(wpm.w, wpm.h, tpv);
		}

	/**
	 * Draws the worm descriptor indicating bisectors and angles.
	 */
	public ArrayList<Integer> drawAngles()
		{
		ArrayList<Integer> points = new ArrayList<Integer>();
		int[] lp;
		// North Angle
		for (int i = 1; i<angleNorthLine.length-1; i++)
			{
			lp = angleNorthLine[i];
			for (int j = 0; j<lp.length; j++)
				{
				points.add(lp[j]);
				}
			}
		// South angle
		for (int i = 1; i<angleSouthLine.length-1; i++)
			{
			lp = angleSouthLine[i];
			for (int j = 0; j<lp.length; j++)
				{
				points.add(lp[j]);
				}
			}

		// line connecting control points
		for (int i = 0; i<wprof.thickness.length-1; i++)
			{
			Line l = new Line(wpm.getPixelPos(controlPoints[i]), wpm
					.getPixelPos(controlPoints[i+1]));
			ArrayList<Integer> lpoints = l.getLinePoints(wpm.w);
			Iterator<Integer> lit = lpoints.iterator();
			while (lit.hasNext())
				{
				points.add(lit.next());
				}
			}
		return points;
		}

	public void updateCP(int position, int value)
		{
		this.controlPoints[position] = value;
		}

	/**
	 * Calculates a cardinal spline that starts in one base point and ends in the
	 * other one, passing through the points defined at the points given in the
	 * worm Skeleton
	 * 
	 * @param alpha
	 *          slack value
	 * @param numPointsPercentage
	 *          Number of points from 'points' that want to be consider as control
	 *          points
	 * @return
	 */
	public static CardinalSpline getShapeSpline(WormSkeleton ws, double alpha,
			double numPointsPercentage)
		{
		ArrayList<Point> bases = ws.getPixelMatcher().baseToPoint(
				ws.getBasePoints());
		ArrayList<Point> points = ws.getPixelMatcher().pixelListToPoint(
				ws.getSkPoints());
		return EvCardinalSpline.getShapeSpline(bases, points, alpha,
				numPointsPercentage);
		}

	public WormPixelMatcher getWpm()
		{
		return wpm;
		}

	public WormProfile getWprof()
		{
		return wprof;
		}

	public int[] getControlPoints()
		{
		return controlPoints;
		}

	public int[] getDtArray()
		{
		return dtArray;
		}

	public ImVector2[] getAngleNorthVector()
		{
		return angleNorthVector;
		}

	public ImVector2[] getAngleSouthVector()
		{
		return angleSouthVector;
		}

	public int[][] getAngleNorthLine()
		{
		return angleNorthLine;
		}

	public int[][] getAngleSouthLine()
		{
		return angleSouthLine;
		}

	public int getNumPoints()
		{
		return numPoints;
		}

	}
