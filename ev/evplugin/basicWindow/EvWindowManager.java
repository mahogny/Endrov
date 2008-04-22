package evplugin.basicWindow;

import java.awt.Rectangle;
import java.awt.event.WindowListener;
import javax.swing.JMenuBar;

/**
 * Manager for Endrov windows
 * @author Johan Henriksson
 */
public interface EvWindowManager
	{
	public void pack();
	public void addWindowListener(WindowListener l);
	public void dispose();
	public void setJMenuBar(JMenuBar mb);
	public void toFront();
	public void setVisible(boolean b);
	public void setBounds(Rectangle r);
	public void setTitle(String title);
	public Rectangle getBounds();
	}
