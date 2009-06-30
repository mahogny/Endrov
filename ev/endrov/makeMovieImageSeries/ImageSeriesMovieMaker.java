package endrov.makeMovieImageSeries;

import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

import endrov.ev.EV;
import endrov.makeMovie.EvMovieMaker;

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

		
		
		File thisFile=new File(path,EV.pad(curframe, 8)+".png");
		curframe++;

		ImageIO.write(im, "png", thisFile);
		}

	public void done() throws Exception
		{
		}	
	
	
	}