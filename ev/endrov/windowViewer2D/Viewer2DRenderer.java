/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer2D;

import java.awt.*;

/**
 * Draw overlay graphics
 * @author Johan Henriksson
 *
 */
public interface Viewer2DRenderer
	{
	public void draw(Graphics g);
	public void dataChangedEvent();
	}
