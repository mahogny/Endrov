package evplugin.imageWindow;

import java.awt.image.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.*;

import evplugin.imageset.*;
import evplugin.ev.*;

/**
 * Fast image viewer with various filters. Very raw implementation, meant to be extended.
 * 
 * @author Johan Henriksson
 */
public class ImagePanel extends JPanel
	{
	public EvImage image=null;
	public double contrast=1;
	public double brightness=0;
	public double zoom=1;
	public double transX=0, transY=0;
	
	private BufferedImage bufi=null;
	
	static final long serialVersionUID=0; //wtf
	
		
	/**
	 * Tell that variables has been updated and redraw is needed
	 */
	public void update()
		{
		bufi=null;
		repaint();
		}
	
	/**
	 * Load image from imageloader if this has not been done yet
	 *
	 */
	private void loadImage()
		{
		try
			{
			if(image==null)
				bufi=null;
			
			//Load image if this has not already been done
			if(bufi==null && image!=null)
				{
				bufi=image.getJavaImage();
				if(bufi==null)
					throw new Exception();
				ContrastBrightnessOp bcfilter=new ContrastBrightnessOp(contrast,brightness);
				//System.out.println("A "+System.currentTimeMillis());
				//TODO
				BufferedImage bufo=new BufferedImage(bufi.getWidth(), bufi.getHeight(), bufi.getType());
				bcfilter.filter(bufi,bufo);
				bufi=bufo;
				//System.out.println("B "+System.currentTimeMillis());
				}
			}
		catch(Exception e)
			{
			Log.printError("image failed to load"/*: "+filename*/,e);
			}
		}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g)
		{
		g.fillRect(0,0,getWidth(),getHeight());
		Graphics2D g2 = (Graphics2D)g; 
		
		loadImage();
		
		if(bufi!=null)
			{
			//Calculate translation and zoom of image
			double tx=i2sx(image.getDispX());
			double ty=i2sy(image.getDispY());
			double zoomBinning=zoom*image.getBinning();
			double invZoomBinning=1.0/zoomBinning;

			g2.translate(tx,ty);
			g2.scale(zoomBinning,zoomBinning);
			g2.drawImage(bufi, null, 0, 0);
			g2.scale(invZoomBinning,invZoomBinning);
			g2.translate(-tx,-ty);
			} 
		}

	/**
	 * Pan by a certain ammount
	 * @param dx Mouse movement X in pixels
	 * @param dy Mouse movement Y in pixels
	 */
	public void pan(double dx, double dy)
		{
		transX+=(double)dx/zoom;
		transY+=(double)dy/zoom;
		}

	/** Convert screen coordinate to image coordinate (image scaled by binning) */
	public double s2ix(double sx){return (sx-getWidth() /2.0)/zoom  - transX;}
	/** Convert screen coordinate to image coordinate (image scaled by binning) */
	public double s2iy(double sy){return (sy-getHeight()/2.0)/zoom  - transY;}

	/** Convert image coordinate to screen coordinate (image scaled by binning) */
	public double i2sx(double ix){return getWidth()/2.0   + (transX + ix)*zoom;}
	/** Convert image coordinate to screen coordinate (image scaled by binning) */
	public double i2sy(double iy){return getHeight()/2.0  + transY*zoom + iy*zoom;}
		

	/**
	 * Zoom image to fit panel
	 */
	public void zoomToFit()
		{
		loadImage();
		if(bufi!=null)
			{
			int w=bufi.getWidth()*image.getBinning();
			int h=bufi.getHeight()*image.getBinning();
						
			//Adjust zoom
			double zoom1=getWidth()/(double)w;
			double zoom2=getHeight()/(double)h;
			if(zoom1<zoom2)
				zoom=zoom1;
			else
				zoom=zoom2;
			
			//Place camera in the middle
			transX=-w/2-image.getDispX();
			transY=-h/2-image.getDispY();
			
			repaint();
			}
		}
	}


