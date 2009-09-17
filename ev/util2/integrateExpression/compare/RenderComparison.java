package util2.integrateExpression.compare;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import util2.integrateExpression.FindAnnotatedStrains;


import endrov.flowColocalization.ColocCoefficients;
import endrov.springGraph.Graph;
import endrov.springGraph.GraphPanel;
import endrov.springGraph.SimpleGraphRenderer;
import endrov.springGraph.SpringGraphLayout;
import endrov.util.Tuple;

public class RenderComparison
	{

	
	

	public static void main(String[] args)
		{
		
		Set<File> datas=FindAnnotatedStrains.getAnnotated();

		final Map<Tuple<File,File>, ColocCoefficients> comparisonT;
		//Map<Tuple<File,File>, ColocCoefficients> comparisonAP=new HashMap<Tuple<File,File>, ColocCoefficients>();
		//Map<Tuple<File,File>, ColocCoefficients> comparisonXYZ=new HashMap<Tuple<File,File>, ColocCoefficients>();
		comparisonT=CompareAll.loadCache(datas, CompareAll.cachedValuesFileT);
//		comparisonAP=CompareAll.loadCache(datas, CompareAll.cachedValuesFileAP);
	//	comparisonXYZ=CompareAll.loadCache(datas, CompareAll.cachedValuesFileXYZ);

		
		final double restDistance=200;
		
		
		
		JFrame frame=new JFrame();
		
		SpringGraphLayout<MyVertex> layout=new SpringGraphLayout<MyVertex>(){
		public double calcForce(MyVertex from, MyVertex to, double distance)
			{
			double springConstant=5;
			ColocCoefficients coef=comparisonT.get(Tuple.make(from.name,to.name));
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
					return (distance-restDistance)*springConstant*Math.abs(cov*cov*cov*500);
					}
				}
			}
		};
		
		Graph<MyVertex> graph=new Graph<MyVertex>();
		for(File f:datas)
			{
			graph.nodes.add(new MyVertex(f));
			}
		
		//layout.initGraph(graph, restDistance);
		layout.initGraph(graph, 10);
		
		SimpleGraphRenderer<MyVertex> renderer=new SimpleGraphRenderer<MyVertex>(graph, layout){
		public void paintNode(Graphics g, MyVertex v, int x, int y)
			{
			int radius=21;
			g.setColor(Color.yellow);
			g.fillOval(x,y, radius, radius);
			g.setColor(Color.black);
			g.drawOval(x,y, radius, radius);
			g.drawString(v.name.getName(), x+6, y+15);
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
