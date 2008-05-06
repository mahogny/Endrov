package util2.rfpTrack;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import javax.vecmath.Vector3d;

import evplugin.ev.Tuple;

public class Cluster
	{

	public Set<OnePoint> points=new HashSet<OnePoint>();
	public TreeMap<Double, Tuple<OnePoint, OnePoint>> distances=new TreeMap<Double, Tuple<OnePoint, OnePoint>>();
	
	
	
	public class OnePoint
		{
		public Vector3d v;
		//Could keep list of points here to average better. or just a number
		}

	
	
	private void addDistance(OnePoint p1, OnePoint p2)
		{
		Vector3d w=new Vector3d(p1.v);
		w.sub(p2.v);
		Tuple<OnePoint, OnePoint> t=new Tuple<OnePoint, OnePoint>(p1,p2);
		distances.put(w.length(),t);
		}

	public double joindist=40;
	
	public double[][] cluster(double[][] arr)
		{
		System.out.println(""+arr.length+" "+arr[0].length);
		//Dimension y,x

		//Add all points (x,y,z)
		for(int i=0;i<arr.length;i++)
			{
			OnePoint p=new OnePoint();
			p.v=new Vector3d(arr[i][0],arr[i][1],arr[i][2]);
			points.add(p);
			}
		
		//Insert all
		for(OnePoint p1:points)
			for(OnePoint p2:points)
				if(p1!=p2)
					addDistance(p1, p2);

		//Eliminate
		double key=distances.firstKey();
		while(key<joindist)
			{
			Tuple<OnePoint, OnePoint> t=distances.get(key);
			distances.remove(key);
			if(points.contains(t.fst()) && points.contains(t.snd()))
				{
				System.out.println("deleting, key "+key);
				//Too close, merge
				Vector3d v=new Vector3d(t.fst().v);
				v.add(t.snd().v);
				v.scale(0.5);
				OnePoint p=new OnePoint();
				p.v=v;
				points.remove(t.fst());
				points.remove(t.snd());
				points.add(p);
				for(OnePoint p2:points)
					if(p!=p2)
						addDistance(p, p2);
				}
			key=distances.firstKey();
			}
		
		double arr2[][]=new double[points.size()][3];
		int pi=0;
		for(OnePoint p:points)
			{
			arr2[pi][0]=p.v.x;
			arr2[pi][1]=p.v.y;
			arr2[pi][2]=p.v.z;
			pi++;
			}
		
		
		System.out.println(""+distances.firstKey());
		
		
		
		return arr2;
		}
	
	
	}
