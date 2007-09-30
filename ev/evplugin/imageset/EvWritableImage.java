package evplugin.imageset;

import java.awt.image.*;


/**
 * Loader of images from single slice images using JAI
 * @author Johan Henriksson
 */
public class EvWritableImage extends EvImage
	{
	private String filename;
	private BufferedImage im;
	
	/**
	 * Load a single-slice image
	 */
	public EvWritableImage(String filename, BufferedImage im)
		{
		this.filename=filename;
		this.im=im;
		}

	
	
	public String sourceName()
		{
		return filename;
		}

	
	/**
	 * Load the image (internally just pass it)
	 */
	public BufferedImage loadJavaImage()
		{
		return im;
		}
	
	/**
	 * Set image
	 */
	public void setImage(BufferedImage im)
		{
		this.im=im;
		}
	
	
	}

