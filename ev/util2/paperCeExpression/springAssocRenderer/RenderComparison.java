/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperCeExpression.springAssocRenderer;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import util2.paperCeExpression.collectData.PaperCeExpressionUtil;
import util2.paperCeExpression.compare.CompareAll;
import util2.paperCeExpression.integrate.ExpUtil;


import endrov.flowColocalization.ColocCoefficients;
import endrov.springGraph.Graph;
import endrov.springGraph.GraphPanel;
import endrov.springGraph.SimpleGraphRenderer;
import endrov.springGraph.SpringGraphLayout;
import endrov.util.Tuple;

public class RenderComparison
	{

	
	final double restDistance=200;

	public static void main(String[] args)
		{
		
		Set<File> datas=PaperCeExpressionUtil.getAnnotated();

		final Map<Tuple<File,File>, ColocCoefficients> comparison;
		comparison=CompareAll.loadCache(datas, CompareAll.cachedValuesFileAP);
		
		
		
		
		
		JFrame frame=new JFrame();
		
		SpringGraphLayout<MyVertex> layout=new SpringGraphLayout<MyVertex>(){
		public double calcForce(MyVertex from, MyVertex to, double distance)
			{
			ColocCoefficients coef=comparison.get(Tuple.make(from.name,to.name));
			if(coef==null)
				return 0;
			else
				{
				Double cov=coef.getCovXY();
				if(cov==null || Double.isInfinite(cov) || Double.isNaN(cov))
					return 0;
				else
					{
//					System.out.println(cov);
					//With cov for spring coeff
					//double springConstant=5;
					//return (distance-restDistance)*springConstant*Math.abs(cov*cov*cov*500);
					
					//With cov as rest distance
					double springConstant=3;
					
					double newRestDist=500*(1-Math.abs(cov*cov*cov*cov));
					//System.out.println(newRestDist);
					
					return (distance-newRestDist)*springConstant;
					}
				}
			}
		};
		
		Graph<MyVertex> graph=new Graph<MyVertex>();
		for(File f:datas)
			graph.nodes.add(new MyVertex(f));
		for(MyVertex f:graph.nodes)
			for(MyVertex g:graph.nodes)
				if(ExpUtil.nameDateFromOSTName(f.name.getName()).fst().equals(
						ExpUtil.nameDateFromOSTName(g.name.getName()).fst()))
					graph.edges.add(Tuple.make(f, g));
		
		
		
		//layout.initGraph(graph, restDistance);
//		layout.initGraph(graph, 10);
		layout.initGraph(graph, 100);
		
		SimpleGraphRenderer<MyVertex> renderer=new SimpleGraphRenderer<MyVertex>(graph, layout){
		public void paintNode(Graphics g, MyVertex v, int x, int y)
			{
			int radius=21;
			g.setColor(Color.yellow);
			g.fillOval(x-radius/2,y-radius/2, radius, radius);
			g.setColor(Color.black);
			g.drawOval(x-radius/2,y-radius/2, radius, radius);
			g.drawString(v.name.getName(), x-radius/2+6, y-radius/2+15);
			}
		};
		
		GraphPanel<MyVertex> p=new GraphPanel<MyVertex>(renderer,layout);

		//p.graph=graph;
		
		
		
//		for(int i=0;i<100;i++)
	//		layout.iterate();

		layout.bouncy=false;
		
		frame.add(p);
		frame.setSize(400,300);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		p.start();
		
		}
	}
