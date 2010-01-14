/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.basicWindow;

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
	public void setLocation(int x, int y);
	public void setTitle(String title);
	public void setResizable(boolean b);
	public Rectangle getBounds();
	}
