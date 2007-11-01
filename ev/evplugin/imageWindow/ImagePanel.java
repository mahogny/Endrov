package evplugin.imageWindow;

import java.util.*;
import java.awt.image.*;
import java.awt.*;
import javax.swing.*;
import javax.vecmath.Vector2d;

import evplugin.imageset.*;
import evplugin.ev.*;


/**
 * Fast image viewer with various filters. Very raw implementation, meant to be extended.
 * 
 * @author Johan Henriksson
 */
public class ImagePanel extends JPanel
	{
	static final long serialVersionUID=0; //wtf
	public Vector<ImagePanelImage> images=new Vector<ImagePanelImage>();
	public double zoom=1;
	public double rotation=0;
	public double transX=0, transY=0;

	
	/**
	 * One image to be drawn in the panel
	 */
	public static class ImagePanelImage
		{
		public EvImage image=null;
		public double contrast=1;
		public double brightness=0;
		private BufferedImage bufi=null;
		
		public void update()
			{
			bufi=null;
			}

		/**
		 * Load image into memory
		 */
		private void loadImage()
			{
			try
				{
				if(image==null)
					bufi=null;
				else
					{
					//Load image if this has not already been done
					if(bufi==null)
						{
						bufi=image.getJavaImage();
						if(bufi==null)
							throw new Exception();
						ContrastBrightnessOp bcfilter=new ContrastBrightnessOp(contrast,brightness);
						BufferedImage bufo=new BufferedImage(bufi.getWidth(), bufi.getHeight(), bufi.getType());
						bcfilter.filter(bufi,bufo);
						bufi=bufo;
						
						//System.out.println("t"+bufi.getType()+" "+BufferedImage.TYPE_INT_RGB+" "+BufferedImage.TYPE_3BYTE_BGR+" "+BufferedImage.TYPE_BYTE_GRAY);
						}
					}
				}
			catch(Exception e)
				{
				Log.printError("image failed to load",e);
				}
			}
		
		
		/**
		 * Zoom image to fit panel
		 */
		public void zoomToFit(ImagePanel p)
			{
			loadImage();
			if(bufi!=null)
				{
				int w=bufi.getWidth()*image.getBinning();
				int h=bufi.getHeight()*image.getBinning();
							
				//Adjust zoom
				double zoom1=p.getWidth()/(double)w;
				double zoom2=p.getHeight()/(double)h;
				if(zoom1<zoom2)
					p.zoom=zoom1;
				else
					p.zoom=zoom2;
				
				//Place camera in the middle
				p.transX=-w/2-image.getDispX();
				p.transY=-h/2-image.getDispY();
				}
			}
		
		
		public void paintComponent(Graphics g, ImagePanel p, Integer b)
			{
			Graphics2D g2 = (Graphics2D)g; 			
			if(bufi!=null)
				{
				//Calculate translation and zoom of image
				Vector2d trans=p.transformI2S(new Vector2d(image.getDispX(), image.getDispY()));
				double zoomBinning=p.zoom*image.getBinning();
				double invZoomBinning=1.0/zoomBinning;

				g2.translate(trans.x,trans.y);
				g2.scale(zoomBinning,zoomBinning);
				g2.rotate(p.rotation);
				
				if(b!=null)
					{
					Composite origComp=g2.getComposite();
					g2.setComposite(new CompositeColor(b));
					g2.drawImage(bufi, null, 0, 0);
					g2.setComposite(origComp);
					}
				else
					g2.drawImage(bufi, null, 0, 0);
				
				g2.rotate(-p.rotation);
				g2.scale(invZoomBinning,invZoomBinning);
				g2.translate(-trans.x,-trans.y);
				} 
			}
		
		}
	
		
	
	/**
	 * Tell that images need be reloaded from disk.
	 * Better register callbacks from images instead
	 */
	public void invalidateImages()
		{
		for(ImagePanelImage im:images)
			im.update();
		}
	
	/**
	 * Load image from imageloader if this has not been done yet
	 *
	 */
	private void loadImage()
		{
		for(ImagePanelImage im:images)
			im.loadImage();
		}
	
	
	//Temporary drawing surfaces. Creation seems rather expensive so these are cached
	private BufferedImage temporaryTotal=null;
	private BufferedImage temporaryPart=null;
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g)
		{
//		long ma=System.currentTimeMillis();
		
		loadImage();
		if(images.size()==1)
			{
			g.setColor(Color.BLACK);
			g.fillRect(0,0,getWidth(),getHeight());
			images.get(0).paintComponent(g, this, null);
			}
		else
			{
			
			//With composite
			//for(int i=0;i<3 && i<images.size();i++)
			//	images.get(i).paintComponent(g, this,i);
			 
			if(temporaryTotal==null || temporaryTotal.getWidth()!=getWidth() || temporaryTotal.getHeight()!=getHeight())
				temporaryTotal=new BufferedImage(getWidth(), getHeight(),BufferedImage.TYPE_INT_RGB);
			if(images.size()<3)
				{
				Graphics temporaryTotalG=temporaryTotal.createGraphics();
				temporaryTotalG.setColor(Color.BLACK);
				temporaryTotalG.fillRect(0,0,getWidth(),getHeight());
				}
			int s[]=null;
			for(int i=0;i<3 && i<images.size();i++)
				{
				if(temporaryPart==null || temporaryPart.getWidth()!=getWidth() || temporaryPart.getHeight()!=getHeight())
					temporaryPart=new BufferedImage(getWidth(), getHeight(),BufferedImage.TYPE_BYTE_GRAY);
				if(s==null)
					s=new int[getWidth()*getHeight()];
				
				Graphics temporaryPartG=temporaryPart.createGraphics();
				temporaryPartG.setColor(Color.BLACK);
				temporaryPartG.fillRect(0,0,getWidth(),getHeight());

				
				ImagePanelImage im=images.get(i);
				Graphics2D g2=(Graphics2D)temporaryPart.getGraphics();
				im.paintComponent(g2, this, null);
				
				temporaryPart.getRaster().getSamples(0, 0, getWidth(), getHeight(), 0, s);
				temporaryTotal.getRaster().setSamples(0, 0, getWidth(), getHeight(), i	, s);
				}
			g.drawImage(temporaryTotal,0,0,null);
			}
			
	//	System.out.println("   "+(System.currentTimeMillis()-ma));
		
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


	
	/** Convert image coordinate to screen coordinate (image scaled by binning) */
	public Vector2d transformI2S(Vector2d u) //ok
		{
		Vector2d v=new Vector2d(u);
		Matrix2d rotmat=new Matrix2d();
		rotmat.rot(rotation);
		rotmat.transform(v);
		v.add(new Vector2d(transX,transY));
		v.scale(zoom);
		v.add(new Vector2d(getWidth()/2.0, getHeight()/2.0));
		return v;
		}
	
	/** Convert screen coordinate to image coordinate (image scaled by binning) */
	public Vector2d transformS2I(Vector2d u)
		{
		Vector2d v=new Vector2d(u);
		Matrix2d rotmat=new Matrix2d();
		rotmat.rot(-rotation);
		v.add(new Vector2d(-getWidth()/2.0, -getHeight()/2.0));
		v.scale(1.0/zoom);
		v.add(new Vector2d(-transX,-transY));
		rotmat.transform(v);
		return v;
		}
	
	
	/**
	 * Zoom image to fit panel
	 */
	public void zoomToFit()
		{
		loadImage();
		if(images.size()>0)
			images.get(0).zoomToFit(this);
		repaint();
		}
	}


