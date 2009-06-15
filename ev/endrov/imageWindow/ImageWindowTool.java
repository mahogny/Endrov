package endrov.imageWindow;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;

/**
 * A selectable tool in the image window
 */
public interface ImageWindowTool
	{
	/**
	 * Get the menu item to be placed in the tools menu
	 */
	public JMenuItem getMenuItem();

	/**
	 * Called when the tool has been deselected
	 */
	public void deselected();
	
	public void mouseClicked(MouseEvent e);
	public void mousePressed(MouseEvent e);
	public void mouseReleased(MouseEvent e);
	public void mouseDragged(MouseEvent e, int dx, int dy);
	public void paintComponent(Graphics g);
	public void mouseMoved(MouseEvent e, int dx, int dy);
	public void keyPressed(KeyEvent e);
	public void keyReleased(KeyEvent e);
	public void mouseExited(MouseEvent e);
	}
