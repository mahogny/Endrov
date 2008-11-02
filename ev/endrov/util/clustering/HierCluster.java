package endrov.util.clustering;

import java.util.*;

import javax.vecmath.Vector3d;


import endrov.util.Tuple;

/**
 * Hierarchial clustering.
 * TODO refactor, test
 * TODO nearest and furthest neighbour
 * 
 * @author Johan Henriksson
 *
 */
public class HierCluster
	{

	public TreeMap<UniqueDouble, Tuple<PointCluster, PointCluster>> distances=new TreeMap<UniqueDouble, Tuple<PointCluster, PointCluster>>();
	//better: new class for double that allows duplicates

	

	//hm. is this correct?
	/**
	 * Unique doubles
	 */
	public static class UniqueDouble implements Comparable<UniqueDouble>
		{
//		private static int idpool=0;
		private double d;
		//private int id;
		
		
		public UniqueDouble(double d)
			{
			this.d=d;
//			id=idpool++;
			}

		public int compareTo(UniqueDouble o)
			{
			if(d<o.d)
				return -1;
			else if(d>o.d)
				return 1;
			else
				{
				//This relies on hashcode being object address,
				//which is not ok. but the earlier solution with
				//id was not ok either.
				int h1=hashCode();
				int h2=o.hashCode();
				if(h1<h2)
					return -1;
				else if(h1>h2)
					return 1;
				else
					return 0;
				}
			}
		
		public double toDouble()
			{
			return d;
			}
		}
	
	
	private void addDistance(PointCluster p1, PointCluster p2)
		{
		Vector3d w=new Vector3d(p1.v);
		w.sub(p2.v);
		Tuple<PointCluster, PointCluster> t=new Tuple<PointCluster, PointCluster>(p1,p2);
		distances.put(new UniqueDouble(w.length()),t);
		}

	/**
	 * Cluster, given array of (x,y,z)
	 */
	public PointCluster cluster(double[][] arr)
		{
		LinkedList<PointCluster> pc=new LinkedList<PointCluster>();
		for(int i=0;i<arr.length;i++)
			pc.add(new PointCluster(new Vector3d(arr[i][0],arr[i][1],arr[i][2])));
		return cluster(pc);
		}

	public PointCluster clusterV3d(List<Vector3d> points)
		{
		LinkedList<PointCluster> pc=new LinkedList<PointCluster>();
		for(Vector3d v:points)
			pc.add(new PointCluster(v));
		return cluster(pc);
		}
	public PointCluster cluster(List<PointCluster> points)
		{
		//Insert all
		//TODO optimize
		for(PointCluster p1:points)
			for(PointCluster p2:points)
				if(p1!=p2)
					addDistance(p1, p2);

		//Eliminate
		while(points.size()>1)
			{
			UniqueDouble key=distances.firstKey();
			Tuple<PointCluster, PointCluster> t=distances.get(key);
			distances.remove(key);
			if(points.contains(t.fst()) && points.contains(t.snd()))
				{
				//Too close, merge
				Vector3d v1=new Vector3d(t.fst().v);
				v1.scale(t.fst().numpoint);
				Vector3d v2=new Vector3d(t.snd().v);
				v2.scale(t.snd().numpoint);
				v1.add(v2);
				v1.scale(1.0/(t.fst().numpoint+t.snd().numpoint));
				
				PointCluster p=new PointCluster();
				p.v=v1;
				p.numpoint=t.fst().numpoint+t.snd().numpoint;
				p.subpoint.add(t.fst());
				p.subpoint.add(t.snd());
				
				points.remove(t.fst());
				points.remove(t.snd());				
				points.add(p);
				
				for(PointCluster p2:points)
					if(p!=p2)
						addDistance(p, p2);
				}
			}
	
		return points.iterator().next();
		}
	
	
	}
