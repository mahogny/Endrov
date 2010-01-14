/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
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
