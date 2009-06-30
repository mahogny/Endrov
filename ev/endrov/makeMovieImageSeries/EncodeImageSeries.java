package endrov.makeMovieImageSeries;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import endrov.makeMovie.EvMovieMaker;
import endrov.makeMovie.EvMovieMakerFactory;

/**
 * Encode movies as a series of files
 * @author Johan Henriksson
 */
public class EncodeImageSeries
	{
	public static void initPlugin()	{}
	static
		{
			EvMovieMakerFactory.makers.add(new EvMovieMakerFactory()
			{
				public EvMovieMaker getInstance(File path, int w, int h, String quality) throws Exception
					{
					return new ImageSeriesMovieMaker(path,w,h,quality);
					}

				public String getName() 
					{
					return "PNG images";
					}

				public String toString()
					{
					return getName();
					}
				
				public List<String> getQualities() 
					{
					return Arrays.asList("Lossless");
					}

				public String getDefaultQuality()
					{
					return "Lossless";
					}
				
				
			});
		}
	
	
	
	}
