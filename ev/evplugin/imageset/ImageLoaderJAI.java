package evplugin.imageset;

import javax.imageio.ImageIO;
import com.sun.media.jai.codec.*;
import java.io.File;
import java.awt.image.*;
import evplugin.ev.*;

//static int getNumDirectories(SeekableStream stream)

/**
 * Loader of images from single slice images using JAI
 * @author Johan Henriksson
 */
public class ImageLoaderJAI implements ImageLoader
	{
	private String filename;
	private int slice;
	
	/**
	 * Load a single-slice image
	 */
	public ImageLoaderJAI(String filename)
		{
		this.filename=filename;
		this.slice=-1;
		}

	/**
	 * Load a slice in a stack
	 */
	public ImageLoaderJAI(String filename, int slice)
		{
		this.filename=filename;
		this.slice=slice;
		}

	
	public String sourceName()
		{
		if(slice==-1)
			return filename;
		else
			return filename+":"+slice;
		}

	
	/**
	 * Load the image
	 */
	public BufferedImage loadImage()
		{
		try
			{
			File file=new File(filename);
			if(!file.exists())
				return null;
			else
				{
				if(slice==-1)
					{
					//Single-slice image
					return ImageIO.read(file);
					}
				else
					{
					//Multi-slice image
					//Only one type supported right now: tiff stacks. so assume this is the type
					SeekableStream s = new FileSeekableStream(file);
					TIFFDecodeParam param = null;
	        ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);

	        EV.printDebug("Number of images in this TIFF: " + dec.getNumPages());

	        Raster ir=dec.decodeAsRaster();
	        BufferedImage bim=new BufferedImage(ir.getWidth(),ir.getHeight(),ir.getSampleModel().getDataType());
	        WritableRaster wr=bim.getRaster();
	        wr.setRect(ir);
	        return bim;
					}
				
				
				}
			}
		catch(Exception e)
			{
			EV.printError("Failed to read image "+filename,e);
			return null;
			}
		}
	}

