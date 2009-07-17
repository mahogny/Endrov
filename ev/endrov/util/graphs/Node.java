package endrov.util.graphs;

/**
 * @author http://algowiki.net/wiki/index.php/Node
 * 
 * 
 * 
 *
 */
public class Node implements Comparable<Node> 
	{

	public int name;
	public boolean visited = false;   // used for Kosaraju's algorithm and Edmonds's algorithm
	public int lowlink = -1;          // used for Tarjan's algorithm
	public int index = -1;            // used for Tarjan's algorithm
	public Node(int n)
		{
		name = n;
		}

	public int compareTo(Node n)
		{
		if(n == this)
			return 0;
		return -1;
		}
	}
