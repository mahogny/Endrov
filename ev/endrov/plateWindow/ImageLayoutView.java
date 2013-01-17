/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.plateWindow;

import java.util.*;
import java.awt.image.*;
import java.awt.*;

import javax.swing.*;
import javax.vecmath.Vector2d;


import endrov.basicWindow.EvColor;
import endrov.ev.*;
//import endrov.filterBasic.ContrastBrightnessOp;
import endrov.imageset.*;
import endrov.util.Matrix2d;
import endrov.util.ProgressHandle;


/**
 * Fast image viewer with various filters. 
 * 
 * This image is raw - it is meant to be extended.
 * 
 * TODO add hooks for drawing overlay?
 * 
 * TODO add viewports!
 * 
 * @author Johan Henriksson
 */
public class ImageLayoutView extends JPanel
	{
	static final long serialVersionUID=0;
	public Vector<ImagePanelImage> images=new Vector<ImagePanelImage>();
	public double zoom=1;
	public double rotation=0;
	public double transX=0, transY=0;

	

	public void dataChangedEvent()
		{
		}

	private static final byte clampByte(int i)
		{
		if(i > 255)
			return -1; //really correct? why -1????
		if(i < 0)
			return 0;
		else
			return (byte)i;
		}
	
	private static final byte clampShort(int i)
		{
		if(i > 65535)
			return -1; //really correct?
		if(i < 0)
			return 0;
		else
			return (byte)i;
		}
	
	static EvPixels applyOrig(EvPixels a, double contrast, double brightness)
		{
		if(a.getType()==EvPixelsType.AWT)
			{
			BufferedImage src=a.getAWT();
			
			int numBits=src.getSampleModel().getSampleSize(0);
			LookupOp f;
			
			if(numBits==8)
				{
				byte[] b=new byte[256];
				for(int i=0;i<256;i++)
					b[i]=clampByte((int)(i*contrast+brightness));     //Centralize contrast* maybe?
				ByteLookupTable table=new ByteLookupTable(0,b);
				f=new LookupOp(table,null);
				}
			else if(numBits==16)
				{
				short[] b=new short[65536];
				for(int i=0;i<65536;i++)
					b[i]=clampShort((int)(i*contrast+brightness));     //Centralize contrast* maybe?
				ShortLookupTable table=new ShortLookupTable(0,b);
				f=new LookupOp(table,null);
				
				}
			else
				f=null;
			
			WritableRaster wr = Raster.createWritableRaster(src.getSampleModel(),new Point(0,0));
			BufferedImage bufo = new BufferedImage(src.getColorModel(),wr,true,new Hashtable<Object,Object>());
			f.filter(src,bufo);
			
			return new EvPixels(bufo);
			}
		else
			{
			a=a.getReadOnly(EvPixelsType.DOUBLE);
			
			int w=a.getWidth();
			int h=a.getHeight();
			EvPixels out=new EvPixels(a.getType(),w,h);
			double[] aPixels=a.getArrayDouble();
			double[] outPixels=out.getArrayDouble();
			
			for(int i=0;i<aPixels.length;i++)
				{
				double c=aPixels[i]*contrast+brightness;
				if(c>255) c=255;
				else if(c<0) c=0;
				outPixels[i]=c;
				}
			
			return out;
			}
		}

	
	
	/**
	 * One image to be drawn in the panel
	 */
	public static class ImagePanelImage
		{
//		private EvImage image=null;
		int z;
		private EvStack stack=null;
		public double contrast=1;
		public double brightness=0;
		private BufferedImage bufi=null;
		public EvColor color;
		
		boolean drawBorder=true;
		
		
		
		public BufferedImage applyIntensityTransform(EvPixels p)
			{
			double contrastR=contrast*color.getRedDouble();
			double contrastG=contrast*color.getGreenDouble();
			double contrastB=contrast*color.getBlueDouble();
			
			
			int w=p.getWidth();
			int h=p.getHeight();
			double[] aPixels=p.convertToDouble(true).getArrayDouble();
			BufferedImage buf=new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
			
			byte[] outarr=new byte[w*h*3];
			
			for(int i=0;i<w*h;i++)
				{
				byte b=clampByte((int)(aPixels[i]*contrastB+brightness));
				byte g=clampByte((int)(aPixels[i]*contrastG+brightness));
				byte r=clampByte((int)(aPixels[i]*contrastR+brightness));
				outarr[i*3+0]=r;
				outarr[i*3+1]=g;
				outarr[i*3+2]=b;
				}

			buf.getRaster().setDataElements(0, 0, w, h, outarr);
			return buf;
			}
		
		
		
		
		
		
		
		public void setImage(EvStack stack, int z)//EvImage image)
			{
			this.z=z;
			this.stack=stack;
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
				if(stack==null)
					bufi=null;
				else
					{
					//Load image if this has not already been done
					if(bufi==null)
						{
						EvImage image=stack.getInt(z);
						
						//TODO handle progress. Put this in a thread if it takes too long. or, put it there right away, postpone update of image
						EvPixels p=image.getPixels(new ProgressHandle()); /////////////////////////////////////////// HERE FOR LONG EXECUTIONS /////////////
						bufi=applyIntensityTransform(p);
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
		public void zoomToFit(ImageLayoutView p)
			{
			loadImage();
			if(bufi!=null)
				{
				int w=stack.getWidth();
				int h=stack.getHeight();
							
				//Adjust zoom
				double zoom1=p.getWidth()/(double)(w*stack.resX);
				double zoom2=p.getHeight()/(double)(h*stack.resY);
				p.zoom=Math.min(zoom1,zoom2);

				//Place camera in the middle
				Vector2d mid=transformI2S(p,stack,new Vector2d(w/2,h/2));
				mid.sub(new Vector2d(p.getWidth()/2,p.getHeight()/2));
				p.transX-=(int)mid.x/p.zoom;
				p.transY-=(int)mid.y/p.zoom;
				}
			}
		
		public void paintComponent(Graphics g, ImageLayoutView p)
			{
			Graphics2D g2 = (Graphics2D)g; 			
			if(bufi!=null)
				{
				//Calculate translation and zoom of image
				//Vector2d trans=transformI2S(p,stack,new Vector2d(stack.dispX, stack.dispY));
				Vector2d trans=transformI2S(p,stack,new Vector2d(0, 0));

				
				double scaleX=p.zoom*stack.resX;
				double scaleY=p.zoom*stack.resY;
				
				g2.translate(trans.x,trans.y);
				g2.scale(scaleX,scaleY);  
				g2.rotate(p.rotation);
				g2.drawImage(bufi, null, 0, 0);
				g2.rotate(-p.rotation);
				g2.scale(1/scaleX,1/scaleY); 
				g2.translate(-trans.x,-trans.y);
				
				
				/*
				Vector2d v1=p.transformPointW2S(new Vector2d(0,0));
				Vector2d v2=p.transformPointW2S(new Vector2d(100,0));
				Vector2d v3=p.transformPointW2S(new Vector2d(0,100));
				
				g2.setColor(Color.RED);
				g2.drawLine((int)v1.x, (int)v1.y, (int)v2.x, (int)v2.y);
				g2.drawLine((int)v1.x, (int)v1.y, (int)v3.x, (int)v3.y); 
				
				*/
//				stack.cs.transformToWorld(v)


				} 
			
			if(drawBorder)
				{
				int w=stack.getWidth();
				int h=stack.getHeight();
				
				//Reference area. This is what the transform decides on; the image above should be in it
				g2.setColor(Color.GREEN); //No displacement
				//actually already displaced. red is double-displaced
				Vector2d u1=transformI2S(p,stack,new Vector2d(0,0));
				Vector2d u2=transformI2S(p,stack,new Vector2d(w,0));
				Vector2d u3=transformI2S(p,stack,new Vector2d(0,h));
				Vector2d u4=transformI2S(p,stack,new Vector2d(w,h));
				g2.drawLine((int)u1.x, (int)u1.y, (int)u2.x, (int)u2.y);
				g2.drawLine((int)u3.x, (int)u3.y, (int)u4.x, (int)u4.y);
				}

			
			}
		
		


		/** 
		 * Convert image coordinate to screen coordinate (image scaled by binning) 
		 */
		public static Vector2d transformI2S(ImageLayoutView p, EvStack stack, Vector2d u)
			{
			return p.transformPointW2S(stack.transformImageWorld(new Vector2d(u.x,u.y)));
			}
		
		
		
		/** 
		 * Convert screen coordinate to image coordinate (image scaled by binning) 
		 */
		public static Vector2d transformS2I(ImageLayoutView p, EvStack stack, Vector2d u)
			{
			return stack.transformWorldImage(p.transformPointS2W(u));
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
	
	
	private static class AddCompositeContext implements CompositeContext
		{
	
		ColorModel srcColorModel;
		ColorModel dstColorModel;
	
		int ALPHA = 0xFF000000; // alpha mask
		int MASK7Bit = 0xFEFEFF; // mask for additive/subtractive shading
	
		public AddCompositeContext(ColorModel srcColorModel, ColorModel dstColorModel) 
			{
			this.srcColorModel = srcColorModel;
			this.dstColorModel = dstColorModel;
			}
	
		int add(int color1, int color2) 
			{
			int pixel = (color1 & MASK7Bit) + (color2 & MASK7Bit);
			int overflow = pixel & 0x1010100;
			overflow = overflow - (overflow >> 8);
			return ALPHA | overflow | pixel;
			}
	
	
		public void compose(Raster src, Raster dstIn, WritableRaster dstOut)
			{
			Rectangle srcRect = src.getBounds();
			Rectangle dstInRect = dstIn.getBounds();
			Rectangle dstOutRect = dstOut.getBounds();
			int x = 0, y = 0;
			int w = Math.min(Math.min(srcRect.width, dstOutRect.width), dstInRect.width);
			int h = Math.min(Math.min(srcRect.height, dstOutRect.height), dstInRect.height);
			Object srcPix = null, dstPix = null;
			for (y = 0; y < h; y++)
				for (x = 0; x < w; x++) 
					{
					srcPix = src.getDataElements(x + srcRect.x, y + srcRect.y, srcPix);
					dstPix = dstIn.getDataElements(x + dstInRect.x, y + dstInRect.y, dstPix);
					int sp = srcColorModel.getRGB(srcPix);
					int dp = dstColorModel.getRGB(dstPix);
					int rp = add(sp,dp);
					dstOut.setDataElements(x + dstOutRect.x, y + dstOutRect.y, dstColorModel.getDataElements(rp, null));
					}
			}
	
		public void dispose()
			{
			}
	
		}
			

	public void paintComponent(Graphics g)
		{
		BufferedImage bim=new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2d=(Graphics2D)bim.getGraphics();

		loadImage();

		g2d.setColor(Color.BLACK);
		g2d.fillRect(0,0,getWidth(),getHeight());

		for(int i=0;i<images.size();i++)
			{
			if(i==0)
				{
				images.get(0).paintComponent(g2d, this);
				}
			else
				{
				//Draw overlay additive
				g2d.setComposite(AlphaComposite.Src);
				g2d.setComposite(
						new Composite(){
						public CompositeContext createContext(
								ColorModel srcColorModel, 
								ColorModel dstColorModel, 
								RenderingHints arg2) 
							{
							return new AddCompositeContext(srcColorModel,dstColorModel);
							}
						}
				);

				ImagePanelImage im=images.get(i);
				im.paintComponent(g2d, this);
				}
			}
		
		g.drawImage(bim, 0,0, null);
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
		Vector2d v1=transformPointS2W(new Vector2d(getWidth()/2,getHeight()/2)); 
		rotation+=angle;
		//Where is it after rotation?
		v1=transformPointW2S(v1); 
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
	public Vector2d transformPointW2S(Vector2d u)
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
	public Vector2d transformPointS2W(Vector2d u)
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
	
	/** Transform screen vector to world vector */
	public Vector2d transformVectorS2W(Vector2d u)
		{
		Vector2d v=new Vector2d(u);
		v.scale(1.0/zoom);
		
		Matrix2d rotmat=new Matrix2d();
		rotmat.rot(-rotation);
		rotmat.transform(v);
		return v;
		}
	}


