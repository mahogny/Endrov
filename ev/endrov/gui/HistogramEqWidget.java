/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import endrov.flowBasic.EvImageHistogram;
import endrov.unsortedImageFilters.unfinished.HistogramTransform;

public class HistogramEqWidget extends JPanel
	{
	private static final long serialVersionUID = 1L;


	/**
	 * From color, to color.
	 * Map is never inverted
	 */

	HistogramTransform eq=new HistogramTransform();
	
	
	SortedMap<?,Integer> cumHist=new TreeMap<Integer, Integer>();

	public HistogramEqWidget()
		{
		//Maybe separate mapping from the widget? can do later
		
		Map<Integer, Integer> hist=new HashMap<Integer, Integer>();
		hist.put(new Integer(5), new Integer(30));
		hist.put(new Integer(15), new Integer(40));
		hist.put(new Integer(25), new Integer(20));
		
		cumHist=EvImageHistogram.makeHistCumulative(hist);
		}

	
	
	
	/*
	private double map(double d)
		{
		EvArrayUtil.
		points.h
		}*/
	
	private boolean renderHistogram=true;
	
	double zoomX=1;
	double camX=0;
	double zoomY=1;
	
	private int toScreenX(double x)
		{
		System.out.println("x "+(int)((x-camX)*zoomX));
		return (int)((x-camX)*zoomX);
		}
	
	private int toScreenY(double y)
		{
		int out=getHeight()-(int)(y*zoomY);
		System.out.println(out);
		return out;
		}

	@SuppressWarnings("unchecked")
	private void findScale()
		{

		//Equ point limits

		Double start=eq.points.firstKey();
		Double end=eq.points.lastKey();
		Double maxY=eq.points.get(eq.points.lastKey());

		//Histogram limits
		if(EvImageHistogram.isIntegerHist(cumHist))
			{
			SortedMap<Integer, Integer> cum=(SortedMap<Integer, Integer>) cumHist;
			Integer start2=cum.firstKey();
			Integer end2=cum.lastKey();
			if(start2<start)
				start=(double)start2;
			if(end2>end)
				end=(double)end2;
			if(cum.get(end2)>maxY)
				maxY=(double)cum.get(end2);
			}
		end+=5;
		start-=5;
		camX=start;
		zoomX=getWidth()/(end-start);
		zoomY=getHeight()/maxY;
		}
	
	@SuppressWarnings("unchecked")
	protected void paintComponent(Graphics g)
		{
		findScale();
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		//Need to find out range
		
		
		
		g.setColor(Color.LIGHT_GRAY);
		//Render histogram
		if(renderHistogram)
			{
			if(EvImageHistogram.isIntegerHist(cumHist))
				{
				SortedMap<Integer, Integer> cum=(SortedMap<Integer, Integer>) cumHist;
				
				
				Integer lastX=toScreenX(cum.firstKey());
				Integer lastY=0;//cum.get(cum.firstKey());
				for(Map.Entry<Integer, Integer> it:cum.entrySet())
					{
					int nextX=toScreenX(it.getKey());
					int nextY=toScreenY(it.getValue());
					g.fillRect(lastX, lastY, nextX-lastX, lastY);
					lastX=nextX;
					lastY=nextY;
					}
				//Render to the end: it continues to infinity
				g.fillRect(lastX, lastY, getWidth()-lastX, lastY);
				
				}
			
			
			
			}
		
		//Render mapping
		g.setColor(Color.BLACK);
		Iterator<Map.Entry<Double,Double>> itp=eq.points.entrySet().iterator();
		Map.Entry<Double,Double> lastPoint=itp.next();
		if(itp.hasNext())
			{
			Map.Entry<Double,Double> nextPoint=itp.next();
			int x1=toScreenX(lastPoint.getKey());
			int y1=toScreenY(lastPoint.getValue());
			int x2=toScreenX(nextPoint.getKey());
			int y2=toScreenY(nextPoint.getValue());
			g.drawLine(x1, y1, x2, y2);
			}
		for(Map.Entry<Double,Double> e:eq.points.entrySet())
			{
			int x=toScreenX(e.getKey());
			int y=toScreenY(e.getValue());
			int r=2;
			g.drawOval(x-r, y-r, 2*r, 2*r);
			}
		g.setColor(Color.BLACK);
		for(Map.Entry<Double,Double> e:eq.points.entrySet())
			{
			Double mx=e.getKey();
			Double my=e.getValue();
			int x=toScreenX(mx);
			int y=toScreenY(my);
			int r=2;
			g.drawOval(x-r, y-r, 2*r, 2*r);
			}
		
		}




	public static void main(String[] args)
		{
		JFrame f=new JFrame();
		f.setSize(100, 30);
		
		HistogramEqWidget h=new HistogramEqWidget();
		f.add(h);
		
		f.setVisible(true);
		
		
		
		
		
		// TODO Auto-generated method stub

		}

	}
