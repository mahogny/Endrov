package endrov.imageWindow;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;

//either send down variables or add accessors to imagewindow


public interface ImageWindowTool
	{
	public JMenuItem getMenuItem();
	
	/*
	public boolean isToggleable();
	public String toolCaption();
	public boolean enabled();*/
	public void unselected();
	
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
