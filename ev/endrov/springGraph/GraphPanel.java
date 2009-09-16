package endrov.springGraph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.vecmath.Vector2d;

import endrov.springGraph.SpringGraphLayout.SpringNode;


/**
 * 
 * @author Johan Henriksson
 *
 */
public class GraphPanel extends JPanel implements ActionListener, MouseMotionListener, MouseListener
	{
	private static final long serialVersionUID = 1L;

	private Graph<MyNode> graph;
	private SpringGraphLayout<MyNode> layout;
	private NodeRenderer<MyNode> renderer;

	private Timer timer=new Timer(5, this);
	
	private Vector2d cam=new Vector2d(-100,-50);
	
	
	public GraphPanel()
		{
		addMouseListener(this);
		addMouseMotionListener(this);
		}
	
	public void start()
		{
		timer.start();
		}
	
	protected void paintComponent(Graphics g)
		{
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		
		renderer.paintComponent(g, cam);
		
//		for(MyNode n:graph.nodes)
//			renderer.paintComponent(g, n, cam);
			
		}
	
	
	
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==timer)
			{
			layout.iterate();
			repaint();
			timer.restart();
			}
		}
	
	
	
	
	public int mouseLastX, mouseLastY;
	
	public void mouseDragged(MouseEvent e)
		{
		//Pan
		if(SwingUtilities.isRightMouseButton(e))
			{
			cam.x-=e.getX()-mouseLastX;
			cam.y-=e.getY()-mouseLastY;
			mouseLastX=e.getX();
			mouseLastY=e.getY();
			repaint();
			}
		
		}

	public void mouseMoved(MouseEvent e)
		{
		}
	
	
	

	public void mouseClicked(MouseEvent e)
		{
		}

	public void mouseEntered(MouseEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		}

	public void mousePressed(MouseEvent e)
		{
		System.out.println("here");
		mouseLastX=e.getX();
		mouseLastY=e.getY();
		}

	public void mouseReleased(MouseEvent e)
		{
		// TODO Auto-generated method stub
		
		}
	
	
	
	
	
	
	

	public static void main(String[] args)
		{
		JFrame f=new JFrame();
		GraphPanel p=new GraphPanel();
		
		SpringGraphLayout<MyNode> layout=new SpringGraphLayout<MyNode>(){
		public double calcForce(MyNode from, MyNode to, double distance)
			{
			double restDistance=100;
			double springConstant=5;
			return -(distance-restDistance)*springConstant;
			}
		};
		
		Graph<MyNode> graph=new Graph<MyNode>();
		graph.nodes.add(new MyNode());
		graph.nodes.add(new MyNode());
		graph.nodes.add(new MyNode());
		graph.nodes.add(new MyNode());
		graph.nodes.add(new MyNode());
		graph.nodes.add(new MyNode());
		graph.nodes.add(new MyNode());
		graph.nodes.add(new MyNode());
		graph.nodes.add(new MyNode());

		layout.initGraph(graph);
		
		
		
		
		p.layout=layout;
		p.renderer=layout;
		p.graph=graph;
		
		
		
//		for(int i=0;i<100;i++)
	//		layout.iterate();
		
		for(SpringNode sn:layout.nodes.values())
			System.out.println(sn.pos);
		
		f.add(p);
		f.setSize(400,300);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		p.start();
		
		}
	}
