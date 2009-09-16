package endrov.springGraph;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Vector2d;


/**
 * Graph, positions given by springs
 * @author Johan Henriksson
 */
public abstract class SpringGraphLayout<V> implements NodeRenderer<V>
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
	
	public boolean bouncy=true;
	
	/**
	 * Damping, only with bouncy movement 
	 */
	public double dampingCoefficient=1;
	
	public void iterate()
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
					double thisf=calcForce(ae.getKey(), be.getKey(), len);
					a.f.x+=dx*thisf/len;
					a.f.y+=dy*thisf/len;
					}
			}
		
		
		
		//Move nodes
		if(bouncy)
			{
			//Accelerate with dampening
			double dt=0.005;
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
			double dt=0.01;
			for(Map.Entry<V,SpringNode> n:nodes.entrySet())
				{
				SpringNode sn=n.getValue();
				sn.pos.x+=dt*sn.f.x;
				sn.pos.y+=dt*sn.f.y;
				}
			}
		
		/*
		
*/
		}
	
	
	public double getX(V e)
		{
		return nodes.get(e).pos.x;
		}

	public double getY(V e)
		{
		return nodes.get(e).pos.y;
		}
	
	
	
	
	public void paintComponent(Graphics g, Vector2d cam)
		{
		for(V e:nodes.keySet())
			{
		
		SpringNode sn=nodes.get(e);
		g.setColor(Color.blue);
		g.fillOval((int)(sn.pos.x-cam.x), (int)(sn.pos.y-cam.y), 21, 21);
			}
		}

	
	
	
	public void initGraph(Graph<V> graph)
		{
		for(V v:graph.nodes)
			{
			if(!nodes.containsKey(v))
				{
				SpringNode sn=new SpringNode();
				sn.pos.x=Math.random()*10;
				sn.pos.y=Math.random()*10;
				nodes.put(v, sn);
				}
			}
		
		}

	
	public abstract double calcForce(V from, V to, double distance);

	}
