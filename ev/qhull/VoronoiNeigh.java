package qhull;

import java.util.*;

/**
 * Extract neighbours from voronoi i.e. the delaunay dual
 * 
 * @author Johan Henriksson
 */
public class VoronoiNeigh
	{
	/** 
	 * Which are neighbours? dneigh.get(i) is a list of neighbours of i. These are indices
	 * into vertex list  
	 */
	public ArrayList<Set<Integer>> dneigh=new ArrayList<Set<Integer>>();
	
	
	public VoronoiNeigh(Voronoi v, boolean selfNeigh, Collection<Integer> infinityCell)
		{
		int numsimplex=v.vsimplex.size();
		for(int i=0;i<numsimplex;i++)
			dneigh.add(new HashSet<Integer>());

		//Set virtual infinity
		v.setInfinityCell(infinityCell);
		
		for(int i=0;i<numsimplex;i++) //Simplex A
			{
			//Face A vertices
			HashSet<Integer> faceA=new HashSet<Integer>();
			for(int e:v.vsimplex.get(i))
				faceA.add(e);
			
			//X ~ X relation
			if(selfNeigh)
				dneigh.get(i).add(i);
			
			for(int j=i+1;j<numsimplex;j++) //Simplex B
				{
				//Face B vertices
				HashSet<Integer> faceB=new HashSet<Integer>();
				for(int e:v.vsimplex.get(j))
					faceB.add(e);
				//Find common face 
				faceB.retainAll(faceA);

				//Ignore points at infinity
				faceB.remove(-1);
				//3 vert before. cuts too many.
				
				//Face shared?
				if(faceB.size()>=1)
					{
					dneigh.get(i).add(j);
					dneigh.get(j).add(i);
					}

				}

			}
		}

	}
