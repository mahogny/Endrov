package util2.rfpTrack;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.vecmath.Vector3d;


import endrov.ev.Tuple;

public class Cluster
	{

	public Set<OnePoint> points=new HashSet<OnePoint>();
	public TreeMap<UniqueDouble, Tuple<OnePoint, OnePoint>> distances=new TreeMap<UniqueDouble, Tuple<OnePoint, OnePoint>>();
	//better: new class for double that allows duplicates

	public double joindist=25;

	
	public static class UniqueDouble implements Comparable<UniqueDouble>
		{
		private static int idpool=0;
		private double d;
		private int id;
		
		public UniqueDouble(double d)
			{
			this.d=d;
			id=idpool++;
			}

		public int compareTo(UniqueDouble o)
			{
			if(d<o.d)
				return -1;
			else if(d>o.d)
				return 1;
			else if(id<o.id)
				return -1;
			else if(id>o.id)
				return 1;
			else
				return 0;
			}
		
		public double toDouble()
			{
			return d;
			}
		}
	
	public class OnePoint
		{
		public Vector3d v;
		int numpoint;
		double radius=0;
		List<OnePoint> subpoint=new LinkedList<OnePoint>();
		}

	
	
	private void addDistance(OnePoint p1, OnePoint p2)
		{
		Vector3d w=new Vector3d(p1.v);
		w.sub(p2.v);
		Tuple<OnePoint, OnePoint> t=new Tuple<OnePoint, OnePoint>(p1,p2);
		distances.put(new UniqueDouble(w.length()),t);
		}

	
	private void collectPoint(OnePoint p, List<OnePoint> outp, double minrad)
		{
		if(p.radius>=minrad)
			{
			outp.add(p);
			for(OnePoint sp:p.subpoint)
				collectPoint(sp, outp,minrad);
			}
		}
	
	private boolean collectLeaf(OnePoint p, List<OnePoint> outp, double minrad)
		{
		if(p.radius>=minrad)
			{
			
			
			if(p.subpoint.isEmpty())
				{
				outp.add(p);
				return true;
				}
			else
				{
				boolean anyadded=false;
				for(OnePoint sp:p.subpoint)
					anyadded|=collectLeaf(sp, outp,minrad);
				if(!anyadded)
					outp.add(p);
				return true;
				}
			}
		else
			return false;
		}
	
	public void calcRadius(OnePoint p, int depth)
		{
		List<OnePoint> child=new LinkedList<OnePoint>();
		collectPoint(p,child,-1);
		Double maxrad=null;
		for(OnePoint sp:child)
			{
			Vector3d v=new Vector3d(p.v);
			v.sub(sp.v);
			double len=v.lengthSquared();
			if(maxrad==null || maxrad<len)
				maxrad=len;
			}
		p.radius=maxrad==null ? 0 : Math.sqrt(maxrad);
//		if(p.radius>=joindist)
		if(p.radius>=joindist && p.subpoint.isEmpty())
			{
			for(int i=0;i<depth;i++)
				System.out.print(" ");
			System.out.println(p.radius);//+"    # "+child.size()+"     "+p.v.x+" "+p.v.y+" "+p.v.z);
			}
		for(OnePoint sp:p.subpoint)
			calcRadius(sp,depth+1);
		}


	
	
	public double[][] cluster(double[][] arr)
		{
//		System.out.println(""+arr.length+" "+arr[0].length);
		//Dimension y,x
		System.out.println("# points"+arr.length);

		//Add all points (x,y,z)
		for(int i=0;i<arr.length;i++)
			{
			OnePoint p=new OnePoint();
			p.v=new Vector3d(arr[i][0],arr[i][1],arr[i][2]);
			p.numpoint=1;
			points.add(p);
			}
		
		//Insert all
		for(OnePoint p1:points)
			for(OnePoint p2:points)
				if(p1!=p2)
					addDistance(p1, p2);

		//Eliminate
//		while(key<joindist)
		while(points.size()>1)
			{
			UniqueDouble key=distances.firstKey();
			Tuple<OnePoint, OnePoint> t=distances.get(key);
			distances.remove(key);
			if(points.contains(t.fst()) && points.contains(t.snd()))
				{
//				System.out.println("deleting, key "+key);
				//Too close, merge
				Vector3d v1=new Vector3d(t.fst().v);
				v1.scale(t.fst().numpoint);
				Vector3d v2=new Vector3d(t.snd().v);
				v2.scale(t.snd().numpoint);
				v1.add(v2);
				v1.scale(1.0/(t.fst().numpoint+t.snd().numpoint));
//				v1.scale(0.5);
				
				OnePoint p=new OnePoint();
				p.v=v1;
				p.numpoint=t.fst().numpoint+t.snd().numpoint;
				p.subpoint.add(t.fst());
				p.subpoint.add(t.snd());
				
				points.remove(t.fst());
				points.remove(t.snd());				
				points.add(p);
				
				for(OnePoint p2:points)
					if(p!=p2)
						addDistance(p, p2);
				}
//			key=distances.firstKey();
			}
	
		List<OnePoint> outp=new LinkedList<OnePoint>();
		
//		List<List<Double>> outp=new LinkedList<List<Double>>();
		if(points.size()==1)
			{
			calcRadius(points.iterator().next(),0);
	//		collectPointArray(points.iterator().next(),outp);
			collectLeaf(points.iterator().next(),outp,joindist);
			}
		double arr2[][]=new double[outp.size()][4];
		int pi=0;
		for(OnePoint p:outp)
			{
			arr2[pi][0]=p.v.x;
			arr2[pi][1]=p.v.y;
			arr2[pi][2]=p.v.z;
			arr2[pi][3]=p.radius;
			pi++;
			}

		
		
/*		
		double arr2[][]=new double[points.size()][3];
		int pi=0;
		for(OnePoint p:points)
			{
			arr2[pi][0]=p.v.x;
			arr2[pi][1]=p.v.y;
			arr2[pi][2]=p.v.z;
			pi++;
			}
	*/	
		
		System.out.println(""+distances.firstKey().toDouble());
		
		
		return arr2;
		}
	
	
	}
