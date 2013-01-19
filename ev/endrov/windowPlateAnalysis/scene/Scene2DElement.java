package endrov.windowPlateAnalysis.scene;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Elements of a 2D scene
 * 
 * @author Johan Henriksson
 *
 */
public interface Scene2DElement
	{
	public void paintComponent(Graphics g, Scene2DView p);
	
	public Rectangle getBoundingBox();
	
	}