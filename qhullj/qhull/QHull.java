package qhull;

import javax.vecmath.Vector3d;

public class QHull
	{
	private static String lock="foo";

	static
		{
		System.loadLibrary("qhullj");
		}

	public static boolean voronoi(VoronoiResult result, Vector3d[] points)
		{
		synchronized (lock)
			{
			return voronoi_(result, points);
			}
		}
	
	private static native boolean voronoi_(VoronoiResult result, Vector3d[] points);

	}
