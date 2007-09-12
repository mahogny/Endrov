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
	public ImageLoader imageLoader=null;
	public double contrast=1;
	public double brightness=0;
	public double zoom=1;
	public int binning=1;
	public double transX=0, transY=0;
	public double dispX=0, dispY=0;
	
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
			if(imageLoader==null)
				bufi=null;
			
			//Load image if this has not already been done
			if(bufi==null && imageLoader!=null)
				{
				bufi=imageLoader.loadImage();
				if(bufi==null)
					throw new Exception();
				ContrastBrightnessOp bcfilter=new ContrastBrightnessOp(contrast,brightness);
				bcfilter.filter(bufi,bufi);
				}
			}
		catch(Exception e)
			{
			EV.printError("image failed to load"/*: "+filename*/,e);
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
			double tx=i2sx(dispX);
			double ty=i2sy(dispY);
			double zoomBinning=zoom*binning;
			double invZoomBinning=1.0/zoomBinning;

			//TODO: readd centering, maybe not here?
			
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
	 * @param dy Mouse movement X in pixels
	 */
	public void pan(double dx, double dy)
		{
		transX+=(double)dx/zoom;
		transY+=(double)dy/zoom;
		}

	/**
	 * Convert screen coordinate to image coordinate (image scaled by binning)
	 * @param sx Screen x coordinate
	 * @return Image x coordinate
	 */
	public double s2ix(double sx)
		{
		/*
		int w=bufi.getWidth()*binning;
		int h=bufi.getHeight()*binning;
		return (sx-(getWidth()/2  - w*zoom/2.0  + transX))/zoom;
		*/
		return (sx-getWidth()/2)/zoom  - transX; //fixed
		}

	/**
	 * Convert screen coordinate to image coordinate (image scaled by binning)
	 * @param sy Screen y coordinate
	 * @return Image y coordinate
	 */
	public double s2iy(double sy)
		{
		/*
		int w=bufi.getWidth()*binning;
		int h=bufi.getHeight()*binning;
		return (sy-(getHeight()/2.0  - h*zoom/2.0  + transY))/zoom;
		*/
		return (sy-getHeight()/2.0)/zoom    - transY; //fixed
		}

	/**
	 * Convert image coordinate to screen coordinate (image scaled by binning)
	 * @param ix Image x coordinate
	 * @return Screen x coordinate
	 */
	public double i2sx(double ix)
		{
		/*
		int w=bufi.getWidth()*binning;
		int h=bufi.getHeight()*binning;
		return getWidth()/2.0  - w*zoom/2.0  + transX*zoom + ix*zoom;
		*/
		return getWidth()/2.0   + transX*zoom + ix*zoom;
		}

	/**
	 * Convert image coordinate to screen coordinate (image scaled by binning)
	 * @param iy Image y coordinate
	 * @return Screen y coordinate
	 */
	public double i2sy(double iy)
		{
		/*
		int w=bufi.getWidth()*binning;
		int h=bufi.getHeight()*binning;
		return getHeight()/2.0  - h*zoom/2.0  + transY*zoom + iy*zoom;
		*/
		return getHeight()/2.0   + transY*zoom + iy*zoom;
		}
		

	/**
	 * Zoom image to fit panel
	 */
	public void zoomToFit()
		{
		loadImage();
		if(bufi!=null)
			{
			int w=bufi.getWidth()*binning;
			int h=bufi.getHeight()*binning;
						
			//Adjust zoom
			double zoom1=getWidth()/(double)w;
			double zoom2=getHeight()/(double)h;
			if(zoom1<zoom2)
				zoom=zoom1;
			else
				zoom=zoom2;
			
			//Place camera in the middle
			transX=-w/2-dispX;
			transY=-h/2-dispY;
			
			repaint();
			}
		}
	}


