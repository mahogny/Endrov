package endrov.springGraph;

import java.awt.Graphics;

import javax.vecmath.Vector2d;

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
	
	//public void paintComponent(Graphics g, E e, Vector2d cam);

	public void paintComponent(Graphics g, Vector2d cam);

	//and size
	}