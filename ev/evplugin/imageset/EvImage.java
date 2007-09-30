package evplugin.imageset;

import java.awt.image.*;


/**
 * Interface to any form of image loader
 * @author Johan Henriksson
 */
public abstract class EvImage
	{
	/**
	 * In-memory image. Set to null if there is none.
	 */
	private BufferedImage im=null;
	
	/**
	 * Get AWT representation of image. This should be as fast as it can be, but since AWT has limitations, data might be lost.
	 * It is the choice for rendering or if AWT is guaranteed to be able to handle the image.
	 */
	public BufferedImage getJavaImage()
		{
		if(im==null)
			return loadJavaImage();
		else
			return im;
		}
	
	
	/**
	 * Modify image by setting a new image in this container. AWT format: Only use this format if no data will be lost.
	 */
	public void setImage(BufferedImage im)
		{
		this.im=im;
		}
	
	/**
	 * Load image from disk.
	 */
	protected abstract BufferedImage loadJavaImage();
	}
