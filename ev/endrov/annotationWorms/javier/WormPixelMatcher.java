package endrov.annotationWorms.javier;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector2d;

import com.graphbuilder.curve.Point;

import endrov.util.Vector2i;
import endrov.util.curves.PointFactory;

/**
 * Class that represents a matching matrix containing the corresponding (x,y)
 * coordinates of an EvPixels array point.
 * 
 * @author Javier Fernandez
 */
public class WormPixelMatcher
	{
	private Vector2i[] matchMatrix;
	int w;
	int h;

	public WormPixelMatcher(int w, int h)
		{
		this.w = w;
		this.h = h;
		matchMatrix = new Vector2i[w*h];
		int count = 0;
		for (int j = 0; j<h; j++)
			{
			for (int i = 0; i<w; i++)
				{
				matchMatrix[count] = new Vector2i(i, j);
				count++;
				}
			}
		}

	public int getH()
		{
		return h;
		}

	public int getW()
		{
		return w;
		}

	public int posToPixel(Vector2i pos)
		{
		return ((pos.x)+pos.y*w);
		}

	public int posToPixel(Vector2d pos)
		{
		return (((int) pos.x)+((int) pos.y)*w);
		}

	/**
	 * Returns 2-dimensional position corresponding to pixel position
	 */
	public Vector2i getPixelPos(int pixel)
		{
		return matchMatrix[pixel];
		}

	/**
	 * Returns 2-dimensional position corresponding to pixel position
	 */
	public Vector2i getPixelPos(Point p)
		{
		return matchMatrix[pointToPixel(p)];
		}

	public int pointToPixel(Point p)
		{
		double[] xy = p.getLocation();
		return (((int) xy[0])+((int) (xy[1]))*w);
		}

	/**
	 * Returns a Point representation corresponding to the position pixel,
	 * corresponding to the worm image
	 * 
	 * @param pixel
	 * @return
	 */
	public Point pixelToPoint(int pixel)
		{
		Vector2i pixel2D = getPixelPos(pixel);
		return (new PointFactory()).createPoint(pixel2D.x, pixel2D.y);
		}

	/**
	 * Returns a list of Point transforming each integer point in the given
	 * integer list points
	 * 
	 * @param points
	 *          List of integer points
	 */
	public ArrayList<Point> pixelListToPoint(ArrayList<Integer> points)
		{
		ArrayList<Point> pointList = new ArrayList<Point>(points.size());
		Iterator<Integer> pIt = points.iterator();
		while (pIt.hasNext())
			{
			pointList.add(pixelToPoint(pIt.next()));
			}
		return pointList;
		}

	public ArrayList<Vector2i> pixelListToVector2i(ArrayList<Integer> points)
		{
		ArrayList<Vector2i> vList = new ArrayList<Vector2i>(points.size());
		Iterator<Integer> pIt = points.iterator();
		int count = 0;
		while (pIt.hasNext())
			{
			vList.add(getPixelPos(pIt.next()));
			count++;
			}
		return vList;
		}

	public ArrayList<Vector2d> pixelListToVector2d(ArrayList<Integer> points)
		{
		ArrayList<Vector2d> vList = new ArrayList<Vector2d>(points.size());
		Iterator<Integer> pIt = points.iterator();
		Vector2i n;
		while (pIt.hasNext())
			{
			n = getPixelPos(pIt.next());
			vList.add(new Vector2d(n.x, n.y));
			}
		return vList;
		}

	public float[][] pixelListTo2DArray(ArrayList<Integer> points)
		{
		float vArray[][] = new float[points.size()][2];
		Iterator<Integer> pIt = points.iterator();
		Vector2i temp;
		int count = 0;
		while (pIt.hasNext())
			{
			temp = getPixelPos(pIt.next());
			vArray[count][0] = temp.x;
			vArray[count][1] = temp.y;
			count++;
			}
		return vArray;
		}

	/**
	 * Returns an array of int transforming each Point from points list to the
	 * corresponding integer matrix value
	 */

	public int[] pointListToPixel(ArrayList<Point> points)
		{
		int[] pixels = new int[points.size()];
		Iterator<Point> it = points.iterator();
		int count = 0;
		while (it.hasNext())
			{
			pixels[count] = pointToPixel(it.next());
			count++;
			}
		return pixels;
		}

	public ArrayList<Integer> pointListToPixelList(ArrayList<Point> points)
		{
		ArrayList<Integer> pixels = new ArrayList<Integer>(points.size());
		Iterator<Point> it = points.iterator();
		while (it.hasNext())
			{
			pixels.add(pointToPixel(it.next()));
			}
		return pixels;
		}

	/**
	 * Transform a int array containing the two base points into a Point array
	 */

	public ArrayList<Point> baseToPoint(int[] basePoints)
		{
		if (basePoints.length!=2)
			return null;
		ArrayList<Point> pl = new ArrayList<Point>(2);
		pl.add(pixelToPoint(basePoints[0]));
		pl.add(pixelToPoint(basePoints[1]));

		return pl;
		}

	public static double calculatePixelDistance(int p1, int p2,
			WormPixelMatcher wpm)
		{
		Vector2i p1c = wpm.getPixelPos(p1);
		Vector2i p2c = wpm.getPixelPos(p2);
		double distance = Math.sqrt(Math.pow(p1c.x-p2c.x, 2)
				+Math.pow(p1c.y-p2c.y, 2));
		return distance;
		}

	}
