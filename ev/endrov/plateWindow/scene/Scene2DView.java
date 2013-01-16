/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.plateWindow.scene;

import java.util.*;
import java.awt.image.*;
import java.awt.*;

import javax.swing.*;
import javax.vecmath.Vector2d;


import endrov.util.Matrix2d;


/**
 * Scene graph for 2D views
 * 
 * TODO add hooks for drawing overlay?
 * 
 * TODO add viewports!
 * 
 * @author Johan Henriksson
 */
public class Scene2DView extends JPanel
	{
	static final long serialVersionUID=0;
	
	
	
	private LinkedList<Scene2DElement> images=new LinkedList<Scene2DElement>();
	public double zoom=1;
	public double rotation=0;
	public double transX=0, transY=0;

	
	public void addElem(Scene2DElement elem)
		{
		images.add(elem);
		}

	public void dataChangedEvent()
		{
		}

	
	
	public boolean checkIfTransformOk()
		{
		return zoom!=0; 
		}
	
	/**
	 * Tell that images need be reloaded from disk.
	 * Better register callbacks from images instead
	 */
	/*
	public void invalidateImages()
		{
		for(Scene2DElement e:images)
			if(e instanceof Scene2DImage)
//		for(ImagePanelImage im:images)
				((Scene2DImage) e).update();
		}
	*/
	/**
	 * Load image from imageloader if this has not been done yet
	 *
	 */
	private void loadImage()
		{
		for(Scene2DElement e:images)
			if(e instanceof Scene2DImage)
				((Scene2DImage) e).prepareImage();
//		for(ImagePanelImage im:images)
	//		im.loadImage();
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

		loadImage();

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
		repaint();
		}


	
	
	/**
	 * Zoom image to fit panel
	 */
	public void zoomToFit()
		{
		Rectangle rect=null;
		
//		loadImage();
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
		/*
			if(e instanceof Scene2DImage)
				((Scene2DImage) e).zoomToFit(this);
		*/
		
		if(rect!=null)
			{
			
			double w=rect.getWidth();
			double h=rect.getHeight();

			
			//Adjust zoom
			double zoom1=getWidth()/w;
			double zoom2=getHeight()/h;
			zoom=Math.min(zoom1,zoom2);

			//Place camera in the middle
			
			Vector2d mid=transformPointW2S(new Vector2d(w/2, h/2));
					//transformI2S(p,stack,new Vector2d(w/2,h/2));
			mid.sub(new Vector2d(getWidth()/2,getHeight()/2));
			transX-=(int)mid.x/zoom;
			transY-=(int)mid.y/zoom;

			
			}
			
		
//		if(images.size()>0)
//			images.get(0).zoomToFit(this);
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

	public void clear()
		{
		images.clear();
		repaint();
		}

	public void zoom(double scale)
		{
		zoom*=scale;
		repaint();
		}
	}


