package evplugin.modelWindow;

import java.awt.event.MouseEvent;

/**
 * Listener for model window mouse events
 * @author Johan Henriksson
 */
public interface ModelWindowMouseListener
	{
	public boolean mouseDragged(MouseEvent e, int dx, int dy);
	public void mouseMoved(MouseEvent e);
	public void mouseClicked(MouseEvent e);
	public void mouseEntered(MouseEvent e);
	public void mouseExited(MouseEvent e);
	public void mousePressed(MouseEvent e);
	public void mouseReleased(MouseEvent e);
	}
