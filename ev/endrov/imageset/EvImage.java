package endrov.imageset;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.lang.ref.SoftReference;


/**
 * @author Johan Henriksson
 *
 */

public class EvImage  
	{
	
	//TODO copy constructor

	
	public EvImage()
		{
		}

	public EvImage(EvImage copy)
		{
		resX=copy.resX;
		resY=copy.resY;
		dispX=copy.dispX;
		dispY=copy.dispY;
		binning=copy.binning;
		io=copy.io;
		
		if(copy.im!=null)
			{
			im=new BufferedImage(copy.im.getWidth(),copy.im.getHeight(),copy.im.getType());
			im.getGraphics().drawImage(copy.im, 0, 0, null);
			}
		}
	
	
	/**
	 * Connection to I/O. This is how partial loading is implemented
	 */
	public EvIOImage io=null;
	
  /**
   * Force rewrite, such as change of compression
   */ 
	public boolean isDirty=false;   
	
	/**
	 * In-memory image. Set to null if there is none.
	 * 
	 * TODO new image representation
	 * 
	 * TODO loadJavaImage, what to replace it with?
	 * 
	 * class can be abstract. EvImageCopy would work, and then a *generic* EvImageWithLoader that
	 * has a pointer to the imageset
	 * 
	 * keeping pointers in program up to date will be a mess if abstract. better put in a loader reference.
	 * it can be swapped upon saving all images
	 * 
	 * 
	 */
	protected BufferedImage im=null;
	
	
	/**
	 * ONLY for use by I/O system
	 */
	public BufferedImage getMemoryImage()
		{
		return im;
		}
	/**
	 * ONLY for use by I/O system
	 */
	public void setMemoryImage(BufferedImage im)
		{
		this.im=im;
		}
	
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
				loaded=io.loadJavaImage();
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
		return im!=null || isDirty;
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
	
	
	
	//what to do about this? now it points to io
	
	public double transformImageWorldX(double c){return (c*getBinning()+getDispX())/getResX();}
	public double transformImageWorldY(double c){return (c*getBinning()+getDispY())/getResY();}			
	public double transformWorldImageX(double c){return (c*getResX()-getDispX())/getBinning();}
	public double transformWorldImageY(double c){return (c*getResY()-getDispY())/getBinning();}
	public double scaleImageWorldX(double c){return c/(getResX()/getBinning());}
	public double scaleImageWorldY(double c){return c/(getResY()/getBinning());}
	public double scaleWorldImageX(double c){return c*getResX()/getBinning();}
	public double scaleWorldImageY(double c){return c*getResY()/getBinning();}
	
	
	
	
	//Is this the final solution? Probably not, it will be moved to Stack level(?). or homogenized.
	//but it's a quick patch
	
	public double resX, resY, binning;
	public double dispX, dispY;
	
	public double getResX()
		{
		return resX;
		}
	public double getResY()
		{
		return resY;
		}
	public double getBinning()
		{
		return binning;
		}
	
	public double getDispX()
		{
		return dispX;
		}
	public double getDispY()
		{
		return dispY;
		}
	
	
	}
