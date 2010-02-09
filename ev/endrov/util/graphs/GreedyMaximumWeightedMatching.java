/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.graphs;

import java.util.*;

import org.jgrapht.Graph;


/**
 * Calculate the maximum weighted matching i.e. the subset of edges maximizing total weight,
 * such that none of the vertices are connected to more than one other vertex.
 * 
 * This is a greedy approximation
 * 
 * @author Johan Henriksson
 *
 */
public class GreedyMaximumWeightedMatching<V,E>
	{
	private List<E> out=new LinkedList<E>();
	
	
	public GreedyMaximumWeightedMatching(final Graph<V,E> graph)
		{
		ArrayList<E> sorted=new ArrayList<E>(graph.edgeSet());
		Collections.sort(sorted, new Comparator<E>(){
			public int compare(E arg0, E arg1)
				{
				return -Double.compare(graph.getEdgeWeight(arg0),graph.getEdgeWeight(arg1));
				}
		});
		
		HashSet<V> used=new HashSet<V>();
		
		for(E e:sorted)
			{
			V v1=graph.getEdgeSource(e);
			V v2=graph.getEdgeTarget(e);
			if(!used.contains(v1) && used.contains(v2))
				out.add(e);
			}
		}
	
	/**
	 * Get optimal subset of edges
	 */
	public Collection<E> getEdges()
		{
		return new LinkedList<E>(out);
		}
	
	
	}
