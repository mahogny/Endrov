package endrov.annotationNetwork;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Vector3d;

import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.Vector3i;


public abstract class NetworkTracerInterface
	{


	public static Collection<Vector3i> startingPointsFromFrame(EvStack stack, Network.NetworkFrame frame)
		{
		List<Vector3i> startingPoints=new LinkedList<Vector3i>();
		for(Network.Point p:frame.points.values())
			{
			Vector3d pi=stack.transformWorldImage(p.toVector3d());   
			int x=(int)pi.x;
			int y=(int)pi.y;
			int z=(int)pi.z;
			startingPoints.add(new Vector3i(x,y,z));
			}
		return startingPoints;
		}


	public abstract void preprocess(ProgressHandle progh, EvStack stack, Collection<Vector3i> startingPoints);
		
	
	public abstract List<Vector3i> findPathTo(int x, int y, int z);
	
	
	}
