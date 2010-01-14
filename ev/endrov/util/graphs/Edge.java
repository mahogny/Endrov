/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.graphs;



/**
 * 
 * @author http://algowiki.net/wiki/index.php/Edge
 *
 */
public class Edge implements Comparable<Edge> 
	{

	public Node from, to;
	public int weight;

	public Edge(Node f, Node t, int w)
		{
		from = f;
		to = t;
		weight = w;
		}

	public int compareTo(Edge e)
		{
		return weight - e.weight;
		}
	}
