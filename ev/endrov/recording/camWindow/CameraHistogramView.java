/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.camWindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;


/**
 * Calculate histogram for display only. Hence the histogram is binned to fit on screen. 
 * @author Johan Henriksson
 *
 */
public class CameraHistogramView extends JPanel
	{
	private static final long serialVersionUID = 1L;

	//private int rangeMin;
	protected int rangeMax=255;
	
	private EvPixels[] currentImage;
	private BufferedImage cachedImage=null; //cached image

	private int height=50;
	
	/**
	 * Set pixels to calculate histogram from. #bits determines maximum range
	 */
	public void setImage(EvPixels[] p, int numBits)
		{
		cachedImage=null;
		
		currentImage=p;
		
		//rangeMin=0;
		rangeMax=2<<numBits-1;
		repaint();
		}

	@Override
	public Dimension getMinimumSize()
		{
		return new Dimension(1, height);
		}
	
	@Override
	public Dimension getPreferredSize()
		{
		return new Dimension(1, height);
		}
	
	/**
	 * Calculate the histogram
	 * @param p Pixels intensities
	 * @return Bins
	 */
	private int[] calculateHistogram(int[] p)
		{
		int screenWidth=getWidth();
		int[] bins=new int[screenWidth];
		
		//Figure out max range
		int max=0;
		for(int v:p)
			if(v>max)
				max=v;
		
		if(max<256)
			max=255;
		else if(max<1024)
			max=1023;
		else if(max<4096)
			max=4095;
		else if(max<65535)
			max=65535;
		else
			max=(1<<32)-1;
		rangeMax=max;
		
		//Calculate histogram
		for(int v:p)
			{
			if(v<0)
				{
				v=0;
//				System.out.print(v+" ");
				}
			
			int i=v*screenWidth/rangeMax;
			bins[i]++;
			}

		int totalNum=p.length;
		for(int i=0;i<bins.length;i++)
			bins[i]=bins[i]*height/totalNum;

		return bins;
		}
	
	/**
	 * Render bins onto image
	 */
	private void renderBins(Graphics g, int[] bins)
		{
		g.setColor(Color.BLACK);
		for(int ax=0;ax<bins.length;ax++)
			g.drawLine(ax, height, ax, height-bins[ax]);
		}
	
	/**
	 * Generate image of histogram
	 */
	private void makeImage()
		{
		//Only int values will be fast for now. no floating point
		currentImage[0]=currentImage[0].convertToInt(true); 

		BufferedImage bim=cachedImage=new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_BYTE_GRAY);
		Graphics g2=bim.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		if(currentImage[0].getType()==EvPixelsType.INT)
			renderBins(g2,calculateHistogram(currentImage[0].getArrayInt()));
		}
	
	

	/**
	 * Transform to screen coordinates
	 */
	public int toScreenX(int x)
		{
		int screenWidth=getWidth();
		return x*screenWidth/rangeMax;
		}

	/**
	 * Transform to world coordinates
	 */
	public int toWorldX(int x)
		{
		int screenWidth=getWidth();
		return x*rangeMax/screenWidth;
		}

	
	@Override
	protected void paintComponent(Graphics g)
		{
		//Recalculate histogram if component size changes
		if(cachedImage!=null && getWidth()!=cachedImage.getWidth())
			cachedImage=null;
		
		if(currentImage!=null)
			{
			if(cachedImage==null)
				makeImage();
			
			//Image is opaque, no need to clear background
			g.drawImage(cachedImage, 0, 0, null);
			}
		else
			{
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	
	}
