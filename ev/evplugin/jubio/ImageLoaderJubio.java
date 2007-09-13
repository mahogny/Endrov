package evplugin.jubio;

import java.awt.image.*;

import evplugin.imageset.ImageLoader;
import evplugin.ev.*;
/**
 * An imageloader using Jubio
 * @author Johan Henriksson
 */
public class ImageLoaderJubio implements ImageLoader
	{
	private String filename;
	private int slice;

	
	public ImageLoaderJubio(String filename, int slice)
		{
		this.filename=filename;
		this.slice=slice;
		}

	public String sourceName()
		{
		return filename;
		}

	
	public BufferedImage loadImage()
		{
		try
			{
			Jubio jubio=new Jubio(filename,slice);
			return jubio.getBufferedImage();
			}
		catch (Exception e)
			{
			Log.printError("Could not load image",e);
			return null;
			}
		}
	
	
	}
