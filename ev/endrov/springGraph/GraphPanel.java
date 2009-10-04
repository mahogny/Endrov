package endrov.springGraph;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * 
 * @author Johan Henriksson
 *
 */
public class GraphPanel extends JPanel
	{
	private static final long serialVersionUID = 1L;

	private Graph<MyNode> graph;
	private SpringGraphLayout<MyNode> layout;
	private NodeRenderer<MyNode> renderer;
	
	protected void paintComponent(Graphics g)
		{
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		
		
		for(MyNode n:graph.nodes)
			renderer.paintComponent(g, n);
			
		}
	
	/**
	 * do GraphRenderer instead. 
	 * separate from JPanel.
	 * postscript output somehow.
	 * 
	 * graphrenderer, some system to allow feedback
	 * 
	 * 
	 * @author Johan Henriksson
	 */
	public interface NodeRenderer<E>
		{
		public double getX(E e);
		public double getY(E e);
		
		public void paintComponent(Graphics g, E e);
		
		//and size
		}
	
	
	/**
	 * 
	 * @author Johan Henriksson
	 */
	public static class MyNode
		{
		String name;
		}
		
	
	
	
	
	public static void main(String[] args)
		{
		JFrame f=new JFrame();
		GraphPanel p=new GraphPanel();
		
		SpringGraphLayout<MyNode> layout=new SpringGraphLayout<MyNode>(){
		public double calcForce(MyNode from, MyNode to, double distance)
			{
			return (distance-50)*5;
			}
		};
		
		Graph<MyNode> graph=new Graph<MyNode>();
		graph.nodes.add(new MyNode());
		graph.nodes.add(new MyNode());
		graph.nodes.add(new MyNode());
		graph.nodes.add(new MyNode());

		
		
		
		
		
		p.layout=layout;
		p.renderer=layout;
		
		
		f.add(p);
		f.setSize(400,300);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		}
	}
