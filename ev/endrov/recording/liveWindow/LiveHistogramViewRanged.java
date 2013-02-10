/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.liveWindow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;


/**
 * Histogram, with a range setting displayed on top
 * 
 * @author Johan Henriksson
 *
 */
public class LiveHistogramViewRanged extends LiveHistogramView implements MouseListener, MouseMotionListener
	{
	private static final long serialVersionUID = 1L;

	public int lower=0;
	public int upper=255;
	
	private Color rangeBarColor=Color.RED;
	
	
	public LiveHistogramViewRanged()
		{
		addMouseListener(this);
		addMouseMotionListener(this);
		}
	
	
	/**
	 * Vertical stitched line
	 */
	private void vertStitch(Graphics g, int x)
		{
		int h=getHeight();
		for(int y=0;y<h;y+=4)
			g.drawLine(x, y, x, y);
		}

	
	@Override
	protected void paintComponent(Graphics g)
		{
		super.paintComponent(g);
		int xLower=toScreenX(lower);
		int xUpper=toScreenX(upper);
		g.setColor(rangeBarColor);
		vertStitch(g, xLower);
		vertStitch(g, xUpper);		
		}
	
	
	/**
	 * Calculate range automatically from the image that has been set
	 */
	public void calcAutoRange()
		{
		lower=histoRangeMin;
		upper=histoRangeMax;
//		calcAutoRange(p.convertToInt(true).getArrayInt());
		}
	


	public void mouseClicked(MouseEvent e)
		{
		}

	public void mouseEntered(MouseEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		}

	public void mousePressed(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			moveLimit(e);
		else if(SwingUtilities.isRightMouseButton(e))
			{
			JPopupMenu menu=new JPopupMenu();
			JCheckBoxMenuItem miUseCDF=new JCheckBoxMenuItem("Show CDF", showCDF);
			miUseCDF.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent arg0)
					{
					setShowCDF(!showCDF);
					}
				});
			menu.add(miUseCDF);
			menu.show(this, e.getX(), e.getY());
			}
		}

	public void mouseReleased(MouseEvent e)
		{
		}

	public void mouseDragged(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			moveLimit(e);
		}

	public void mouseMoved(MouseEvent arg0)
		{
		}
	
	/**
	 * Move one limit using the mouse 
	 */
	private void moveLimit(MouseEvent e)
		{
		int mx=toWorldX(e.getX());
//		int xLower=toScreenX(lower);
	//	int xUpper=toScreenX(upper);
		
		if(Math.abs(mx-lower) < Math.abs(mx-upper))
			lower=mx;
		else
			upper=mx;
		
		if(lower<showRangeMin)
			lower=showRangeMin;
		if(upper>showRangeMax)
			upper=showRangeMax;
		
		repaint();
		
		for(ActionListener l:actionListeners)
			l.actionPerformed(new ActionEvent(this, 0, "rangeChanged"));
		}
	
	private List<ActionListener> actionListeners=new LinkedList<ActionListener>();
	
	public void addActionListener(ActionListener l)
		{
		actionListeners.add(l);
		}
	
	
	
	}
