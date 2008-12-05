package endrov.imageset;

import java.awt.image.*;
import java.lang.ref.*;

import endrov.data.EvIOData;

/**
 * Interface to any form of image loader
 * @author Johan Henriksson
 */
public abstract class EvImage
	{
	
	public EvIOData io=null; //temp added

	
	/**
	 * In-memory image. Set to null if there is none.
	 */
	protected BufferedImage im=null;
	
	/**
	 * Cache: pointer to loaded image
	 */
	private SoftReference<BufferedImage> cachedImage=new SoftReference<BufferedImage>(null);
	
	/**
	 * Remove cached image. Can be called whenever.
	 */
	public void clearCachedImage()
		{
		cachedImage.clear();
		}
	
	/**
	 * Get AWT representation of image. This should be as fast as it can be, but since AWT has limitations, data might be lost.
	 * It is the choice for rendering or if AWT is guaranteed to be able to handle the image.
	 * 
	 * This image is read-only unless it is again set to be the AWT image of this EVImage. (best-practice?)
	 */
	public BufferedImage getJavaImage()
		{
		if(im==null)
			{
			CacheImages.addToCache(this);
			BufferedImage loaded=cachedImage.get();
			if(loaded==null)
				{
				loaded=loadJavaImage();
				cachedImage=new SoftReference<BufferedImage>(loaded);
				}
			return loaded;
			}
		else
			return im;
		}
	
	/**
	 * Get array representation of image.
	 * In the next-gen EV imaging API this will be the central function but at the moment
	 * the AWT interface is faster.
	 */
	public double[][] getArrayImage()
		{
		BufferedImage bim=getJavaImage();
		int w=bim.getWidth();
		int h=bim.getHeight();
		double[][] aim=new double[h][w];
		Raster r=bim.getRaster();
		for(int i=0;i<h;i++)
			r.getSamples(0, i, w, 1, 0, aim[i]);
		
		//I get 0-255. wtf? 0-1 better
		return aim;
		}
	
	/**
	 * Check if this memory has been modified since it was loaded into memory
	 */
	public boolean modified()
		{
		return im!=null;
		}
	
	/**
	 * Modify image by setting a new image in this container. AWT format: Only use this format if no data will be lost.
	 */
	public void setImage(BufferedImage im)
		{
		this.im=im;
		cachedImage.clear();
		cachedImage=new SoftReference<BufferedImage>(null);
		}
	
	/**
	 * Load image from disk.
	 */
	protected abstract BufferedImage loadJavaImage();
	
	
	/**
	 * Get a copy of this image that is legal to modify
	 */
	public EvImage getWritableCopy()
		{
		return new EvImageCopy(this);
		}
	
	public double transformImageWorldX(double c){return (c*getBinning()+getDispX())/getResX();}
	public double transformImageWorldY(double c){return (c*getBinning()+getDispY())/getResY();}			
	public double transformWorldImageX(double c){return (c*getResX()-getDispX())/getBinning();}
	public double transformWorldImageY(double c){return (c*getResY()-getDispY())/getBinning();}
	
	
	
	public double scaleImageWorldX(double c){return c/(getResX()/getBinning());}
	public double scaleImageWorldY(double c){return c/(getResY()/getBinning());}
	public double scaleWorldImageX(double c){return c*getResX()/getBinning();}
	public double scaleWorldImageY(double c){return c*getResY()/getBinning();}
	
	//how about a Z-transform too?

	public abstract int getBinning();
	public abstract double getDispX();
	public abstract double getDispY();
	public abstract double getResX();
	public abstract double getResY();
	
	/*
	public void finalize()
		{
		System.out.println("Removing image");
		}
		*/

	}
