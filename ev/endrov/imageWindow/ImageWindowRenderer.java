package endrov.imageWindow;

import java.awt.*;

/**
 * Draw overlay graphics
 * @author Johan Henriksson
 *
 */
public interface ImageWindowRenderer
	{
	public void draw(Graphics g);
	public void dataChangedEvent();
	}
