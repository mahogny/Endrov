package evplugin.imageset;

import java.awt.image.*;


/**
 * Interface to any form of image loader
 * @author Johan Henriksson
 */
public abstract class EvImage
	{
	public BufferedImage getJavaImage()
		{
		return loadJavaImage();
		}
	
	public abstract BufferedImage loadJavaImage();
	
	/**
	 * Source of image; path to file if it is a single slice. Best used sparingly.
	 */
	//only used by OST imageset. ELIMINATE
	//public abstract String sourceName();
	}
