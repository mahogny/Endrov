package util.graphs;



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