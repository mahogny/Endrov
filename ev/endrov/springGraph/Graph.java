package endrov.springGraph;

import java.util.HashSet;
import java.util.Set;

import endrov.util.Tuple;

/**
 * 
 * @author Johan Henriksson
 *
 * @param <V>
 */
public class Graph<V>
	{
	public Set<V> nodes=new HashSet<V>();
	public Set<Tuple<V,V>> edges=new HashSet<Tuple<V,V>>();

	//Or map, V,V -> E?
	
	//One of many possible graph structures. how to generalize?
	
	}
