package endrov.worms.skeleton;

import java.util.ArrayList;
import endrov.imageset.EvPixels;
import endrov.util.Vector2i;
import endrov.worms.WormPixelMatcher;
import endrov.worms.skeleton.NotWormException;

/**
 * Class representing a 1 worm skeleton, conformed by its base points, and
 * skeleton points 
 * 
 * @author Javier Fernandez
 */
public class WormSkeleton extends Skeleton
	{
	int[] basePoints;
	ArrayList<Integer> skPoints;
	boolean[] isSkPoint;
	WormPixelMatcher wpm;

	public WormSkeleton(EvPixels image, int[] dt, int w, int h,
			ArrayList<Integer> basePoints, ArrayList<Integer> skPoints,
			WormPixelMatcher wpm) throws NotWormException
		{
		super(image, dt, w, h);
		if (basePoints.size()!=2)
			throw new NotWormException(
					"Wrong amount of base points. Must be exactly two");
		else
			{
			this.basePoints = new int[2];
			this.basePoints[0] = basePoints.get(0);
			this.basePoints[1] = basePoints.get(1);
			}
		this.skPoints = new ArrayList<Integer>(skPoints);
		this.isSkPoint = new boolean[wpm.getH()*wpm.getW()];
		this.isSkPoint = SkeletonUtils
				.listToMatrix(wpm.getH()*wpm.getW(), skPoints);
		this.wpm = wpm;
		}

	public void replaceSkPoint(int position, int point)
		{
		isSkPoint[skPoints.get(position)] = false;
		skPoints.set(position, point);
		isSkPoint[point] = true;
		}

	public WormSkeleton(WormClusterSkeleton wcs, WormPixelMatcher wpm)
			throws NotWormException
		{
		super(wcs.image, wcs.dt, wcs.w, wcs.h);
		if (wcs.getBasePoints().size()!=2)
			{
			System.out.println("NUM WORMS: "+wcs.numWorms);
			System.out.println("NUM BASE POINTS: "+wcs.getBasePoints());
			throw new NotWormException(
					"Wrong amount of base points. Must be exactly two");
			}
		else
			{
			ArrayList<Integer> bp = wcs.getBasePoints();
			this.basePoints = new int[2];
			this.basePoints[0] = bp.get(0);
			this.basePoints[1] = bp.get(1);
			}
		this.skPoints = new ArrayList<Integer>(wcs.getSkPoints());
		this.isSkPoint = new boolean[wpm.getH()*wpm.getW()];
		this.isSkPoint = SkeletonUtils.listToMatrix(wpm.getH()*wpm.getW(), wcs
				.getSkPoints());
		this.wpm = wpm;
		}

	public WormClusterSkeleton toWormClusterSkeleton()
		{
		ArrayList<Integer> base = new ArrayList<Integer>();
		base.add(basePoints[0]);
		base.add(basePoints[1]);
		return new WormClusterSkeleton(image, dt, w, h, base, skPoints, wpm);
		}

	public int[] getBasePoints()
		{
		return basePoints;
		}

	public ArrayList<Integer> getSkPoints()
		{
		return skPoints;
		}

	public boolean[] getIsSkPoint()
		{
		return isSkPoint;
		}

	public WormPixelMatcher getPixelMatcher()
		{
		return wpm;
		}

	public void setSkPoints(ArrayList<Integer> skPoints)
		{
		this.skPoints = new ArrayList<Integer>(skPoints);
		}

	public static Vector2i getMinMaxLength(int wormLength, double percMin,
			double percMax)
		{
		return new Vector2i((int) (wormLength*percMin), (int) (wormLength*percMax));
		}

	public static Vector2i getMinMaxLength(int wormLength)
		{
		return getMinMaxLength(wormLength, 0.70, 1.3);
		}

	}
