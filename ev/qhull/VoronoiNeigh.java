package qhull;

import java.util.*;

/**
 * Extract neighbours from voronoi. This is not a reflexive relation ie NOT a ~ a
 * @author Johan Henriksson
 */
public class VoronoiNeigh
	{
	/**
	 * Which are neighbours? index list. a ~ a does not hold.
	 */
	public List<Set<Integer>> dneigh=new ArrayList<Set<Integer>>();
	
	
	public VoronoiNeigh(Voronoi v, boolean selfNeigh, Set<Integer> infinityCell)
		{
		int used=0;
		
		int numsimplex=v.vsimplex.size();
		for(int i=0;i<numsimplex;i++)
			dneigh.add(new HashSet<Integer>());

		Set<Integer> infinityVertex=new HashSet<Integer>();
		infinityVertex.add(-1);
		for(int c:infinityCell)
			for(int i:v.vsimplex.get(c))
				infinityVertex.add(i);
		
		
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
				boolean was=!faceB.isEmpty();
				faceB.removeAll(infinityVertex);

				if(faceB.isEmpty() && was)
					used++;
				
				//Face shared?
				if(!faceB.isEmpty())
					{
					dneigh.get(i).add(j);
					dneigh.get(j).add(i);
					}

				}

			//			for(int j:v.vface.get(i))
			//				dneigh.get(i).add(j);
			}
		System.out.println("used "+used);
		}

	}
