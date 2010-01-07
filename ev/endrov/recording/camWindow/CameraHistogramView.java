package endrov.recording.camWindow;

import java.awt.Color;
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
	int screenWidth;
	
	int rangeMin;
	int rangeMax;
//	private int[] bins; //Cached histogram
	BufferedImage cachedImage=null;//new BufferedImage(width, height, imageType);
	private int lastWidth=0;
	
	
	EvPixels currentImage;
	
	public void setImage(EvPixels p)
		{
		cachedImage=null;
		
		currentImage=p;
		
		}
	
	
	private void setInt(int[] p, int screenWidth, int rangeMax)
		{
		this.screenWidth=screenWidth;
		int[] bins=new int[screenWidth];
		
		for(int v:p)
			{
			int i=v*screenWidth/255;
			bins[i]++;
			}

		int totalNum=p.length;
		for(int i=0;i<bins.length;i++)
			bins[i]/=totalNum;

		
		
	//	this.bins=bins;
		}
	
	
	private void makeImage()
		{
		BufferedImage bim=new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_BYTE_GRAY);
		Graphics g2=bim.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		
		

		//TODO
		
		rangeMin=0;
		rangeMax=255;
		
		cachedImage=new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics g=cachedImage.getGraphics();
		
		
		
		if(currentImage.getType()==EvPixelsType.INT)
			setInt(currentImage.getArrayInt(), screenWidth, rangeMax);
		
		
		}
	
	@Override
	protected void paintComponent(Graphics g)
		{
		
		if(getWidth()!=lastWidth)
			{
			cachedImage=null;
			lastWidth=getWidth();
			}
		
		makeImage();
		
		g.drawImage(cachedImage, 0, 0, null);
		
		}
	
	}
