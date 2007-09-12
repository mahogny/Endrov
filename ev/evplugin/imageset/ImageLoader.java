package evplugin.imageset;

import java.awt.image.*;


/**
 * Interface to any form of image loader
 * @author Johan Henriksson
 */
public interface ImageLoader
	{
	public BufferedImage loadImage();
	
	/**
	 * Source of image; path to file if it is a single slice. Best used sparingly.
	 */
	public String sourceName();
	}
