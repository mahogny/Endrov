package qhull;

import java.util.*;

/**
 * Extract neighbours from voronoi. This is not a reflexive relation ie NOT a ~ a
 * @author Johan Henriksson
 */
public class VoronoiNeigh
	{
	public List<List<Integer>> dneigh=new ArrayList<List<Integer>>();
	
	
	public VoronoiNeigh(Voronoi v)
		{
		int numsimplex=v.vsimplex.size();
		for(int i=0;i<numsimplex;i++)
			dneigh.add(new ArrayList<Integer>());

		for(int i=0;i<numsimplex;i++)
			{
			HashSet<Integer> faceA=new HashSet<Integer>();
			for(int e:v.vsimplex.get(i))
				faceA.add(e);
			for(int j=i+1;j<numsimplex;j++)
					{
					for(int e:v.vsimplex.get(j))
						if(faceA.contains(e) && e!=-1)
							{
							dneigh.get(i).add(j);
							dneigh.get(j).add(i);
							
//							System.out.println("neigh "+i+" "+j);
							break;
							}
					}
			
//			for(int j:v.vface.get(i))
//				dneigh.get(i).add(j);
			}
		}

	}
