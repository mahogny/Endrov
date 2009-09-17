package endrov.springGraph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.vecmath.Vector2d;


/**
 * Simple graph display
 * 
 * @author Johan Henriksson
 *
 */
public class GraphPanel<E> extends JPanel implements ActionListener, MouseMotionListener, MouseListener
	{
	private static final long serialVersionUID = 1L;

	//private Graph<MyNode> graph;
	private GraphLayout<E> layout;
	private GraphRenderer<E> renderer;

	private Timer timer=new Timer(5, this);
	
	private Vector2d cam=new Vector2d(-100,-50);
	
	
	public GraphPanel(GraphRenderer<E> renderer, GraphLayout<E> layout)
		{
		this.renderer=renderer;
		this.layout=layout;
		addMouseListener(this);
		addMouseMotionListener(this);
		}
	
	public void start()
		{
		timer.setRepeats(false);
		timer.start();
		}
	
	protected void paintComponent(Graphics g)
		{
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		
		renderer.paintComponent(g, cam);
		
		timer.restart();

//		for(MyNode n:graph.nodes)
//			renderer.paintComponent(g, n, cam);
			
//		System.out.println("repaint "+System.currentTimeMillis());
		}
	
	
	
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==timer)
			{
			layout.updatePositions();
			repaint();
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
		}
	
	
	
	
	
	
	

	}
