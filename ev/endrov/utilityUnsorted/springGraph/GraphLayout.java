/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.springGraph;

import javax.vecmath.Vector2d;

/**
 * Handle positions of vertices in a graph when drawn on screen
 * 
 * @author Johan Henriksson
 *
 * @param <V>
 */
public interface GraphLayout<V>
	{

	/**
	 * Do one step of movement
	 */
	public abstract void updatePositions();

	/**
	 * Return read-only vector
	 */
	public abstract Vector2d getPosition(V v);

	}
