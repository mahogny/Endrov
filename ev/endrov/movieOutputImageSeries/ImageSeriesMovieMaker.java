/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.movieOutputImageSeries;

import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

import endrov.core.EndrovUtil;
import endrov.opMakeMovie.EvMovieMaker;

/**
 * Interface to quicktime. Encapsulates all shitty commands.
 */
public class ImageSeriesMovieMaker implements EvMovieMaker
	{
	private File path;
	//private int w;
	//private int h;
	
	private int curframe=0;
	
	public ImageSeriesMovieMaker(File path, int w, int h, String quality)
		{
		this.path=path;
		}
	
	public void addFrame(BufferedImage im) throws Exception
		{
		path.mkdirs();

		
		
		File thisFile=new File(path,EndrovUtil.pad(curframe, 8)+".png");
		curframe++;

		ImageIO.write(im, "png", thisFile);
		}

	public void done() throws Exception
		{
		}	
	
	
	}
