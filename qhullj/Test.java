import javax.vecmath.Vector3d;

import qhull.VoronoiResult;


public class Test
	{

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		VoronoiResult result=new VoronoiResult();
		Vector3d[] points=new Vector3d[]{new Vector3d(1,0,0),new Vector3d(0,1,0),new Vector3d(1,0,1),new Vector3d(1,1,1),new Vector3d(1,10,0)};
		qhull.QHull.voronoi(result, points);

		}

	}
