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
	private int rangeMax;
	
	private EvPixels currentImage;
	private BufferedImage cachedImage=null; //cached image
	
	private int lastWidth=0;

	private int height=50;
	
	
	public void setImage(EvPixels p, int numBits)
		{
		cachedImage=null;
		
		currentImage=p;
		
		//rangeMin=0;
		rangeMax=2<<numBits-1;
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
		
		for(int v:p)
			{
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
		currentImage=currentImage.convertToInt(true); 

		BufferedImage bim=cachedImage=new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_BYTE_GRAY);
		Graphics g2=bim.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		if(currentImage.getType()==EvPixelsType.INT)
			renderBins(g2,calculateHistogram(currentImage.getArrayInt()));
		}
	
	@Override
	protected void paintComponent(Graphics g)
		{
		//Recalculate histogram if component size changes
		if(getWidth()!=lastWidth)
			{
			cachedImage=null;
			lastWidth=getWidth();
			}
		if(cachedImage==null)
			makeImage();
		
		//Image is opaque, no need to clear background
		g.drawImage(cachedImage, 0, 0, null);
		}
	
	}
