/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.lineage.util;

import java.util.*;

import javax.vecmath.Vector3d;

import endrov.lineage.Lineage;
import endrov.lineage.LineageSelParticle;
import endrov.util.EvGeomUtil;
import endrov.util.Tuple;

import qhull.Voronoi;
import qhull.VoronoiNeigh;

/**
 * Construct neighbourhood based on voronoi approximation
 * 
 * @author Johan Henriksson
 */
public class LineageVoronoi
	{
	public Voronoi vor;
	public Vector<String> nucnames;
	public Vector<Vector3d> nmid;
	public VoronoiNeigh vneigh;
	
	//nuc -> nuc -> area
	public Map<String,Map<String,Double>> contactArea=new HashMap<String, Map<String,Double>>();
	//nuc -> area
	public Map<String,Double> totArea=new HashMap<String, Double>();

	
	public LineageVoronoi(Map<LineageSelParticle, Lineage.InterpolatedParticle> inter, boolean selfNeigh) throws Exception
		{
		nucnames=new Vector<String>();
		nmid=new Vector<Vector3d>();
		for(Map.Entry<LineageSelParticle, Lineage.InterpolatedParticle> entry:inter.entrySet())
			{
			if(entry.getValue().isVisible())
				{
				nucnames.add(entry.getKey().snd());
				nmid.add(entry.getValue().pos.getPosCopy());
				}
			}
		vor=new Voronoi(nmid.toArray(new Vector3d[]{}));
		//System.out.println(vor.toString());
		
		HashSet<Integer> infinityCell=new HashSet<Integer>();
		for(int i=0;i<nucnames.size();i++)
			if(nucnames.get(i).startsWith(":::"))
				infinityCell.add(i);

		vneigh=new VoronoiNeigh(vor,selfNeigh,infinityCell);
		
		//Remove high angle neighbours facing infinity.
		//This is the Copan (see paper)
		HashSet<Integer> atInf=new HashSet<Integer>();
		for(int i=0;i<vor.vsimplex.size();i++)
			if(vor.isAtInfinity(i))
				atInf.add(i);
		for(Tuple<Integer, Integer> pair:getNeighPairSetIndex())
			if(atInf.contains(pair.fst()) || atInf.contains(pair.snd()))
				{
				//Common neighbours
				Set<Integer> neigh=new HashSet<Integer>(vneigh.dneigh.get(pair.fst()));
				neigh.retainAll(vneigh.dneigh.get(pair.snd()));
				neigh.remove(pair.fst());
				neigh.remove(pair.snd());
				
				//Check angles. The COPAN thing in the c.e model paper.
				for(int i:neigh)
					{
					double angle=EvGeomUtil.midAngle(vor.center[pair.fst()], vor.center[i], vor.center[pair.snd()]);
					if(angle>110.0*2*Math.PI/360.0)
						{
						vneigh.dneigh.get(pair.fst()).remove(pair.snd());
						vneigh.dneigh.get(pair.snd()).remove(pair.fst());
						break;
						}
					}
				}
		}
	
	
	
	public Set<Tuple<String, String>> getNeighPairSet()
		{
		Set<Tuple<String, String>> list=new HashSet<Tuple<String,String>>();
		for(int i=0;i<vneigh.dneigh.size();i++)
			for(int j:vneigh.dneigh.get(i))
				list.add(new Tuple<String, String>(nucnames.get(i),nucnames.get(j)));
		return list;
		}
	public Set<Tuple<Integer, Integer>> getNeighPairSetIndex()
		{
		Set<Tuple<Integer, Integer>> list=new HashSet<Tuple<Integer,Integer>>();
		for(int i=0;i<vneigh.dneigh.size();i++)
			for(int j:vneigh.dneigh.get(i))
				list.add(new Tuple<Integer, Integer>(i,j));
		return list;
		}
	
	/**
	 * Calculate contact areas
	 */
	public void calcContactArea()
		{
		//Prepare list
		for(int i=0;i<vneigh.dneigh.size();i++)
			contactArea.put(nucnames.get(i), new HashMap<String,Double>());
		
		
		//nuc-nuc areas
		for(int i=0;i<vneigh.dneigh.size();i++)
			for(int j:vneigh.dneigh.get(i))
				if(i!=j)
					{
					//Check if area will be finite
					double area;
					if(vor.isAtInfinity(i) && vor.isAtInfinity(j))
						area=0;
					else
						{
						//Obtain common surface
						HashSet<Integer> surf=new HashSet<Integer>();
						for(int v:vor.vsimplex.get(i))
							surf.add(v);
						HashSet<Integer> surfB=new HashSet<Integer>();
						for(int v:vor.vsimplex.get(j))
							surfB.add(v);
						surf.retainAll(surfB);

						//Calculate area
						Vector3d[] vv=new Vector3d[surf.size()];
						Iterator<Integer> it=surf.iterator();
						for(int ap=0;ap<surf.size();ap++)
							vv[ap]=vor.vvert.get(it.next());
						
						area=EvGeomUtil.polygonArea(EvGeomUtil.sortConvexPolygon(vv));
						}
					contactArea.get(nucnames.get(i)).put(nucnames.get(j), area);
					contactArea.get(nucnames.get(j)).put(nucnames.get(i), area);
					}
		
		//total areas
		for(int i=0;i<nucnames.size();i++)
			{
			String name=nucnames.get(i);
			double sum=0;
			for(double area:contactArea.get(name).values())
				if(area>0)
					sum+=area;
			totArea.put(name, vor.isAtInfinity(i) ? -sum : sum);

			System.out.println("tot "+name+" "+totArea.get(name));
			}

		}
	
	}
