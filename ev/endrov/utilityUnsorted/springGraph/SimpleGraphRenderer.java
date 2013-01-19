/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.springGraph;

import java.awt.Color;
import java.awt.Graphics;

import javax.vecmath.Vector2d;

import endrov.util.collection.Tuple;

/**
 * 
 * 
 * @author Johan Henriksson
 *
 * @param <V>
 */
public abstract class SimpleGraphRenderer<V> implements GraphRenderer<V>
	{
	private final Graph<V> graph;
	private final GraphLayout<V> layout;

	public SimpleGraphRenderer(Graph<V> graph, GraphLayout<V> layout)
		{
		this.graph=graph;
		this.layout=layout;
		}

	
	public Vector2d toWorld(Vector2d pos, Vector2d cam, double zoom)
		{
		return new Vector2d(zoom*(pos.x-cam.x), zoom*(pos.y-cam.y));
		}
	
	public void paintComponent(Graphics g, Vector2d cam, double zoom)
		{
		g.setColor(Color.green);
		for(Tuple<V,V> e:graph.edges)
			{
			Vector2d posFrom=toWorld(layout.getPosition(e.fst()), cam, zoom);
			Vector2d posTo=toWorld(layout.getPosition(e.snd()), cam, zoom);
			g.drawLine((int)posFrom.x, (int)posFrom.y, (int)posTo.x, (int)posTo.y);
			}
		
		
		for(V v:graph.nodes)
			{
			Vector2d pos=toWorld(layout.getPosition(v), cam, zoom);
//			SpringNode sn=layoutnodes.get(e);
			paintNode(g,v, (int)pos.x, (int)pos.y);
			}
		}

	public abstract void paintNode(Graphics g, V v, int x, int y);
	
	}
