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
public interface GraphRenderer<E>
	{
	public void paintComponent(Graphics g, Vector2d cam, double zoom);
	}