package endrov.nuc;

import java.util.*;

import javax.vecmath.Vector3d;

import endrov.util.Tuple;

import qhull.Voronoi;
import qhull.VoronoiNeigh;

/**
 * Construct neighbourhood based on voronoi approximation
 * 
 * @author Johan Henriksson
 */
public class NucVoronoi
	{
	public Voronoi vor;
	public Vector<String> nucnames;
	public Vector<Vector3d> nmid;
	public VoronoiNeigh vneigh;
	
	//vert -> vert -> -> area
	public Map<String,Map<String,SortedSet<Double>>> contactsf=new TreeMap<String, Map<String,SortedSet<Double>>>();

	
	public NucVoronoi(Map<NucPair, NucLineage.NucInterp> inter, boolean selfNeigh) throws Exception
		{
		nucnames=new Vector<String>();
		nmid=new Vector<Vector3d>();
		for(Map.Entry<NucPair, NucLineage.NucInterp> entry:inter.entrySet())
			{
			//NucLineage.Nuc nuc=entry.getKey().fst().nuc.get(entry.getKey().snd());
			//int curframe=100;
			//if(nuc.firstFrame()<=curframe && nuc.lastFrame()>=curframe)
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
		
/*		for(int i=0;i<nucnames.size();i++)
			{
			String nucName=nucnames.get(i);
			if(nucName.startsWith(":::"))
				for(Set<Integer> dneigh:vneigh.dneigh)
					dneigh.remove(i);
			}*/
		}
	
	public Set<Tuple<String, String>> getNeighPairSet()
		{
		Set<Tuple<String, String>> list=new HashSet<Tuple<String,String>>();
		for(int i=0;i<vneigh.dneigh.size();i++)
			{
			for(int j:vneigh.dneigh.get(i))
				list.add(new Tuple<String, String>(nucnames.get(i),nucnames.get(j)));
			}
		return list;
		}
	
	
	public void calcNeighArea()
		{
		for(int i=0;i<vneigh.dneigh.size();i++)
			for(int j:vneigh.dneigh.get(i))
				{
				//Obtain common surface
				HashSet<Integer> surf=new HashSet<Integer>();
				for(int v:vor.vsimplex.get(i))
					surf.add(v);
				HashSet<Integer> surfB=new HashSet<Integer>();
				for(int v:vor.vsimplex.get(j))
					surfB.add(v);
				surf.retainAll(surfB);
				
				//
				
				}
		
		}
	
	}
