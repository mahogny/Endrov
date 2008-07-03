package endrov.jubio;

import java.awt.image.*;

import endrov.ev.*;
import endrov.imageset.EvImage;
/**
 * An imageloader using Jubio
 * @author Johan Henriksson
 */
public abstract class EvImageJubio extends EvImage
	{
	private String filename;
	private int slice;

	
	public EvImageJubio(String filename, int slice)
		{
		this.filename=filename;
		this.slice=slice;
		}

	public String sourceName()
		{
		return filename;
		}

	
	public BufferedImage loadJavaImage()
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
