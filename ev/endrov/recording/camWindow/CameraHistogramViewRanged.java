/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.camWindow;

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

import endrov.imageset.EvPixels;


/**
 * Histogram extended with range 
 * @author Johan Henriksson
 *
 */
public class CameraHistogramViewRanged extends CameraHistogramView implements MouseListener, MouseMotionListener
	{
	private static final long serialVersionUID = 1L;

	public int lower=0;
	public int upper=255;
	
	private Color rangeBarColor=Color.RED;
	
	
	public CameraHistogramViewRanged()
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
//		System.out.println("limit "+lower+"    "+upper);
		}
	
	
	/**
	 * Calculate range automatically from image
	 */
	public void calcAutoRange(EvPixels[] p)
		{
		if(p.length==1)
			calcAutoRange(p[0].convertToInt(true).getArrayInt());
		else
			{
			calcAutoRange(p[0].convertToInt(true).getArrayInt());
			extendAutoRange(p[1].convertToInt(true).getArrayInt());
			extendAutoRange(p[2].convertToInt(true).getArrayInt());
			}
		}
	
	/**
	 * Calculate range automatically from image
	 */
	private void calcAutoRange(int[] p)
		{
		int min=p[0], max=p[0];
		for(int i=1;i<p.length;i++)
			{
			int v=p[i];
			if(v<min)
				min=v;
			else if(v>max)
				max=v;
			}
		lower=min;
		upper=max;
		}
	

	private void extendAutoRange(int[] p)
		{
		int min=p[0], max=p[0];
		for(int i=1;i<p.length;i++)
			{
			int v=p[i];
			if(v<min)
				min=v;
			else if(v>max)
				max=v;
			}
		lower=Math.min(min,lower);
		upper=Math.max(max,upper);
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
	public void moveLimit(MouseEvent e)
		{
		int mx=toWorldX(e.getX());
//		int xLower=toScreenX(lower);
	//	int xUpper=toScreenX(upper);
		
		if(Math.abs(mx-lower) < Math.abs(mx-upper))
			lower=mx;
		else
			upper=mx;
		
		if(lower<0)
			lower=0;
		if(upper>rangeMax)
			upper=rangeMax;
		
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
