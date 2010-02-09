/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.springGraph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

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
public class GraphPanel<E> extends JPanel implements ActionListener, MouseMotionListener, MouseListener, MouseWheelListener
	{
	private static final long serialVersionUID = 1L;

	//private Graph<MyNode> graph;
	private GraphLayout<E> layout;
	private GraphRenderer<E> renderer;

	private Timer timer=new Timer(5, this);
	
	private Vector2d cam=new Vector2d(-100,-50);
	private double zoom=1;
	
	public GraphPanel(GraphRenderer<E> renderer, GraphLayout<E> layout)
		{
		this.renderer=renderer;
		this.layout=layout;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
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
		
		
		renderer.paintComponent(g, cam, zoom);
		
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
			cam.x-=(e.getX()-mouseLastX)/zoom;
			cam.y-=(e.getY()-mouseLastY)/zoom;
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

	public void mouseWheelMoved(MouseWheelEvent e)
		{
		zoom*=Math.exp(0.3*e.getWheelRotation());
		repaint();
		}
	
	
	
	
	
	
	

	}
