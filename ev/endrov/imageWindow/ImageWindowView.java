/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
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
public class ImageWindowView extends JPanel
	{
	static final long serialVersionUID=0;
	public Vector<ImagePanelImage> images=new Vector<ImagePanelImage>();
	public double zoom=1;
	public double rotation=0;
	public double transX=0, transY=0;

	
	/** Extension: Overlay renderers */
	public final Vector<ImageWindowRenderer> imageWindowRenderers=new Vector<ImageWindowRenderer>();

	public void dataChangedEvent()
		{
		for(ImageWindowRenderer r:imageWindowRenderers)
			r.dataChangedEvent();
		}

	
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
				EvLog.printError("image panel: image failed to load",e);
				}
			}
		
		
		/**
		 * Zoom image to fit panel
		 */
		public void zoomToFit(ImageWindowView p)
			{
			loadImage();
			if(bufi!=null)
				{
				int w=(int)(bufi.getWidth());
				int h=(int)(bufi.getHeight());
							
				//Adjust zoom
				double zoom1=stack.getResbinX()*p.getWidth()/(double)w;
				double zoom2=stack.getResbinY()*p.getHeight()/(double)h;
				p.zoom=Math.min(zoom1,zoom2);

				//Place camera in the middle
				Vector2d mid=transformI2S(p,stack,new Vector2d(w/2,h/2));
				mid.sub(new Vector2d(p.getWidth()/2,p.getHeight()/2));
				p.transX-=(int)mid.x/p.zoom;
				p.transY-=(int)mid.y/p.zoom;
				}
			}
		
		public void paintComponent(Graphics g, ImageWindowView p)
			{
			Graphics2D g2 = (Graphics2D)g; 			
			if(bufi!=null)
				{
				//Calculate translation and zoom of image
				//Vector2d trans=transformI2S(p,stack,new Vector2d(stack.dispX, stack.dispY));
				Vector2d trans=transformI2S(p,stack,new Vector2d(0, 0));

				
				double scaleX=p.zoom/stack.getResbinX();
				double scaleY=p.zoom/stack.getResbinY();
				
				g2.translate(trans.x,trans.y);
				g2.scale(scaleX,scaleY);  
				g2.rotate(p.rotation);
				g2.drawImage(bufi, null, 0, 0);
				g2.rotate(-p.rotation);
				g2.scale(1/scaleX,1/scaleY); 
				g2.translate(-trans.x,-trans.y);
				
				//Reference area. This is what the transform decides on; the image above should be in it
				/*
				g2.setColor(Color.GREEN); //No displacement
				//actually already displaced. red is double-displaced
				Vector2d u1=transformI2S(p,stack,new Vector2d(0,0));
				Vector2d u2=transformI2S(p,stack,new Vector2d(bufi.getWidth(),0));
				Vector2d u3=transformI2S(p,stack,new Vector2d(0,bufi.getHeight()));
				Vector2d u4=transformI2S(p,stack,new Vector2d(bufi.getWidth(),bufi.getHeight()));
				g2.drawLine((int)u1.x, (int)u1.y, (int)u2.x, (int)u2.y);
				g2.drawLine((int)u3.x, (int)u3.y, (int)u4.x, (int)u4.y);

				g2.setColor(Color.RED); //with displacement
				Vector2d v1=transformI2S(p,stack,new Vector2d(stack.dispX,stack.dispY));
				Vector2d v2=transformI2S(p,stack,new Vector2d(bufi.getWidth()+stack.dispX,stack.dispY));
				Vector2d v3=transformI2S(p,stack,new Vector2d(0,bufi.getHeight()+stack.dispY));
				Vector2d v4=transformI2S(p,stack,new Vector2d(bufi.getWidth()+stack.dispX,bufi.getHeight()+stack.dispY));
				g2.drawLine((int)v1.x, (int)v1.y, (int)v2.x, (int)v2.y);
				g2.drawLine((int)v3.x, (int)v3.y, (int)v4.x, (int)v4.y);
				*/

				} 
			}
		
		


		/** Convert image coordinate to screen coordinate (image scaled by binning) */
		
		//Problem: should now be stack specific! currently ignores binning as stated above
		public static Vector2d transformI2S(ImageWindowView p, EvStack stack, Vector2d u)
			{
			return p.transformW2S(new Vector2d(stack.transformImageWorldX(u.x),stack.transformImageWorldY(u.y)));
			}
		
		
		
		/** Convert screen coordinate to image coordinate (image scaled by binning) */
		//Problem: should now be stack specific!
		public static Vector2d transformS2I(ImageWindowView p, EvStack stack, Vector2d u)
			{
			Vector2d v=p.transformS2W(u);
			return new Vector2d(stack.transformWorldImageX(v.x),stack.transformWorldImageY(v.y));
			}
		
		
		}
	
	
	
	
	public boolean checkIfTransformOk()
		{
		return zoom!=0; 
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
		
		
		
		}

	/**
	 * Pan by a certain ammount
	 * @param dx Mouse movement X in pixels
	 * @param dy Mouse movement Y in pixels
	 */
	public void pan(double dx, double dy)
		{
		transX+=dx/zoom;
		transY+=dy/zoom;
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

	
	/**
	 * Rotate camera by given angle, with center point in the middle of the screen
	 */
	public void rotateCamera(double angle)
		{
		//Where is the middle of the screen now?
		Vector2d v1=transformS2W(new Vector2d(getWidth()/2,getHeight()/2)); 
		rotation+=angle;
		//Where is it after rotation?
		v1=transformW2S(v1); 
		//How much has it moved?
		Vector2d v2=new Vector2d(getWidth()/2,getHeight()/2);
		v2.sub(v1);
		//Move back
		pan(v2.x,v2.y);
		repaint();
		}

	
	/** 
	 * Scale screen vector to world vector 
	 */
	public double scaleS2w(double s) {return s/zoom;}
	
	/**
	 * Scale world to screen vector 
	 */
	public double scaleW2s(double w) {return w*zoom;}

	
	
	
	
	
	/**
	 * Transform world coordinate to screen coordinate 
	 */
	public Vector2d transformW2S(Vector2d u)
		{
		Vector2d v=new Vector2d(u);
		Matrix2d rotmat=new Matrix2d();
		rotmat.rot(rotation);
		rotmat.transform(v);
		
		
		v.add(new Vector2d(transX,transY));  //TODO change order of zoom and trans
		v.scale(zoom);
		v.add(new Vector2d(getWidth()/2.0, getHeight()/2.0));
		return v;
		}
		
	/** Transform screen coordinate to world coordinate */
	public Vector2d transformS2W(Vector2d u)
		{
		Vector2d v=new Vector2d(u);
		v.add(new Vector2d(-getWidth()/2.0, -getHeight()/2.0));
		v.scale(1.0/zoom);
		v.add(new Vector2d(-transX,-transY));  //TODO change order of zoom and trans
		
		Matrix2d rotmat=new Matrix2d();
		rotmat.rot(-rotation);
		rotmat.transform(v);
		return v;
		}
	}


