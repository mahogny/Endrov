/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowPlateAnalysis.scene;

import java.util.*;
import java.awt.image.*;
import java.awt.*;

import javax.swing.*;
import javax.vecmath.Vector2d;


import endrov.util.math.Matrix2d;


/**
 * Scene graph for 2D views
 * 
 * TODO add hooks for drawing overlay?
 * TODO add viewports!
 * 
 * @author Johan Henriksson
 */
public class Scene2DView extends JPanel
	{
	static final long serialVersionUID=0;
	
	private LinkedList<Scene2DElement> images=new LinkedList<Scene2DElement>();
	private double zoom=1;
	private double rotation=0;
	private double transX=0, transY=0;

	
	public void addElem(Scene2DElement elem)
		{
		images.add(elem);
		}

	public void dataChangedEvent()
		{
		}
	
	/**
	 * Load image from imageloader if this has not been done yet
	 */
	private void prepareImages()
		{
		for(Scene2DElement e:images)
			if(e instanceof Scene2DImage)
				((Scene2DImage) e).prepareImage();
		}
	
	/*
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
		*/	

	public void paintComponent(Graphics g)
		{
		BufferedImage bim=new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2d=(Graphics2D)bim.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		
		prepareImages();

		transformIn(g2d);
		
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0,0,getWidth(),getHeight());

		for(Scene2DElement e:images)
			{
			
			e.paintComponent(g2d, this);
		
			
			/*
			if(i==0)
				{
				*/
//				images.get(0).paintComponent(g2d, this);
				/*   TODO need to determine if this should be. right now default
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
*/
			}
		
		transformOut(g2d);
		
		g.drawImage(bim, 0,0, null);
		}
	
	

	/**
	 * Pan by a certain amount
	 * @param dx Mouse movement X in pixels
	 * @param dy Mouse movement Y in pixels
	 */
	public void pan(double dx, double dy)
		{
		transX+=dx/zoom;
		transY+=dy/zoom;
		repaint();
		}


	
	
	/**
	 * Zoom image to fit panel
	 */
	public void zoomToFit()
		{
		//Build bounding box
		Rectangle rect=null;
		for(Scene2DElement e:images)
			{
			Rectangle r=e.getBoundingBox();
			if(r!=null)
				{
				if(rect==null)
					rect=r;
				else
					rect.add(r);
				}
			}

		//Zoom to fit
		if(rect!=null)
			{
			double w=rect.getWidth();
			double h=rect.getHeight();
			
			//Get zoom factor
			double zoom1=getWidth()/w;
			double zoom2=getHeight()/h;
			zoom=Math.min(zoom1,zoom2);

			//Place camera in the middle
			Vector2d mid=transformPointW2S(new Vector2d(w/2, h/2));
			mid.sub(new Vector2d(getWidth()/2,getHeight()/2));
			transX-=(int)mid.x/zoom;
			transY-=(int)mid.y/zoom;

			zoom(0.8);
			}
			
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
		
		
		v.add(new Vector2d(transX,transY)); 
		v.scale(zoom);
		v.add(new Vector2d(getWidth()/2.0, getHeight()/2.0));
		return v;
		}
		
	/** 
	 * Transform screen coordinate to world coordinate 
	 */
	public Vector2d transformPointS2W(Vector2d u)
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
	 * Transform screen vector to world vector 
	 */
	public Vector2d transformVectorS2W(Vector2d u)
		{
		Vector2d v=new Vector2d(u);
		v.scale(1.0/zoom);
		
		Matrix2d rotmat=new Matrix2d();
		rotmat.rot(-rotation);
		rotmat.transform(v);
		return v;
		}

	/**
	 * Remove all content
	 */
	public void clear()
		{
		images.clear();
		repaint();
		}

	/**
	 * Zoom by given factor
	 */
	public void zoom(double scale)
		{
		zoom*=scale;
		repaint();
		}

	private void transformIn(Graphics2D g2)
		{
		Vector2d trans=transformPointW2S(new Vector2d(0, 0));  //This should be in a common class!
		g2.translate(trans.x,trans.y);
		g2.scale(zoom, zoom);  
		g2.rotate(rotation);
		}

	private void transformOut(Graphics2D g2)
		{
		Vector2d trans=transformPointW2S(new Vector2d(0, 0));  //This should be in a common class!
		g2.rotate(-rotation);  //This should be in a common class!
		g2.scale(1/zoom,1/zoom); 
		g2.translate(-trans.x,-trans.y);
		}
	
	
	public void setRotation(double angle)
		{
		this.rotation=angle;
		repaint();
		}

	public double getRotation()
		{
		return rotation;
		}

	public double getZoom()
		{
		return zoom;
		}
	
	public void setZoom(double zoom)
		{
		this.zoom=zoom;
		repaint();
		}


	}


