package endrov.springGraph;

import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Vector2d;


/**
 * Graph, positions given by springs
 * @author Johan Henriksson
 */
public abstract class SpringGraphLayout<V> implements GraphLayout<V>
	{
	/**
	 * State of node 
	 * @author Johan Henriksson
	 *
	 */
	public static class SpringNode
		{
		public Vector2d pos=new Vector2d();
		public Vector2d v=new Vector2d();
		private Vector2d f=new Vector2d();
		}
	
	
	public Map<V,SpringNode> nodes=new HashMap<V, SpringNode>(); 
	
	/**
	 * Enable to use F=ma for movement
	 */
	public boolean bouncy=true;
	
	/**
	 * Damping, only with bouncy movement 
	 */
	public double dampingCoefficient=1;
	
	
	/* (non-Javadoc)
	 * @see endrov.springGraph.GraphLayout#updatePositions()
	 */
	public void updatePositions()
		{
		//Calculate forces
		for(Map.Entry<V,SpringNode> n:nodes.entrySet())
			{
			n.getValue().f.x=0;
			n.getValue().f.y=0;
			}
		for(Map.Entry<V,SpringNode> ae:nodes.entrySet())
			{
			SpringNode a=ae.getValue();
			for(Map.Entry<V,SpringNode> be:nodes.entrySet())
				if(ae!=be)
					{
					SpringNode b=be.getValue();
					double dx=a.pos.x-b.pos.x;
					double dy=a.pos.y-b.pos.y;
					double len=Math.sqrt(dx*dx+dy*dy);
					double thisf=-calcForce(ae.getKey(), be.getKey(), len);
					a.f.x+=dx*thisf/len;
					a.f.y+=dy*thisf/len;
					}
			}
		
		
		
		//Move nodes
		if(bouncy)
			{
			//Accelerate with dampening
			double dt=0.05/nodes.size();
			for(Map.Entry<V,SpringNode> n:nodes.entrySet())
				{
				SpringNode sn=n.getValue();
				//a=f/m
				double mass=1;
				sn.v.x+=dt*sn.f.x/mass;
				sn.v.y+=dt*sn.f.y/mass;
				sn.pos.x+=dt*sn.v.x;
				sn.pos.y+=dt*sn.v.y;
				
				//Dampen
				sn.v.x*=Math.exp(-dt*dampingCoefficient);
				sn.v.y*=Math.exp(-dt*dampingCoefficient);
				}
			}
		else
			{
			//No acceleration, move in direction of force
			double dt=0.01/nodes.size();
			for(Map.Entry<V,SpringNode> n:nodes.entrySet())
				{
				SpringNode sn=n.getValue();
				sn.pos.x+=dt*sn.f.x;
				sn.pos.y+=dt*sn.f.y;
				}
			}
		
		}
	
	
	
	/* (non-Javadoc)
	 * @see endrov.springGraph.GraphLayout#getPosition(V)
	 */
	public Vector2d getPosition(V v)
		{
		return nodes.get(v).pos;
		}
	
	
	
	
	/**
	 * Init graph with random positions. Scale is any non-zero number and decides initial scatter size
	 */
	public void initGraph(Graph<V> graph, double scale)
		{
		for(V v:graph.nodes)
			{
			if(!nodes.containsKey(v))
				{
				SpringNode sn=new SpringNode();
				sn.pos.x=Math.random()*scale;
				sn.pos.y=Math.random()*scale;
				nodes.put(v, sn);
				}
			}
		
		}

	
	public abstract double calcForce(V from, V to, double distance);

	}
