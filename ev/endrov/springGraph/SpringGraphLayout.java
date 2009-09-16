package endrov.springGraph;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Vector2d;

import endrov.springGraph.GraphPanel.NodeRenderer;

/**
 * Graph, positions given by springs
 * @author Johan Henriksson
 */
public abstract class SpringGraphLayout<E> implements NodeRenderer<E>
	{
	
	
	
	public abstract static class SpringNode
		{
		public Vector2d pos=new Vector2d();
		public Vector2d v=new Vector2d();
		public Vector2d a=new Vector2d();
	//	public E ref;
		
		private Vector2d f=new Vector2d();
		}
	
	
	Map<E,SpringNode> nodes=new HashMap<E, SpringNode>(); 
	
	
	public void iterate()
		{
		for(Map.Entry<E,SpringNode> n:nodes.entrySet())
			{
			n.getValue().f.x=0;
			n.getValue().f.y=0;
			}
		for(Map.Entry<E,SpringNode> ae:nodes.entrySet())
			{
			for(Map.Entry<E,SpringNode> be:nodes.entrySet())
				{
				SpringNode a=ae.getValue();
				SpringNode b=be.getValue();
				double dx=a.pos.x-b.pos.x;
				double dy=a.pos.y-b.pos.y;

				double len=Math.sqrt(dx*dx+dy*dy);

				double thisf=calcForce(ae.getKey(), be.getKey(), len);
				a.f.x+=dx*thisf/len;
				a.f.y+=dy*thisf/len;
				}
			}
		}
	
	public abstract double calcForce(E from, E to, double distance);
	
	public double getX(E e)
		{
		return nodes.get(e).pos.x;
		}

	public double getY(E e)
		{
		return nodes.get(e).pos.y;
		}
	
	public void paintComponent(Graphics g, E e)
		{
		g.setColor(Color.blue);
		SpringNode sn=nodes.get(e);
		g.fillOval((int)sn.pos.x, (int)sn.pos.y, 10, 10);
		}

	

	
	}
