/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.springGraph;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;

public class Test
	{

	public static void main(String[] args)
		{
		JFrame f=new JFrame();
		
		SpringGraphLayout<MyVertex> layout=new SpringGraphLayout<MyVertex>(){
		public double calcForce(MyVertex from, MyVertex to, double distance)
			{
			double restDistance=100;
			double springConstant=5;
			return (distance-restDistance)*springConstant;
			}
		};
		
		Graph<MyVertex> graph=new Graph<MyVertex>();
		graph.nodes.add(new MyVertex("1"));
		graph.nodes.add(new MyVertex("2"));
		graph.nodes.add(new MyVertex("3"));
		graph.nodes.add(new MyVertex("4"));
		graph.nodes.add(new MyVertex("5"));
		graph.nodes.add(new MyVertex("6"));
		graph.nodes.add(new MyVertex("7"));
		graph.nodes.add(new MyVertex("8"));
		graph.nodes.add(new MyVertex("9"));

		layout.initGraph(graph,10);
		
		SimpleGraphRenderer<MyVertex> renderer=new SimpleGraphRenderer<MyVertex>(graph, layout){
		public void paintNode(Graphics g, MyVertex v, int x, int y)
			{
			int radius=21;
			g.setColor(Color.yellow);
			g.fillOval(x,y, radius, radius);
			g.setColor(Color.black);
			g.drawOval(x,y, radius, radius);
			g.drawString(v.name, x+6, y+15);
			}
		};
		
		GraphPanel<MyVertex> p=new GraphPanel<MyVertex>(renderer,layout);

		//p.graph=graph;
		
		
		
//		for(int i=0;i<100;i++)
	//		layout.iterate();

		layout.bouncy=true;
		
		f.add(p);
		f.setSize(400,300);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		p.start();
		
		}
	}
