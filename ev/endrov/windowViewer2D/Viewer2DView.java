/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer2D;

import java.util.*;
import java.awt.image.*;
import java.awt.*;

import javax.swing.*;
import javax.vecmath.Vector2d;


import endrov.core.log.EvLog;
import endrov.gui.EvColor;
//import endrov.filterBasic.ContrastBrightnessOp;
import endrov.typeImageset.*;
import endrov.util.ProgressHandle;
import endrov.util.math.Matrix2d;


/**
 * Fast image viewer with various filters. Very raw implementation, meant to be extended.
 * 
 * @author Johan Henriksson
 */
public class Viewer2DView extends JPanel
	{
	static final long serialVersionUID=0;
	public Vector<ImagePanelImage> images=new Vector<ImagePanelImage>();
	public double zoom=1;
	public double rotation=0;
	public double transX=0, transY=0;

	
	/** Extension: Overlay renderers */
	public final Vector<Viewer2DRenderer> imageWindowRenderers=new Vector<Viewer2DRenderer>();

	public void dataChangedEvent()
		{
		for(Viewer2DRenderer r:imageWindowRenderers)
			r.dataChangedEvent();
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
	 * Map from one color to a weight of colors, for fast additive rendering
	 */
	/*static BufferedImage apply(EvPixels a, double contrast, double brightness, double weight1, double weight2, double weight3)
		{*/
		/*if(a.getType()==EvPixelsType.AWT)
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
		else*/
	/*
			{
			a=a.getReadOnly(EvPixelsType.DOUBLE);
			
			//Keep an optimized integer case?
			
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

			p.awt=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
			p.awt.getRaster().setPixels(0, 0, w, h, arrayI);

			return out;
			}
		}

	*/
	
	
	/**
	 * One image to be drawn in the panel
	 */
	public static class ImagePanelImage
		{
		private EvImagePlane image=null;
		private EvStack stack=null;
		public double contrast=1;
		public double brightness=0;
		private BufferedImage bufi=null;
		public EvColor color;
		
		
		
		
		public BufferedImage applyIntensityTransform(EvPixels p)
			{
			double contrastR=contrast*color.getRedDouble();
			double contrastG=contrast*color.getGreenDouble();
			double contrastB=contrast*color.getBlueDouble();
			
			//if(1==1)
				//{
				
				int w=p.getWidth();
				int h=p.getHeight();
				double[] aPixels=p.convertToDouble(true).getArrayDouble();
				BufferedImage buf=new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
				
				
				//System.out.println("Got image, first pixel values "+p.convertToInt(true).getArrayInt()[0]+"   "+p.convertToInt(true).getArrayInt()[1]);
				

				byte[] outarr=new byte[w*h*3];
				//DataBufferByte out=new DataBufferByte(w*h*3);
				//double[]
				
				for(int i=0;i<w*h;i++)
					{
					//double c=aPixels[i]*contrast+brightness;
					byte b=clampByte((int)(aPixels[i]*contrastB+brightness));
					byte g=clampByte((int)(aPixels[i]*contrastG+brightness));
					byte r=clampByte((int)(aPixels[i]*contrastR+brightness));
					//System.out.println(b);
					/*if(c>255) c=255;
					else if(c<0) c=0;*/
					outarr[i*3+0]=r;
					outarr[i*3+1]=g;
					outarr[i*3+2]=b;
					//out.
					}

				//bufi.getRaster().setPixels(0, 0, w, h, arrayI);
				
				//DataBufferByte out=new DataBufferByte(outarr, w*h*3);
				buf.getRaster().setDataElements(0, 0, w, h, outarr);
				//bufi.getRaster().set
				
				return buf;
				
			//	}
				/*
			else
				{
			//This works for grayscale!
				BufferedImage buf=EvOpImageMapScreen.apply(p, contrast, brightness).quickReadOnlyAWT();
				return buf;
				}*/
			
			
			
			
			
			
			
			
			
			
			
			}
		
		
		
		
		
		
		
		public void setImage(EvStack stack, EvImagePlane image)
			{
			/*
			if(image==null)
				System.out.println("Got image, null");
			else
				System.out.println("got image "+image+"  type "+image.getPixels(null).getType());
			*/
//			System.out.println("Got image 2, first pixel values int "+image.getPixels(null).getArrayInt()[0]+"   "+image.getPixels(null).getArrayInt()[1]);
			//System.out.println("Got image 2, first pixel values "+image.getPixels(null).convertToInt(true).getArrayInt()[0]+"   "+image.getPixels(null).convertToInt(true).getArrayInt()[1]);
			this.image=image;
			this.stack=stack;
			}
		
		public EvImagePlane getImage()
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
				//System.out.println("in load image, "+image);
				
				if(image==null)
					bufi=null;
				else
					{
					//Load image if this has not already been done
					if(bufi==null)
						{
						//TODO handle progress. Put this in a thread if it takes too long. or, put it there right away, postpone update of image
						EvPixels p=image.getPixels(new ProgressHandle()); /////////////////////////////////////////// HERE FOR LONG EXECUTIONS /////////////
						//System.out.println("------------ loaded pixels and got "+p);
						
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
		public void zoomToFit(Viewer2DView p)
			{
			loadImage();
			if(bufi!=null)
				{
				int w=(int)(bufi.getWidth());
				int h=(int)(bufi.getHeight());
							
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
		
		public void paintComponent(Graphics g, Viewer2DView p)
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
	
				
				Vector2d v1=p.transformPointW2S(new Vector2d(0,0));
				Vector2d v2=p.transformPointW2S(new Vector2d(100,0));
				Vector2d v3=p.transformPointW2S(new Vector2d(0,100));
				
				g2.setColor(Color.RED);
				g2.drawLine((int)v1.x, (int)v1.y, (int)v2.x, (int)v2.y);
				g2.drawLine((int)v1.x, (int)v1.y, (int)v3.x, (int)v3.y); 
				
				*/
//				stack.cs.transformToWorld(v)


				} 
			}
		
		


		/** Convert image coordinate to screen coordinate (image scaled by binning) */
		
		//Problem: should now be stack specific! currently ignores binning as stated above
		public static Vector2d transformI2S(Viewer2DView p, EvStack stack, Vector2d u)
			{
			return p.transformPointW2S(stack.transformImageWorld(new Vector2d(u.x,u.y)));
			//return p.transformPointW2S(new Vector2d(stack.transformImageWorldX(u.x),stack.transformImageWorldY(u.y)));
			}
		
		
		
		/** Convert screen coordinate to image coordinate (image scaled by binning) */
		//Problem: should now be stack specific!
		public static Vector2d transformS2I(Viewer2DView p, EvStack stack, Vector2d u)
			{
			return stack.transformWorldImage(p.transformPointS2W(u));
			//Vector2d v=p.transformPointS2W(u);
			//return new Vector2d(stack.transformWorldImageX(v.x),stack.transformWorldImageY(v.y));
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
		
	/*
	
	private static class MyAddCompositeContext implements CompositeContext
	{
	
	ColorModel srcColorModel;
	ColorModel dstColorModel;

	int ALPHA = 0xFF000000; // alpha mask
	int MASK7Bit = 0xFEFEFF; // mask for additive/subtractive shading

	final double wr;
	final double wg;
	final double wb;
	
	public MyAddCompositeContext(ColorModel srcColorModel, ColorModel dstColorModel, double wr, double wg, double wb) 
		{
		this.srcColorModel = srcColorModel;
		this.dstColorModel = dstColorModel;
		this.wr=wr;
		this.wg=wg;
		this.wb=wb;
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
				//int dp = dstColorModel.getRGB(dstPix);
				int dp = dstColorModel.getRed(dstPix);
				
				int np = ((int)(wr*dp))<<16 + ((int)(wg*dp))<<8 + ((int)(wb*dp));
				
				int rp = add(sp,np);
				//int rp = add(sp,dp);
				dstOut.setDataElements(x + dstOutRect.x, y + dstOutRect.y, dstColorModel.getDataElements(rp, null));
				}
		}

	public void dispose()
		{
		}

	}
*/
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	/*
	public void paintComponent(Graphics g)
		{
		loadImage();
		
		if(images.size()>0)
			{
			if(images.get(0).color.equals(Color.WHITE))
				{
				//Special case
				
				}
			g.setColor(Color.BLACK);
			g.fillRect(0,0,getWidth(),getHeight());
			
			
			images.get(0).paintComponent(g, this);
			}
		//Draw overlay additive
		Graphics2D g2d=(Graphics2D)g;
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
//		for(int i=1;i<images.size();i++)
			{
			//g2d.setColor(images.get(i).color.getAWTColor());
			g2d.setColor(Color.red);
			g2d.fillRect(50,50,100,100);

			}
		
		
		
		
		
		}*/
	
	
	/*
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
			
			//TODO. handle colors!!!!!!

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
		
		
		
		}*/

	

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
				//Graphics2D g2=(Graphics2D)temporaryPart.getGraphics();
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


