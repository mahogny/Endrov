/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.clusteringOld;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Vector3d;

/**
 * A cluster of points
 * TODO check that it makes sense
 * @author Johan Henriksson
 *
 */
public class PointCluster
	{
	public Vector3d v; //Representative point of cluster
	public int numpoint;
	private double radius=0;
	public List<PointCluster> subpoint=new LinkedList<PointCluster>();
	
	public PointCluster(){}
	public PointCluster(Vector3d v)
		{
		this.v=v;
		numpoint=1;
		}
	
	
	/**
	 * Collect points into linear list, until a minimal radius is found
	 */
	public void collectPoint(List<PointCluster> outp, double minrad)
		{
		if(radius>=minrad)
			{
			outp.add(this);
			for(PointCluster sp:subpoint)
				sp.collectPoint(outp,minrad);
			}
		}

	/**
	 * Calculate radius
	 * TODO check if it makes sense...
	 */
	public void calcRadius(int depth, double joindist)
		{
		List<PointCluster> child=new LinkedList<PointCluster>();
		collectPoint(child,-1);
		Double maxrad=null;
		for(PointCluster sp:child)
			{
			Vector3d tv=new Vector3d(v);
			tv.sub(sp.v);
			double len=tv.lengthSquared();
			if(maxrad==null || maxrad<len)
				maxrad=len;
			}
		radius=maxrad==null ? 0 : Math.sqrt(maxrad);
		if(radius>=joindist && subpoint.isEmpty())
			{
			for(int i=0;i<depth;i++)
				System.out.print(" ");
			System.out.println(radius);
			}
		for(PointCluster sp:subpoint)
			sp.calcRadius(depth+1,joindist);
		}
	}
