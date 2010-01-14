/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Partitioning, or equivalence relation between elements. The class will help computing
 * the total equivalence (compute transitivity) given a few equivalences.
 * 
 * Worst-case complexity: Upper bound, O(n) group joins, O(n) for one group join.
 * Lookup is O(1).
 * 
 * For structured partitioning on images, likely O(n) group joins, but most joins cost only O(1), so linear cost. 
 * 
 * There is an alternative implementation with O(1) join and O(log n) lookup. It can be optimized (constant time)
 * with path compression.
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class Partitioning<E>
	{
	private static class Partition<E>
		{
		HashSet<E> members=new HashSet<E>(); 
		}
		
	//private HashSet<Partition<E>> partitions=new HashSet<Partition<E>>();
	private HashMap<E, Partition<E>> ep=new HashMap<E, Partition<E>>(); 
	
	/*
	 * Add elements and partitions from another partitioning.
	 * Assumes elements disjoint. Will copy links, the other partition will be destroyed.
	 */
	/*public void addPartitionsNoMerge(Partitioning<E> p)
		{
		
		}*/
	
	/**
	 * Create an element with no initial relations except reflexivity.
	 * Checks if it exists first, will not disrupt relations
	 */
	public void createElement(E e)
		{
		if(!ep.containsKey(e))
			{
			Partition<E> p=new Partition<E>();
			p.members.add(e);
			//partitions.add(p);
			ep.put(e,p);
			}
		}
	
	/**
	 * Specify two elements as equivalent. Must have been created before.
	 */
	public void specifyEquivalent(E a, E b)
		{
		Partition<E> pa=ep.get(a);
		Partition<E> pb=ep.get(b);
		pa.members.addAll(pb.members);
		for(E e:pb.members)
			ep.put(e,pa);
		//partitions.remove(pb);
		}

	/**
	 * Specify two elements as equivalent. Create these elements if they did not exist before.
	 * This function is heavily optimized.
	 */
	public void createSpecifyEquivalent(E a, E b)
		{
		/*
		//Slow but correct 
		createElement(a);
		createElement(b);
		specifyEquivalent(a, b);
		*/
		
		Partition<E> pa=ep.get(a);
		Partition<E> pb=ep.get(b);
		if(pa==null)
			{
			if(pb==null)
				{
				//Create a new partition with 2 members
				Partition<E> p=new Partition<E>();
				p.members.add(a);
				p.members.add(b);
				//partitions.add(p);
				ep.put(a,p);
				ep.put(b,p);
				}
			else
				{
				//Add point to partition
				pb.members.add(a);
				ep.put(a,pb);
				}
			}
		else
			{
			if(pb==null)
				{
				//Add point to partition
				pa.members.add(b);
				ep.put(b,pa);
				}
			else
				{
				//Optimization: Make sure they are not already merged
				if(pa!=pb)
					{
					//TODO put the smaller in the larger
					pa.members.addAll(pb.members);
					for(E e:pb.members)
						ep.put(e,pa);
					}
				}
			}
		
		}
	
	/**
	 * Specify two existing elements as equivalent. Might be slower than automatically creating new partitions
	 */
	public void existingSpecifyEquivalent(E a, E b)
		{
		Partition<E> pa=ep.get(a);
		Partition<E> pb=ep.get(b);
		//Merge two partitions
		pa.members.addAll(pb.members);
		for(E e:pb.members)
			ep.put(e,pa);
		}

	
	/**
	 * Check if two elements are equivalent
	 */
	public boolean isEquivalent(E a, E b)
		{
		return ep.get(a).members.contains(b);
		}
	
	/**
	 * Get equivalent elements for one element
	 */
	public Set<E> getPartition(E e)
		{
		return Collections.unmodifiableSet(ep.get(e).members);
		}
	
	/**
	 * Get all partitions
	 */
	/*
	public List<Set<E>> getPartitions()
		{
		LinkedList<Set<E>> list=new LinkedList<Set<E>>();
		for(Partition<E> p:partitions)
			list.add(Collections.unmodifiableSet(p.members));
		return list;
		}*/
	
	/**
	 * Get all partitions
	 */
	public List<Set<E>> getPartitions()
		{
		HashSet<Partition<E>> partitions=new HashSet<Partition<E>>();
		for(Partition<E> e:ep.values())
			partitions.add(e);
		LinkedList<Set<E>> list=new LinkedList<Set<E>>();
		for(Partition<E> p:partitions)
			list.add(Collections.unmodifiableSet(p.members));
		return list;
		}
	
	
	/**
	 * Remove all entries smaller than a certain volume
	 */
	public List<Set<E>> filterSize(List<Set<E>> list, int minSize)
		{
		LinkedList<Set<E>> out=new LinkedList<Set<E>>();
		for(Set<E> e:list)
			if(e.size()>=minSize)
				out.add(e);
		return out;
		}
	
	}
