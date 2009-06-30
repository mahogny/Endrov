package endrov.imageWindow;

import java.util.*;
import java.awt.image.*;
import java.awt.*;
import javax.swing.*;
import javax.vecmath.Vector2d;

import endrov.ev.*;
//import endrov.filterBasic.ContrastBrightnessOp;
import endrov.imageset.*;
import endrov.util.Matrix2d;


/**
 * Fast image viewer with various filters. Very raw implementation, meant to be extended.
 * 
 * @author Johan Henriksson
 */
public class ImagePanel extends JPanel
	{
	static final long serialVersionUID=0;
	public Vector<ImagePanelImage> images=new Vector<ImagePanelImage>();
	public double zoom=1;
	public double rotation=0;
	public double transX=0, transY=0;

	
	/**
	 * One image to be drawn in the panel
	 */
	public static class ImagePanelImage
		{
		private EvImage image=null;
		private EvStack stack=null;
		public double contrast=1;
		public double brightness=0;
		private BufferedImage bufi=null;
		
		public void setImage(EvStack stack, EvImage image)
			{
			this.image=image;
			this.stack=stack;
			}
		
		public EvImage getImage()
			{
			return image;
			}

		public EvStack getStack()
			{
			return stack;
			}
		
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
						/*
						bufi=image.getPixels().quickReadOnlyAWT();
						if(bufi==null)
							throw new Exception("Got null image from I/O");
						
						ContrastBrightnessOp bcfilter=new ContrastBrightnessOp(contrast,brightness);
						
						WritableRaster wr = Raster.createWritableRaster(bufi.getSampleModel(),new Point(0,0));
						BufferedImage bufo = new BufferedImage(bufi.getColorModel(),wr,true,new Hashtable<Object,Object>());
						bcfilter.filter(bufi,bufo);
						bufi=bufo;*/

						bufi=EvOpImageMapScreen.apply(image.getPixels(), contrast, brightness).quickReadOnlyAWT();
						}
					}
				}
			catch(Exception e)
				{
				EvLog.printError("image failed to load",e);
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
				int w=(int)(bufi.getWidth()*stack.binning);
				int h=(int)(bufi.getHeight()*stack.binning);
							
				//Adjust zoom
				double zoom1=p.getWidth()/(double)w;
				double zoom2=p.getHeight()/(double)h;
				if(zoom1<zoom2)
					p.zoom=zoom1;
				else
					p.zoom=zoom2;
				
				//Place camera in the middle
				p.transX=-w/2-stack.dispX;
				p.transY=-h/2-stack.dispY;
				}
			}
		
		public void paintComponent(Graphics g, ImagePanel p)
			{
			Graphics2D g2 = (Graphics2D)g; 			
			if(bufi!=null)
				{
				//Calculate translation and zoom of image
				Vector2d trans=p.transformI2S(new Vector2d(stack.dispX, stack.dispY));
				double zoomBinning=p.zoom*stack.binning;
				double invZoomBinning=1.0/zoomBinning;

				g2.translate(trans.x,trans.y);
				g2.scale(zoomBinning,zoomBinning);
				g2.rotate(p.rotation);
				
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
			images.get(0).paintComponent(g, this);
			}
		else
			{
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
				im.paintComponent(g2, this);
				
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
		v.add(new Vector2d(-getWidth()/2.0, -getHeight()/2.0));
		v.scale(1.0/zoom);
		v.add(new Vector2d(-transX,-transY));
		
		Matrix2d rotmat=new Matrix2d();
		rotmat.rot(-rotation);
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
	
	
	public void rotateCamera(double angle)
		{
		//Where is the middle of the screen now?
		Vector2d v1=transformS2I(new Vector2d(getWidth()/2,getHeight()/2)); 
		rotation+=angle;
		//Where is it after rotation?
		v1=transformI2S(v1); 
		//How much has it moved?
		Vector2d v2=new Vector2d(getWidth()/2,getHeight()/2);
		//Vector2d v2=transformS2I(new Vector2d(getWidth()/2,getHeight()/2));
		v2.sub(v1);
		//Move back
		pan(v2.x,v2.y);
		//transX-=v2.x;
		//transY-=v2.y;
		repaint();
		}

	}


